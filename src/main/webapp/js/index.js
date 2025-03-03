//elegxei tin katastasi syndesis tou xristi kai enimeronei tin emfanisi antistoixa
async function checkLoginStatus() {
    try {
        const response = await fetch('/hy360-project-2024/api/user/profile');
        const data = await response.json();
        
        const userIcon = document.querySelector('.user-icon');
        const authLink = document.querySelector('.auth-link');
        
        if (data.success) {
            userIcon.style.display = 'inline-block';
            authLink.style.display = 'none';
        } else {
            userIcon.style.display = 'none';
            authLink.style.display = 'inline-block';
        }
    } catch (error) {
        const userIcon = document.querySelector('.user-icon');
        const authLink = document.querySelector('.auth-link');
        userIcon.style.display = 'none';
        authLink.style.display = 'inline-block';
    }
}

document.addEventListener('DOMContentLoaded', async () => {    
        await loadEvents();  
        await checkLoginStatus();     
});



let selectedTicketInfo = null;

//diaxeirizete tin diadikasia syndesis tou xristi
async function handleLogin(event) {
    event.preventDefault();
    clearMessage();
    
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    const response = await fetch(`${API_BASE_URL}/login`, {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: `email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}`
    });

    const data = await response.json();
    
    if (data.success) {
        showMessage('Login successful! Redirecting...', false);
        setTimeout(() => {
            window.location.replace('index.html');  
        }, 1500);
    } else {
        showMessage('Invalid email or password', true);
    }
}


//fortoni oles tis ekdilosis apo ton server
async function loadEvents() {
    try {
        const response = await fetch('/hy360-project-2024/api/events');
        const data = await response.json();
        if (data && data.events && Array.isArray(data.events)) {
            createShowBoxes(data.events);
        } else if (Array.isArray(data)) {
            createShowBoxes(data);
        } else {
            console.error('Invalid response format:', data);
            showMessage('Error loading events', true);
        }
    } catch (error) {
        console.error('error', error);
        showMessage('Failed to load events', true);
    }
}

//dimiourgei ta koutakia emfanisis ton ekdiloseon stin arxiki selida
function createShowBoxes(events) {
    const showsContainer = document.getElementById('showsContainer');
    showsContainer.innerHTML = '';

    if (events.length === 0) {
        showsContainer.innerHTML = '<p class="no-events">No events currently available.</p>';
        return;
    }

    events.forEach(event => {
        const eventDate = new Date(event.event_date).toLocaleDateString();
        const eventTime = event.event_time.slice(0, 5);

        let totalAvailable = 0;
        let ticketTypes = [];
        if (event.tickets && event.tickets.length > 0) {
            event.tickets.forEach(ticket => {
                totalAvailable += ticket.quantity_available;
                ticketTypes.push(`${ticket.type_name}: ${ticket.quantity_available}`);
            });
        }

        const showBox = document.createElement('div');
        showBox.className = 'show-box';
        showBox.innerHTML = `
            <h2>${event.name}</h2>
            <div class="event-info">
                <p><i class="event-date"></i> ${eventDate}</p>
                <p><i class="event-time"></i> ${eventTime}</p>
                <p><i class="event-type"></i> ${event.event_type}</p>
                <div class="ticket-summary">
                    <h4>Available Tickets:</h4>
                    ${ticketTypes.map(type => `<p>${type}</p>`).join('')}
                    <p class="total-tickets">Total: ${totalAvailable}/${event.venue_capacity}</p>
                </div>
            </div>
            <button onclick="showDetails(${event.event_id})" class="view-details">View Details</button>
        `;
        showsContainer.appendChild(showBox);
    });
}

//emfanizei tis leptomeries mias ekdilosis kai tis epiloges eisitirion
function showDetails(eventId) {
    hideCardSelection();
    
    fetch(`/hy360-project-2024/api/events/${eventId}`)
        .then(response => response.json())
        .then(event => {
            const modal = document.getElementById('showDetails');
            const eventDate = new Date(event.event_date).toLocaleDateString();
            const eventTime = event.event_time.slice(0, 5);

  
            const ticketOptionsHtml = event.tickets.map(ticket => `
                <div class="ticket-option ${ticket.quantity_available === 0 ? 'sold-out' : ''}" 
                     onclick="selectTicket(this, ${ticket.type_id}, '${ticket.type_name}', ${ticket.price}, ${ticket.quantity_available})">
                    <h3>${ticket.type_name}</h3>
                    <div class="price">€${ticket.price.toFixed(2)}</div>
                    <div class="availability">
                        ${ticket.quantity_available} tickets available
                    </div>
                </div>
            `).join('');

         
            modal.innerHTML = `
                <div class="modal-content">
        <button class="close-button" onclick="hideDetails()">&times;</button>
                    
                    <div class="event-header">
                        <h1>${event.name}</h1>
                        <div class="event-meta">
                            <span class="date">${eventDate}</span>
                            <span class="time">${eventTime}</span>
                            <span class="type">${event.event_type}</span>
                        </div>
                    </div>
                    
                    <div class="ticket-section">
                        <h2>Select Tickets</h2>
                        <div class="ticket-options">
                            ${ticketOptionsHtml}
                        </div>
                        
                        <button id="bookNowBtn" 
                                class="book-button" 
                                onclick="initiateBooking(${event.event_id})" 
                                disabled>
                            Book Now
                        </button>
                    </div>
                </div>
            `;

            modal.style.display = 'flex';
            document.body.style.overflow = 'hidden';
        })
        .catch(error => {
            console.error('error', error);
            showMessage('Failed to load event details', true);
        });
}

//epilogi typou eisitiriou kai energopoiisi tou koumpiou kratisis
function selectTicket(element, typeId, typeName, price, available) {
    if (available === 0) return;


    document.querySelectorAll('.ticket-option').forEach(opt => 
        opt.classList.remove('selected'));
    

    element.classList.add('selected');
    

    document.getElementById('bookNowBtn').disabled = false;
    

    selectedTicketInfo = { typeId, typeName, price, available };
}

//ksekinaei ti diadikasia kratisis eisitiriou
async function initiateBooking(eventId) {
    if (!selectedTicketInfo) {
      
        return;
    }

    try {
        const response = await fetch('/hy360-project-2024/api/user/profile');
        const data = await response.json();

        if (!data.success) {
            if (confirm('Please log in to continue booking. Go to login page?')) {
                window.location.href = 'reglog.html';
            }
            return;
        }

        const cardsResponse = await fetch('/hy360-project-2024/api/user/cards');
        const cardsData = await cardsResponse.json();

        if (!cardsData.success || !cardsData.cards.length) {
            if (confirm('No saved payment methods. Would you like to add one?')) {
                window.location.href = 'user.html';
            }
            return;
        }

        showCardSelection(eventId, cardsData.cards);
    } catch (error) {
        console.error('error :', error);
       
    }
}

//kleinei to parathyro leptomereion mias ekdilosis
function hideDetails() {
    const modal = document.getElementById('showDetails');
    modal.classList.add('fade-out');
    document.body.style.overflow = 'auto';
    setTimeout(() => {
        modal.style.display = 'none';
        modal.classList.remove('fade-out');
        selectedTicketInfo = null;
    }, 300);
}


//emfanizei tis epiloges pliromis gia tin kratisi
function showCardSelection(eventId, cards) {
    const modal = document.getElementById('showDetails');
    const modalContent = modal.querySelector('.modal-content');
    
    if (!modalContent) return;

    const cardSelectionHtml = `
        <button onclick="hideCardSelection()" class="close-modal">&times;</button>
        <div class="event-header">
            <h2>Select Payment Method</h2>
        </div>
        <div class="payment-section">
            <div class="saved-cards">
                ${cards.map(card => `
                    <div class="saved-card" onclick="completeBooking(${eventId}, ${card.id}, ${selectedTicketInfo.typeId})">
                        <p class="card-number">**** **** **** ${card.cardNumber.slice(-4)}</p>
                        <p class="card-expiry">Expires: ${card.expiry}</p>
                    </div>
                `).join('')}
            </div>
            <button onclick="hideCardSelection()" class="book-button">
                Back to Tickets
            </button>
        </div>
    `;

    modalContent.innerHTML = cardSelectionHtml;
}

//krivei tis epiloges pliromis kai epistrofis stin othoni eisitirion
function hideCardSelection() {
    const urlParams = new URLSearchParams(window.location.search);
    const eventId = urlParams.get('eventId');
    
    if (eventId) {
        showDetails(eventId);
    } else {
        const modal = document.getElementById('showDetails');
        if (modal) {
            modal.style.display = 'none';
            document.body.style.overflow = 'auto';
        }
    }
}


//oloklironi tin kratisi eisitiriou me tin epilegmeni carta
async function completeBooking(eventId, cardId, ticketTypeId) {
    try {
        const response = await fetch('/hy360-project-2024/api/bookings', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                eventId: eventId,
                cardId: cardId,
                ticketTypeId: ticketTypeId,
                quantity: 1
            })
        });

        const data = await response.json();
        
        if (data.success) {
            alert('Booking successful!');
            hideCardSelection();
            loadEvents(); 
        } else {
            showMessage(data.message || 'Booking failed', true);
        }
    } catch (error) {
        console.error('error:', error);
        showMessage('Failed to complete booking', true);
    }
}


//emfanizei minimata epitixias i lathous ston xristi
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


