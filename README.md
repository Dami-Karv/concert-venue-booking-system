# Concert Venue Booking System

A comprehensive event booking and ticketing platform that allows users to browse events, purchase tickets, and manage their bookings. The system includes admin features for event management, ticket type creation, and financial reporting.

## Features

- User registration and authentication
- Event browsing and ticket purchasing
- Multiple ticket types with availability tracking
- Secure payment processing
- Booking management (view, cancel)
- Automatic refunds for cancelled bookings
- Admin dashboard with financial statistics

## Tech Stack

- Java Servlets
- MySQL Database
- Maven
- XAMPP (Apache + MySQL)
- Bootstrap for frontend

## Database Structure

The system is built around the following core entities:
- Events
- Customers
- Ticket Types
- Bookings
- Payments
- Credit Cards

## Quick Start Guide

### Prerequisites

- XAMPP (with MySQL 8.0.0)
- JDK 8 or higher
- Maven 3.8 or higher

### Installation

1. Clone the repository
   ```
   git clone https://github.com/yourusername/concert-venue-booking-system.git
   cd concert-venue-booking-system
   ```

2. Start XAMPP (as administrator)
   - Start both Apache and MySQL services

3. Set up the database
   - Open phpMyAdmin (http://localhost/phpmyadmin)
   - Create a new database named `concert_venue_db`
   - Import the SQL file from the `database` folder

4. Build the project
   ```
   mvn clean package
   ```

5. Deploy the WAR file
   - Copy the generated WAR file from the `target` folder to your Tomcat webapps directory
   - Alternatively, deploy through the Tomcat web interface

6. Access the application
   - Open your browser and navigate to http://localhost:8080/concert-venue-booking

### Default Credentials

- Admin:
  - Username: admin
  - Password: admin123

- Test Customer:
  - Username: user1@gmail.com
  - Password: password123

## Project Structure

- `src/main/java` - Java source code
- `src/main/webapp` - Web resources
- `database` - SQL database schema and sample data
