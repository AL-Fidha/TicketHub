# 🎬 TicketHub — Kerala Cinema Ticket Booking System

> **College of Engineering, Muttathara, Thiruvananthapuram — 2026**  
> Department of Computer Science and Engineering  
> **Team:** Abhinav S · Abhirami M K · Adhithya R M · AL-Fidha S R

---

## 📌 Overview

**TicketHub** is a production-ready, full-stack online movie ticket reservation platform designed specifically for multiplexes and screens across Kerala. It combines an architectural dark-luxury frontend with an enterprise Java Spring Boot backend, MySQL database, JWT authentication, and The Movie Database (TMDB) integration.

---

## ✨ Key Features

- **Kerala Multiplex Concierge**: Screenings across 20 state-of-the-art multiplexes across Kochi, Thiruvananthapuram, Kozhikode, Thrissur, Kollam, Palakkad, Kottayam, Malappuram, Alappuzha, and Kasaragod.
- **Architectural UI & Theme**: Embroidered gold ticket badge branding, 3D animated hero showcase, moving 35mm film reel ribbon, and responsive 4-column movie catalogue.
- **Interactive Seat Selection & Real-Time Collision Locking**: Premium recliners (Rows A–B) and Standard seats (Rows C–H) with instant validation to prevent double-booking.
- **Time-Aware Booking Protection**: Automatically displays all daily scheduled showtimes, while clearly disabling and marking past screenings (`Show Ended`) to prevent invalid reservations.
- **Odyssey-Style Admit-One Tickets**: Perforated digital cinema passes with scannable barcodes, auditorium details, sound profiles (Dolby Atmos 7.1, 4K RGB Laser), and printable format.
- **Admin Management Operations**: Dedicated administrative control center allowing staff to schedule shows, broadcast showtimes to all theatres, edit movie profiles, delete movies, and discover/import live titles from TMDB.
- **Customer CineClub Loyalty Tier**: Dynamic loyalty points accumulation (Silver Film Buff, Gold Cinephile, Platinum Premiere Member) with profile preferences.

---

## 🗂️ Project Architecture & File Structure

```text
TicketHub/
│
├── src/main/java/com/tickethub/          ← ENTERPRISE JAVA BACKEND (Spring Boot)
│   ├── TicketHubApplication.java         ← Main entry point & database seeder
│   ├── controller/                       ← REST API Controllers
│   │   ├── AdminController.java          ← Admin stats, lineup, shows, TMDB import
│   │   ├── AuthController.java           ← Registration, login, profile, password
│   │   ├── BookingController.java        ← Ticket booking, history, cancellations
│   │   ├── MovieController.java          ← Movie catalogue CRUD & delete endpoint
│   │   ├── SeatController.java           ← Live seat map & reservation checks
│   │   ├── ShowController.java           ← Showtimes query endpoints
│   │   └── TheatreController.java        ← Kerala theatres and screens
│   ├── dto/                              ← Data Transfer Objects & validation requests
│   ├── exception/                        ← Global REST exception handler
│   ├── model/                            ← JPA Entity models (User, Movie, Theatre, Screen, Show, Booking)
│   ├── repository/                       ← Spring Data JPA interfaces
│   ├── security/                         ← JWT Authentication filter & Spring Security config
│   └── service/                          ← Business logic (Admin, Auth, Booking, Movie, Seat, TMDB)
│
├── src/main/resources/
│   ├── application.properties            ← Active MySQL, JWT, server, & TMDB configuration
│   ├── application.properties.example    ← Configuration template for new environments
│   ├── application-railway.properties    ← Cloud deployment configuration profile
│   └── static/                           ← AUTHENTIC WEB CLIENT (Served by Spring Boot)
│       ├── index.html                    ← Homepage with animated hero & movie catalogue
│       ├── movies.html                   ← Movie catalogue with genre/language filters
│       ├── booking.html                  ← 4-step ticket booking & live seat selection
│       ├── theatres.html                 ← Kerala multiplexes, city tabs & screen details
│       ├── dashboard.html                ← Customer profile, bookings, loyalty & admin panel
│       ├── admin.html                    ← Full-screen dedicated admin operations dashboard
│       ├── login.html                    ← Sign In & Customer Registration portal
│       ├── admin-login.html              ← Dedicated administrator authentication portal
│       ├── css/styles.css                ← Unified styling, animations & responsive layout
│       ├── js/api.js                     ← Centralized API client & JWT token manager
│       └── assets/                       ← Embroidered badge logos and graphic assets
│
├── database/
│   ├── schema.sql                        ← MySQL schema DDL reference
│   └── migrations/                       ← Database migration scripts
│
├── pom.xml                               ← Maven build dependencies and plugins
├── Dockerfile                            ← Containerization for deployment
└── README.md                             ← Project documentation
```

---

## 🛠️ Technology Stack

- **Backend**: Java 17, Spring Boot 3.2.5, Spring Security, Spring Data JPA, Hibernate, JJWT (JSON Web Token)
- **Frontend**: Modern Vanilla JavaScript (ES6+), CSS3 (Flexbox/Grid, 3D Transforms, Keyframes), HTML5
- **Database**: MySQL 8.0+
- **Build & Dependency Management**: Apache Maven 3.9+
- **External APIs**: The Movie Database (TMDB) API v3 for high-resolution movie artwork

---

## 🚀 Getting Started

### Prerequisites

1. **Java 17 (LTS)**: Ensure Java 17 is installed (`java -version`).
2. **Apache Maven**: Ensure Maven is installed and on your PATH (`mvn -version`).
3. **MySQL Server**: Running on `localhost:3306` with database `tickethub`.

### 1. Database Setup

Create the MySQL database:
```sql
CREATE DATABASE IF NOT EXISTS tickethub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Configuration

Create or verify `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tickethub?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

jwt.secret=9a7f8e6d5c4b3a2f1e0d9c8b7a6f5e4d3c2b1a0f9e8d7c6b5a4f3e2d1c0b9a8
tmdb.api.key=YOUR_TMDB_API_KEY_OPTIONAL
app.admin.email=admin@tickethub.com
app.admin.password=admin123
```

### 3. Run the Application

Execute via Maven:
```powershell
mvn spring-boot:run
```

To run on port 8081 if port 8080 is occupied:
```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

Access the platform:
- **Customer Portal**: [http://localhost:8081](http://localhost:8081)
- **Admin Portal**: [http://localhost:8081/admin-login.html](http://localhost:8081/admin-login.html) (Default: `admin@tickethub.com` / `admin123`)

---

## 📡 REST API Documentation

| Method | Endpoint | Access | Description |
|:-------|:---------|:-------|:------------|
| `POST` | `/api/auth/register` | Public | Register customer account |
| `POST` | `/api/auth/login` | Public | Authenticate user & retrieve JWT token |
| `GET` | `/api/auth/me` | Customer / Admin | Retrieve current authenticated session info |
| `PUT` | `/api/auth/profile` | Customer / Admin | Update profile name and phone number |
| `PUT` | `/api/auth/password` | Customer / Admin | Change user password |
| `GET` | `/api/movies` | Public | Browse movie catalogue with filters |
| `GET` | `/api/movies/{id}` | Public | Retrieve single movie details |
| `GET` | `/api/movies/{id}/shows` | Public | Query showtimes (filtered by date, theatre) |
| `POST` | `/api/movies` | 🔒 Admin | Add new movie to catalogue |
| `PUT` | `/api/movies/{id}` | 🔒 Admin | Update existing movie details |
| `DELETE`| `/api/movies/{id}` | 🔒 Admin | Delete movie from catalogue |
| `GET` | `/api/theatres` | Public | List all 20 Kerala multiplexes |
| `GET` | `/api/theatres/{id}/shows`| Public | Retrieve all movie shows at a theatre |
| `GET` | `/api/seats/{show_id}` | Public | Live seat map with booked seat states |
| `POST` | `/api/bookings` | 🔒 Authenticated | Create booking & lock seats |
| `GET` | `/api/bookings/mine` | 🔒 Customer | Fetch personal booking history |
| `GET` | `/api/bookings/{ref}` | 🔒 Authenticated | Retrieve booking by reference code |
| `PUT` | `/api/bookings/{ref}/cancel`| 🔒 Authenticated | Cancel a confirmed booking |
| `GET` | `/api/admin/stats` | 🔒 Admin | Platform analytics (bookings, revenue, users) |
| `GET` | `/api/admin/users` | 🔒 Admin | List all registered accounts |
| `GET` | `/api/admin/theatres` | 🔒 Admin | Manage theatre records |
| `POST` | `/api/admin/theatres` | 🔒 Admin | Register new theatre & screen layout |
| `GET` | `/api/admin/screens` | 🔒 Admin | View screens for scheduling |
| `GET` | `/api/admin/shows` | 🔒 Admin | List all scheduled screenings |
| `POST` | `/api/admin/shows` | 🔒 Admin | Schedule individual show |
| `POST` | `/api/admin/shows/broadcast`| 🔒 Admin | Broadcast movie showtimes across all 20 theatres |
| `DELETE`| `/api/admin/shows/{id}` | 🔒 Admin | Remove show without active booking history |
| `GET` | `/api/admin/tmdb/movies` | 🔒 Admin | Discover live language-specific releases via TMDB |
| `POST` | `/api/admin/tmdb/movies/{id}`| 🔒 Admin | Import TMDB release directly into database |

---

## 📄 License & Attribution

Developed for academic and project demonstration purposes at the **College of Engineering, Muttathara (2026)**.  
All trademarked logos and cinema identities belong to their respective copyright holders.
