# TicketHub

A movie ticket booking system built with Java Spring Boot and MySQL.

## Overview

TicketHub is a full-stack web application for browsing movies, viewing showtimes, booking tickets, and managing bookings for both customers and administrators.

## Features

- Movie listings and showtime browsing
- User registration and login
- Booking flow with seat selection
- Admin dashboard for managing shows and theatres
- MySQL-backed persistence
- Java backend with REST API endpoints

## Tech Stack

- Java 17
- Spring Boot
- Maven
- MySQL
- HTML, CSS, JavaScript

## Project Structure

```text
TicketHub/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
├── database/
├── pom.xml
├── .gitignore
├── README.md
└── .env.example (if present)
```

## Getting Started

### Prerequisites

- Java 17+
- Maven
- MySQL

### Setup

1. Clone the repository.
2. Configure your database and app settings in `src/main/resources/application.properties`.
3. Run the application:

```bash
mvn spring-boot:run
```

4. Open the app in your browser:

```text
http://localhost:8080
```

## Notes

- Keep sensitive credentials such as database passwords and JWT secrets out of version control.
- If this project includes a sample configuration file, copy it and customize it before running the app.

## License

This project is currently unlicensed unless explicitly added later.
