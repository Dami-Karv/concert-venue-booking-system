//arxikopoiisi selidas kai fortosi dedomenon xristi
document.addEventListener('DOMContentLoaded', async () => {
    document.getElementById('userName').textContent = 'Loading...';
    document.getElementById('bookingsList').innerHTML = '<p>Loading your bookings...</p>';

    const cardForm = document.getElementById('cardForm');
    if (cardForm) {
        cardForm.style.display = 'none';
        cardForm.addEventListener('submit', handleCardSubmission);
    }

    const addCardBtn = document.getElementById('addCardBtn');
    if (addCardBtn) {
        addCardBtn.addEventListener('click', toggleCardForm);
    }

    try {
        await Promise.all([
            fetchUserProfile(),
            loadUserBookings(),
            fetchSavedCards()
        ]);

        const loadingElements = document.querySelectorAll('.loading');
        loadingElements.forEach(el => el.remove());
    } catch (error) {
        console.error('error:', error);
        showMessage('Failed to load some user data. Please refresh the page.', true);
    }
});


//anaktisi kai emfanisi stoixeion profil xristi
async function fetchUserProfile() {
    try {
        const response = await fetch('/hy360-project-2024/api/user/profile');
        const data = await response.json();

        if (data.success) {
            document.getElementById('userName').textContent = `${data.user.firstName} ${data.user.lastName}`;
            document.getElementById('userEmail').textContent = data.user.email;
        }
    } catch (error) {
        document.getElementById('userName').textContent = 'Error loading profile';
    }
}

//enimeronei ta prosopika stoixeia tou xristi
async function handleProfileUpdate(event) {
    event.preventDefault();
    
    const firstName = document.getElementById('firstName').value;
    const lastName = document.getElementById('lastName').value;

    try {
        const response = await fetch('/hy360-project-2024/api/user/profile', {
            method: 'POST',  
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                firstName: firstName,
                lastName: lastName
            })
        });

        const data = await response.json();
        
        if (data.success) {
            showMessage('Profile updated successfully');
            fetchUserProfile(); 
        } else {
            showMessage(data.message || 'Failed to update profile', true);
        }
    } catch (error) {
        console.error('error:', error);
        showMessage('Failed to update profile', true);
    }
}

//fortosi kai emfanisi olon ton kratiseon tou xristi
async function loadUserBookings() {
    const bookingsList = document.getElementById('bookingsList');
    if (!bookingsList) return;

    try {
        const response = await fetch('/hy360-project-2024/api/bookings');
        const data = await response.json();

        if (data?.bookings?.length) {
            renderBookings(data, bookingsList);
        } else {
            bookingsList.innerHTML = '<p>No bookings found.</p>';
        }
    } catch (error) {
        bookingsList.innerHTML = '<p>Error loading bookings. Please refresh.</p>';
    }
}

//dimiourgei tin emfanisi ton kratiseon tou xristi
function renderBookings(data, container) {
    if (!data?.bookings?.length) {
        container.innerHTML = '<p class="no-bookings">No bookings found.</p>';
        return;
    }
    const uniqueBookings = new Map();
    
    data.bookings.forEach(booking => {
        uniqueBookings.set(booking.booking_id, booking);
    });

    container.innerHTML = Array.from(uniqueBookings.values()).map(booking => `
        <div class="booking-item">
            <div class="booking-header">
                <h3>${booking.event_name || 'Unknown Event'}</h3>
                <span class="booking-status ${booking.status}">
                    ${booking.status || 'Unknown'}
                </span>
            </div>
            <div class="booking-details">
                <p>Ticket Type: ${booking.ticket_type || 'N/A'}</p>
                <p>Price: €${(booking.price || 0).toFixed(2)}</p>
                <p>Booked on: ${booking.booking_date ? 
                    new Date(booking.booking_date).toLocaleDateString() : 'N/A'}</p>
            </div>
            ${booking.status === 'confirmed' ? 
                `<button onclick="cancelBooking(${booking.booking_id})" 
                         class="cancel-booking-btn">
                    Cancel Booking
                </button>` : 
                ''}
        </div>
    `).join('');
}


//akyrosi kratisis kai enimerosh tis listas
async function cancelBooking(bookingId) {
    if (!confirm('Are you sure you want to cancel this booking? This action cannot be undone.')) {
        return;
    }

    try {
        const response = await fetch('/hy360-project-2024/api/bookings/cancel', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `bookingId=${bookingId}`
        });

        const data = await response.json();
        
        if (data.success) {
            showMessage('Booking cancelled successfully');
            loadUserBookings();
        } else {
            showMessage(data.message || 'Failed to cancel booking', true);
        }
    } catch (error) {
        console.error('error', error);
        showMessage('Failed to cancel booking', true);
    }
}

//anaktisi kai emfanisi apothikevmenon karton
async function fetchSavedCards() {
    try {
        const response = await fetch('/hy360-project-2024/api/user/cards');
        const data = await response.json();
        if (data.success) {
            const cardList = document.getElementById('cardList');
            cardList.innerHTML = '';
            data.cards.forEach(card => cardList.appendChild(createCardElement(card)));
        }
    } catch (error) {
        showMessage('Failed to load saved cards', true);
    }
}

//dimiourgei to stoixeio emfanisis mias kartas
function createCardElement(card) {
    const cardElement = document.createElement('div');
    cardElement.className = 'card';
    cardElement.innerHTML = `
        <h3>${card.cardName}</h3>
        <p>**** **** **** ${card.cardNumber.slice(-4)}</p>
        <p>Expires: ${card.expiry}</p>
        <button onclick="deleteCard(${card.id})" class="delete-btn">Delete</button>
    `;
    return cardElement;
}

//enallagi emfanisis tis formas prosthikis neas kartas
function toggleCardForm() {
    const cardForm = document.getElementById('cardForm');
    const addCardBtn = document.getElementById('addCardBtn');

    if (cardForm.style.display === 'none') {
        cardForm.style.display = 'block';
        addCardBtn.textContent = 'Cancel';
    } else {
        cardForm.style.display = 'none';
        addCardBtn.textContent = 'Add Card';
        cardForm.reset();
    }
}

//epeksergasia kai apothikeusi neas kartas
async function handleCardSubmission(event) {
    event.preventDefault();

    const formData = new FormData(event.target);
    const cardData = new URLSearchParams();
    for (let [key, value] of formData.entries()) {
        cardData.append(key, value);
    }

    try {
        const response = await fetch('/hy360-project-2024/api/user/cards', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: cardData.toString()
        });

        const data = await response.json();
        
        if (data.success) {
            showMessage('Card added successfully');
            toggleCardForm();
            fetchSavedCards();
        } else {
            showMessage(data.message, true);
        }
    } catch (error) {
        console.error('error:', error);
        showMessage('Failed to save card', true);
    }
}

//diagrafi epilegmenis kartas
async function deleteCard(cardId) {
    if (!confirm('Are you sure you want to delete this card?')) {
        return;
    }

    try {
        const response = await fetch(`/hy360-project-2024/api/user/cards/${cardId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        const data = await response.json();
        
        if (data.success) {
            showMessage('Card deleted successfully');
            await fetchSavedCards();
        } else {
            showMessage(data.message || 'Failed to delete card', true);
        }
    } catch (error) {
        console.error('error:', error);
        showMessage('Failed to delete card: ' + error.message, true);
    }
}


//apothikeusi tou xristi kai epistrofi stin arxiki selida
async function handleLogout() {
    try {
        await fetch('/hy360-project-2024/api/logout', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'}
        });
    } finally {
        window.location.href = 'index.html';
    }
}

//emfanizei prosorina minimata epitixias i sfalmatos
function showMessage(message, isError = false) {
    const messageContainer = document.getElementById('messageContainer');
    if (!messageContainer) return;

    const messageElement = document.createElement('div');
    messageElement.className = isError ? 'error-message' : 'success-message';
    messageElement.textContent = message;
    messageContainer.innerHTML = '';
    messageContainer.appendChild(messageElement);
    setTimeout(() => messageElement.remove(), 3000);
}