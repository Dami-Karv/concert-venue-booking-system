//kwdikas pou arxikopoiei to sistima otan fortothei h selida
document.addEventListener('DOMContentLoaded', () => {
    const container = document.querySelector('.concert-container');
    const addButton = document.querySelector('.add-item');
    const eventManagementBtn = document.getElementById('eventManagementBtn');
    const paymentLogsBtn = document.getElementById('paymentLogsBtn');
    const eventManagementSection = document.getElementById('eventManagementSection');
    const paymentLogsSection = document.getElementById('paymentLogsSection');
    let isFormActive = false;

    loadEvents();
    setupEventListeners();
    //dimiourgei tous event listeners gia ola ta koumpia kai ta filtrarismata
    function setupEventListeners() {
        
        eventManagementBtn.addEventListener('click', showEventManagement);
        paymentLogsBtn.addEventListener('click', showPaymentLogs);

        const paymentTypeFilter = document.getElementById('paymentTypeFilter');
        const dateFilter = document.getElementById('dateFilter');
        const refreshLogsBtn = document.getElementById('refreshLogs');

        if (paymentTypeFilter) paymentTypeFilter.addEventListener('change', loadPaymentLogs);
        if (dateFilter) dateFilter.addEventListener('change', loadPaymentLogs);
        if (refreshLogsBtn) refreshLogsBtn.addEventListener('click', loadPaymentLogs);

        if (addButton) {
            addButton.addEventListener('click', async () => {
                const details = await promptForConcertDetails();
                if (details) {
                    await addConcert(details);
                }
            });
        }
    }


    //emfanizei tin selida me ta istorika pliromon kai krivei tin diaxeirisi ekdiloseon
    function showPaymentLogs() {
        eventManagementSection.style.display = 'none';
        paymentLogsSection.style.display = 'block';
        paymentLogsBtn.classList.add('active');
        eventManagementBtn.classList.remove('active');
        loadPaymentLogs();
    }

    //enimeroni ta statistika stoixeia ton ekdiloseon kai pliromon
    function updateStatistics(payments) {
        let eventStats = {};  
        
        payments.forEach(payment => {
            const amount = payment.amount;
            const eventName = payment.event_name;
            
            if (!eventStats[eventName]) {
                eventStats[eventName] = { 
                    payments: 0, 
                    refunds: 0, 
                    netCount: 0, 
                    netRevenue: 0 
                };
            }
            
            if (payment.status === 1) {  
                eventStats[eventName].payments++;
                eventStats[eventName].netCount++;
                eventStats[eventName].netRevenue += amount;
            } else {  
                eventStats[eventName].refunds++;
                eventStats[eventName].netCount--;
                eventStats[eventName].netRevenue -= amount;
            }
        });
        
        let mostPopularEvent = '-';
        let highestRevenueEvent = '-';
        let maxNetCount = 0;
        let maxNetRevenue = 0;
        
        Object.entries(eventStats).forEach(([eventName, stats]) => {
            if (stats.netCount > maxNetCount) {
                maxNetCount = stats.netCount;
                mostPopularEvent = eventName;
            }
            if (stats.netRevenue > maxNetRevenue) {
                maxNetRevenue = stats.netRevenue;
                highestRevenueEvent = eventName;
            }
        });
        
        document.getElementById('mostPopularEvent').textContent = 
            mostPopularEvent !== '-' ? 
            `${mostPopularEvent} (${maxNetCount} active bookings)` : '-';
        
        document.getElementById('highestRevenueEvent').textContent = 
            highestRevenueEvent !== '-' ? 
            `${highestRevenueEvent} (€${maxNetRevenue.toFixed(2)})` : '-';
    }

    //fortoni to istoriko pliromon apo ton server me vasi ta epilegmena filtra
    async function loadPaymentLogs() {
        try {
            const typeFilter = document.getElementById('paymentTypeFilter').value;
                
            let url = '/hy360-project-2024/api/payments';
            if (typeFilter !== 'all') {
                url += `?type=${typeFilter}`;
            }
    
            document.getElementById('loadingSpinner').style.display = 'block';
            
            const response = await fetch(url);
            const data = await response.json();
            
            if (data.success) {
                displayPaymentLogs(data.payments);
                updateFinancialSummary(data.payments);  
                updateStatistics(data.payments);    
            } else {
                showMessage(data.message || 'Failed to load payment logs', true);
            }
        } catch (error) {
            console.error('error loading the  logs:', error);
            showMessage('failed to load payment logs', true);
        } finally {
            document.getElementById('loadingSpinner').style.display = 'none';
        }
    }


    //emfanizei to istoriko pliromon ston pinaka tis selidas
    function displayPaymentLogs(payments) {
        const tbody = document.getElementById('paymentLogsBody');
        tbody.innerHTML = '';

        payments.forEach(payment => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${new Date(payment.payment_date).toLocaleString()}</td>
                <td>${payment.customer_name}</td>
                <td>${payment.event_name}</td>
                <td>**** ${payment.card_number.slice(-4)}</td>
                <td>€${payment.amount.toFixed(2)}</td>
                <td>
                    <span class="${payment.status === 1 ? 'payment-status' : 'refund-status'}">
                        ${payment.status === 1 ? 'Payment' : 'Refund'}
                    </span>
                </td>
            `;
            tbody.appendChild(row);
        });

        updateStatistics(payments);
    }


    //enimeroni tin sinoptiki ikona ton oikonomikon stoixeion
    function updateFinancialSummary(payments) {
        let totalPayments = 0;
        let totalRefunds = 0;

        payments.forEach(payment => {
            if (payment.status === 1) {
                totalPayments += payment.amount;
            } else {
                totalRefunds += payment.amount;
            }
        });

        const netRevenue = totalPayments - totalRefunds;

        document.getElementById('totalPayments').textContent = `€${totalPayments.toFixed(2)}`;
        document.getElementById('totalRefunds').textContent = `€${totalRefunds.toFixed(2)}`;
        document.getElementById('netRevenue').textContent = `€${netRevenue.toFixed(2)}`;
    }


    //emfanizei tin selida diaxeirisis ekdiloseon kai krivei to istoriko pliromon
    function showEventManagement() {
        eventManagementSection.style.display = 'block';
        paymentLogsSection.style.display = 'none';
        eventManagementBtn.classList.add('active');
        paymentLogsBtn.classList.remove('active');
        loadEvents();
    }
    
    //fortoni oles tis ekdiloseis apo ton server kai tis emfanizei
    async function loadEvents() {
        try {
            const paymentResponse = await fetch('/hy360-project-2024/api/payments');
            const paymentData = await paymentResponse.json();
            
            const response = await fetch('/hy360-project-2024/api/events');
            const text = await response.text();
            console.log('response:', text);
            
            let events = JSON.parse(text);
            
            if (!Array.isArray(events)) {
                console.error('error', events);
                throw new Error('error');
            }
            
            events = events.map(event => {
                event.ticketRevenue = paymentData.statistics?.eventTicketRevenue?.[event.event_id]?.tickets || {};
                return event;
            });
            
            const existingEvents = container.querySelectorAll('.concert-item');
            existingEvents.forEach(event => event.remove());
    
            events.forEach(event => {
                const eventElement = createConcertItem(event);
                container.insertBefore(eventElement, addButton);
            });
        } catch (error) {
            console.error('error loading events:', error);
            showMessage('failed to load events: ' + error.message, true);
        }
    }

    //dimiourgei ena neo stoixeio DOM gia tin emfanisi mias ekdilosis
    function createConcertItem(event) {
        const concertItem = document.createElement('div');
        concertItem.classList.add('concert-item');
        concertItem.style.height = '450px';
        concertItem.style.overflowY = 'auto';
    
        let totalRevenue = 0;
        let ticketsHtml = '<div class="ticket-info">';
        if (event.tickets && event.tickets.length > 0) {
            ticketsHtml += '<h3>Ticket Types:</h3><ul>';
            event.tickets.forEach(ticket => {
                const revenue = ticket.revenue || 0;
                totalRevenue += revenue;
                ticketsHtml += `
                    <li>
                        <strong>${ticket.type_name}</strong><br>
                        Price: €${ticket.price}<br>
                        Available: ${ticket.quantity_available}<br>
                        Revenue: €${revenue.toFixed(2)}
                    </li>
                `;
            });
            ticketsHtml += '</ul>';
            ticketsHtml += `
                <div class="total-revenue">
                    <h3>Total Revenue:</h3>
                    <p>€${totalRevenue.toFixed(2)}</p>
                </div>
            `;
        } else {
            ticketsHtml += '<p>No ticket types defined</p>';
        }
        ticketsHtml += '</div>';
    
        concertItem.innerHTML = `
            <h2>${event.name}</h2>
            <p>Date: ${new Date(event.event_date).toLocaleDateString()}</p>
            <p>Time: ${event.event_time.substring(0, 5)}</p>
            <p>Type: ${event.event_type}</p>
            <p>Capacity: ${event.venue_capacity}</p>
            ${ticketsHtml}
            <button class="remove-btn" onclick="removeConcert(${event.event_id})">Cancel Event</button>
        `;
    
        return concertItem;
    }

    //analisi ton kratiseon gia sigkekrimeno xroniko diastima
    async function analyzeBookings() {
        const startDate = document.getElementById('startDate').value;
        const endDate = document.getElementById('endDate').value;
    
        if (!startDate || !endDate) {
            showMessage('Please select both start and end dates', true);
            return;
        }
    
        try {
            const response = await fetch('/hy360-project-2024/api/events');
            const events = await response.json();
            const bookingsResponse = await fetch('/hy360-project-2024/api/payments');
            const bookingsData = await bookingsResponse.json();
    
            const eventBookings = {};
            
            // First, filter events within the date range
            const filteredEvents = events.filter(event => {
                const eventDate = new Date(event.event_date);
                return eventDate >= new Date(startDate) && 
                       eventDate <= new Date(endDate);
            });
    
            // Create mapping of event IDs to event names for filtered events
            const eventMap = filteredEvents.reduce((map, event) => {
                map[event.event_id] = {
                    name: event.name,
                    date: event.event_date
                };
                return map;
            }, {});
    
            // Process bookings only for events within the date range
            bookingsData.payments.forEach(payment => {
                if (eventMap[payment.event_id]) {  // Only process if event is within date range
                    const eventName = eventMap[payment.event_id].name;
                    
                    if (!eventBookings[eventName]) {
                        eventBookings[eventName] = {
                            count: 0,
                            revenue: 0,
                            activeBookings: 0,
                            date: eventMap[payment.event_id].date
                        };
                    }
                    
                    if (payment.status === 1) {  
                        eventBookings[eventName].count++;
                        eventBookings[eventName].revenue += payment.amount;
                        eventBookings[eventName].activeBookings++;
                    } else {  
                        eventBookings[eventName].revenue -= payment.amount;
                        eventBookings[eventName].activeBookings--;
                    }
                }
            });
    
            let maxRevenue = 0;
            let mostProfitableEvent = 'None';
            Object.entries(eventBookings).forEach(([eventName, stats]) => {
                if (stats.revenue > maxRevenue && stats.activeBookings > 0) {
                    maxRevenue = stats.revenue;
                    mostProfitableEvent = eventName;
                }
            });
    
            let bookingsHtml = '<div class="bookings-breakdown">';
            Object.entries(eventBookings)
                .filter(([_, stats]) => stats.activeBookings > 0)
                .sort((a, b) => new Date(a[1].date) - new Date(b[1].date))  // Sort by event date
                .forEach(([eventName, stats]) => {
                    const eventDate = new Date(stats.date).toLocaleDateString();
                    bookingsHtml += `
                        <div class="event-stat">
                            <span class="event-name">${eventName} (${eventDate})</span>
                            <span class="booking-count">${stats.activeBookings} bookings</span>
                        </div>
                    `;
                });
            bookingsHtml += '</div>';
    
            document.getElementById('bookingResults').innerHTML = `
                <div class="analysis-results">
                    <div class="bookings-list">
                        <h4>Event Bookings:</h4>
                        ${bookingsHtml}
                    </div>
                    <div class="most-profitable">
                        <h4>Most Profitable Event:</h4>
                        ${mostProfitableEvent !== 'None' ? `
                            <p>${mostProfitableEvent}</p>
                            <p>Revenue: €${maxRevenue.toFixed(2)}</p>
                        ` : '<p>No profitable events in this period</p>'}
                    </div>
                </div>
            `;
    
        } catch (error) {
            console.error('Error analyzing bookings:', error);
            showMessage('Failed to analyze bookings: ' + error.message, true);
        }
    }
    
    
    //prosthiki event listener gia to koumpi analysis
    document.getElementById('analyzeButton').addEventListener('click', analyzeBookings);

    //diaxeirisi tis apothikeysis tou xristi
    window.handleLogout = async function() {
        try {
            const response = await fetch('/hy360-project-2024/api/logout', {
                method: 'POST'
            });
            
            const data = await response.json();
            if (data.success) {
                window.location.href = 'reglog.html';
            } else {
                showMessage(data.message || 'Logout failed', true);
            }
        } catch (error) {
            console.error('error', error);
            window.location.href = 'reglog.html';
        }
    };

    //dimiourgei kai diaxeirizetai ti forma eisagogis mias neas ekdilosis
    function promptForConcertDetails() {
        if (isFormActive) {
            alert('Please complete the current form first.');
            return;
        }
    
        isFormActive = true;
        const detailsForm = document.createElement('div');
        detailsForm.classList.add('event-form');
    
        const today = new Date().toISOString().split('T')[0];
    
        detailsForm.innerHTML = `
            <label>Name: <input type="text" id="concertName" required></label>
            <label>Date: <input type="date" id="concertDate" min="${today}" required></label>
            <label>Time: <input type="time" id="concertTime" required></label>
            <label>Capacity: <input type="number" id="concertCapacity" required min="1" max="1500"></label>
            <label>Type: 
                <select id="concertType" required>
                    <option value="Concert">Concert</option>
                    <option value="Theatre">Theatre</option>
                    <option value="Musical">Musical</option>
                    <option value="Comedy">Comedy</option>
                </select>
            </label>
            <p class="capacity-note">Maximum venue capacity: 1500</p>
            <div id="ticketDetailsContainer">
                <p>Ticket Details:</p>
                <div class="ticket-holder">
                    <label>Ticket Type: <input type="text" class="ticketType" required></label>
                    <label>Price: <input type="number" class="ticketPrice" min="0" required></label>
                    <label>Amount: <input type="number" class="ticketAmount" min="1" required></label>
                </div>
            </div>
            <button id="addTicketType">+ Add Ticket Type</button>
            <button id="submitDetails">Submit</button>
            <button id="cancelDetails">Cancel</button>
        `;
    
        document.body.appendChild(detailsForm);
    
        const capacityInput = document.getElementById('concertCapacity');
        const ticketDetailsContainer = document.getElementById('ticketDetailsContainer');
    
        document.getElementById('addTicketType').addEventListener('click', () => {
            const ticketHolder = document.createElement('div');
            ticketHolder.classList.add('ticket-holder');
            ticketHolder.innerHTML = `
                <label>Ticket Type: <input type="text" class="ticketType" required></label>
                <label>Price: <input type="number" class="ticketPrice" min="0" required></label>
                <label>Amount: <input type="number" class="ticketAmount" min="1" required></label>
                <button class="remove-ticket-btn">Remove</button>
            `;
            ticketDetailsContainer.appendChild(ticketHolder);
        
            ticketHolder.querySelector('.remove-ticket-btn').addEventListener('click', () => {
                ticketHolder.remove();
            });
        });
    
        ticketDetailsContainer.addEventListener('input', (event) => {
            if (event.target.classList.contains('ticketAmount')) {
                const totalTickets = Array.from(
                    document.querySelectorAll('.ticketAmount')
                ).reduce((sum, input) => sum + parseInt(input.value || 0, 10), 0);
        
                if (totalTickets > parseInt(capacityInput.value)) {
                    alert('Total tickets cannot exceed event capacity.');
                    event.target.value = '';
                }
            }
        });
    
        return new Promise((resolve) => {
            document.getElementById('submitDetails').addEventListener('click', () => {
                const totalTickets = Array.from(document.querySelectorAll('.ticketAmount'))
                    .reduce((sum, input) => sum + parseInt(input.value || 0, 10), 0);
                
                const capacity = parseInt(document.getElementById('concertCapacity').value);
                
                if (totalTickets > capacity) {
                    alert('Total tickets cannot exceed venue capacity');
                    return;
                }
                
                if (totalTickets > 1500) {
                    alert('Total tickets cannot exceed 1500');
                    return;
                }
    
                const ticketDetails = Array.from(document.querySelectorAll('.ticket-holder'))
                    .map((holder) => ({
                        ticket_type: holder.querySelector('.ticketType').value,
                        ticket_price: holder.querySelector('.ticketPrice').value,
                        ticket_amount: holder.querySelector('.ticketAmount').value,
                    }));
    
                const details = {
                    name: document.getElementById('concertName').value,
                    event_date: document.getElementById('concertDate').value,
                    event_time: document.getElementById('concertTime').value + ':00',
                    venue_capacity: totalTickets,
                    event_type: document.getElementById('concertType').value,
                    tickets: ticketDetails,
                };
    
                if (Object.values(details).some(value => !value) || 
                    ticketDetails.some(ticket => !ticket.ticket_type || !ticket.ticket_price || !ticket.ticket_amount)) {
                    alert('Please fill all fields');
                    return;
                }
    
                detailsForm.remove();
                isFormActive = false;
                resolve(details);
            });
            
            document.getElementById('cancelDetails').addEventListener('click', () => {
                detailsForm.remove();
                isFormActive = false;
                resolve(null);
            });
        });
    }

    //dimiourgei mia nea ekdilosi stelontas ta dedomena sto server
    async function addConcert(details) {
        try {
            console.log('error', details);
            
            const eventResponse = await fetch('/hy360-project-2024/api/events/create', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    name: details.name,
                    event_type: details.event_type,
                    event_date: details.event_date,
                    event_time: details.event_time,
                    venue_capacity: details.venue_capacity
                })
            });
    
            const eventResult = await eventResponse.json();
            console.log('error', eventResult);
    
            if (!eventResult.success) {
                throw new Error(eventResult.message);
            }
    
            if (details.tickets && details.tickets.length > 0) {
                console.log('error', eventResult.eventId);
                
                await fetch('/hy360-project-2024/api/ticket-types', {  
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        eventId: eventResult.eventId,
                        tickets: details.tickets
                    })
                });
            }
    
            showMessage('Event created successfully!');
            await loadEvents();
    
        } catch (error) {
            console.error('error', error);
            showMessage('failed to create event: ' + error.message, true);
        }
    }

    //diagrafei mia ekdilosi me vasi to ID tis kai ananeonei tin othonh
    window.removeConcert = async function(eventId) {
        if (!confirm('Are you sure you want to cancel this event?')) {
            return;
        }
    
        try {
            const response = await fetch('/hy360-project-2024/api/deleteEvent', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `eventId=${eventId}`
            });
    
            const data = await response.json();
            
            if (data.success) {
                showMessage(data.message);
                await loadEvents();
            } else {
                throw new Error(data.message || 'Failed to cancel event');
            }
        } catch (error) {
            console.error('error removing event:', error);
            showMessage('Failed to remove event: ' + error.message, true);
        }
    }

    //emfanizei prosorina minimata epitixias h sfalmatos sto xristh
    function showMessage(message, isError = false) {
        const messageContainer = document.getElementById('messageContainer');
        const messageElement = document.createElement('div');
        messageElement.className = isError ? 'error-message' : 'success-message';
        messageElement.textContent = message;
        
        messageContainer.innerHTML = '';
        messageContainer.appendChild(messageElement);
        
        setTimeout(() => {
            messageElement.classList.add('fade-out');
            setTimeout(() => {
                messageElement.remove();
            }, 300);
        }, 2700);
    }

    if (addButton) {
        addButton.style.width = '112.5px';
        addButton.style.height = '225px';
        addButton.style.cursor = 'pointer';
    }
    
    if (container) {
        container.style.display = 'flex';
        container.style.flexWrap = 'nowrap';
        container.style.overflowX = 'auto';
        container.style.gap = '20px';
        container.style.width = '100%';
        container.style.whiteSpace = 'nowrap';
    }
});