//dimiourgei kai emfanizei ti forma syndesis tou xristi
function showLoginForm() {
    const formContainer = document.getElementById('formContainer');
    formContainer.innerHTML = `
        <form id="loginForm" onsubmit="handleLogin(event)">
            <div>
                <label for="email">Email:</label>
                <input type="email" id="email" required>
            </div>
            
            <div>
                <label for="password">Password:</label>
                <input type="password" id="password" required>
            </div>

            <button type="submit">Login</button>
        </form>
    `;
}


//dimiourgei kai emfanizei ti forma eggrafis neou xristi
function showRegisterForm() {
    const formContainer = document.getElementById('formContainer');
    formContainer.innerHTML = `
        <form id="registerForm" onsubmit="handleRegister(event)">
            <div>
                <label for="password">Password:</label>
                <input type="password" id="password" required>
            </div>
            
            <div>
                <label for="confirmPassword">Confirm Password:</label>
                <input type="password" id="confirmPassword" required>
            </div>

            <div>
                <label for="firstName">First Name:</label>
                <input type="text" id="firstName" required>
            </div>

            <div>
                <label for="lastName">Last Name:</label>
                <input type="text" id="lastName" required>
            </div>

            <div>
                <label for="email">Email:</label>
                <input type="email" id="email" required>
            </div>

            <button type="submit">Register</button>
        </form>
    `;
}


//epeksergasia kai apostoli dedomenon syndesis ston server
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


//epeksergasia kai apostoli dedomenon eggrafis ston server
async function handleRegister(event) {
    event.preventDefault();
    
    const firstName = document.getElementById('firstName').value;
    const lastName = document.getElementById('lastName').value;
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (password !== confirmPassword) {
        alert('Passwords do not match!');
        return;
    }

    try {
        const response = await fetch('/api/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `firstName=${encodeURIComponent(firstName)}&lastName=${encodeURIComponent(lastName)}&email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}`
        });

        const data = await response.json();
        
        if (data.success) {
            alert('Registration successful! Please login.');
            showLoginForm();
        } else {
            alert(data.message);
        }
    } catch (error) {
        alert('Error during registration. Please try again.');
    }
}

//arxikopoiisi tis selidas me ti forma syndesis
document.addEventListener('DOMContentLoaded', () => {
    showLoginForm();
});