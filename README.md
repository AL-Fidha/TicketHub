# 🎬 Ticket Hub — Movie Ticket Booking System

> **College of Engineering, Muttathara, Thiruvananthapuram — 2026**  
> Department of Computer Science and Engineering  
> **Team:** Abhinav S · Abhirami M K · Adhithya R M · AL-Fidha S R

---

## 🗂️ Project Structure

```
TicketHub/
│
├── src/main/java/com/tickethub/     ← JAVA BACKEND (Spring Boot)
│   ├── TicketHubApplication.java    ← Main entry point + data seeder
│   ├── model/                       ← JPA Entity classes
│   ├── repository/                  ← Spring Data JPA interfaces
│   ├── service/                     ← Business logic
│   ├── controller/                  ← REST API endpoints
│   ├── security/                    ← JWT auth
│   └── dto/                         ← Request/Response objects
│
├── src/main/resources/
│   ├── application.properties       ← MySQL + JWT + TMDB config
│   └── static/                      ← FRONTEND (HTML/CSS/JS)
│       ├── index.html
│       ├── movies.html
│       ├── booking.html
│       ├── login.html
│       ├── dashboard.html
│       ├── css/styles.css
│       └── js/api.js
│
├── database/
│   └── schema.sql                   ← MySQL schema reference
│
├── frontend/                        ← Frontend source (mirrors static/)
├── pom.xml                          ← Maven dependencies
├── .gitignore
└── README.md
```


For the Java/MySQL application, `src/main/java` and `src/main/resources/static` are authoritative. The root-level pages, `frontend/`, and `backend/` are older parallel prototypes and are not served by Spring Boot. Run the Maven project from this folder.
---

## ⚙️ Step 1 — Install Java 17

1. Go to: https://adoptium.net/
2. Download **Temurin 17 (LTS)**
3. Run the installer — tick **"Set JAVA_HOME"** and **"Add to PATH"**
4. Verify:
   ```powershell
   java -version
   # Should show: openjdk version "17.x.x"
   ```

---

## ⚙️ Step 2 — Install Maven

1. Go to: https://maven.apache.org/download.cgi
2. Download **Binary zip archive** (e.g. `apache-maven-3.9.6-bin.zip`)
3. Extract to `C:\Program Files\Maven\`
4. Add to PATH:
   - Search **"Environment Variables"** in Windows search
   - Under **System Variables** → find `Path` → click Edit → New
   - Add: `C:\Program Files\Maven\apache-maven-3.9.6\bin`
5. Verify:
   ```powershell
   mvn -version
   # Should show: Apache Maven 3.9.x
   ```

---

## ⚙️ Step 3 — Install MySQL

1. Go to: https://dev.mysql.com/downloads/installer/
2. Download **MySQL Installer (Windows)**
3. Run → choose **"Developer Default"**
4. Set a **root password** — remember it!
5. Finish installation
6. Verify:
   ```powershell
   mysql --version
   ```

---

## ⚙️ Step 4 — Get TMDB API Key (free)

1. Sign up at: https://www.themoviedb.org/signup
2. Go to: https://www.themoviedb.org/settings/api
3. Click **Create** → Developer → fill the form
4. Copy your **API Key (v3 auth)**

---

## ⚙️ Step 5 — Configure the app

Create your private local configuration from the safe template:

```powershell
if (!(Test-Path src/main/resources/application.properties)) {
   Copy-Item src/main/resources/application.properties.example src/main/resources/application.properties
}
```

Set your MySQL password, a random JWT secret of at least 32 characters, administrator email/password, and optionally your TMDB API key in the local `application.properties`. That file is ignored by Git; never commit or share credentials.

---

## 🚀 Step 6 — Run the app

Open VS Code, open the **TicketHub** folder, then in the terminal:

```powershell
mvn spring-boot:run
```

If port 8080 is already in use, run on 8081 instead:

```powershell
mvn spring-boot:run '-Dspring-boot.run.arguments=--server.port=8081'
```

First run will:
- Auto-create the `tickethub` database
- Create all tables
- Seed the initial movie and theatre catalogue and sample showtimes only when those tables are empty
- Create the administrator only when `app.admin.email` and `app.admin.password` are configured

Then open the matching local URL, usually **http://localhost:8080** or **http://localhost:8081**.

---

## 🌐 Publish a Public Website (Railway)

People visiting the deployed site only need a browser. Railway builds and runs the Java container; visitors do not install Java, Maven, or MySQL.

1. Review the files to be published and confirm `src/main/resources/application.properties` and any `.env` file are not staged:
   ```powershell
   git status --short
   git check-ignore -v src/main/resources/application.properties
   ```
2. Commit and push the deployment files to this repository's `main` branch:
   ```powershell
   git add .gitignore .dockerignore Dockerfile railway.toml src/main/resources/application-railway.properties src/main/java/com/tickethub/TicketHubApplication.java README.md
   git status --short
   git commit -m "Prepare TicketHub for hosted deployment"
   git push origin main
   ```
3. Sign in to Railway, create a project from the `AL-Fidha/TicketHub` GitHub repository, and add a MySQL service to the project.
4. In the TicketHub service's Variables, set `SPRING_CONFIG_NAME=application-railway`. Add Railway's MySQL service references for `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, and `MYSQLPASSWORD` using the variable picker.
5. Add `JWT_SECRET` (at least 32 random characters), `APP_ADMIN_EMAIL`, `APP_ADMIN_PASSWORD`, and `TMDB_API_KEY` as private Railway variables. Never put these values in GitHub or in frontend code.
6. Deploy the service. Railway builds the Docker image and starts the JAR. Once healthy, open the service's Networking settings and choose **Generate Domain**. Share that HTTPS domain with visitors.

The Railway profile does not create sample movies or unverified theatre records. After deployment, sign in with the configured admin account, import movies from TMDB, and add verified theatre addresses and screens before scheduling shows. Hosting providers may charge for the app and database; check Railway's current pricing before deploying.

---

## 🔑 Login

Register a customer account using **Sign Up**. To provision an administrator, set `app.admin.email` and `app.admin.password` in your local configuration before starting the app. The application does not create demo accounts or ship with a default administrator password. Existing administrators are not overwritten on restart.

---

## 📦 Build as a single JAR

```powershell
mvn package -DskipTests
java -jar target/tickethub-1.0.0.jar
```

Share the `.jar` file — anyone with Java installed can run it.

---

## 🐙 Push to GitHub

```powershell
# In the TicketHub folder:
git init
git add .
git commit -m "Initial commit: Ticket Hub Spring Boot app"

# Create repo on github.com, then:
git remote add origin https://github.com/YOUR_USERNAME/TicketHub.git
git branch -M main
git push -u origin main
```

> ⚠️ `application.properties` is in `.gitignore` — never commit or share real passwords or API keys. Each developer should create their own local configuration from the example template.

---

## 📡 REST API Reference

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | — | Register customer |
| POST | `/api/auth/login` | — | Login, returns JWT |
| GET | `/api/auth/me` | ✅ | Current user info |
| PUT | `/api/auth/profile` | ✅ | Update name/phone |
| PUT | `/api/auth/password` | ✅ | Change password |
| GET | `/api/movies` | — | All movies |
| GET | `/api/movies/{id}` | — | Single movie |
| GET | `/api/movies/{id}/shows` | — | Shows (filter: date, theatre_id) |
| POST | `/api/movies` | 🔒 Admin | Add movie |
| PUT | `/api/movies/{id}` | 🔒 Admin | Edit movie |
| DELETE | `/api/movies/{id}` | 🔒 Admin | Delete movie |
| GET | `/api/theatres` | — | All Kerala theatres |
| GET | `/api/theatres/cities` | — | List cities |
| GET | `/api/seats/{show_id}` | — | Live seat map |
| POST | `/api/bookings` | ✅ | Book tickets |
| GET | `/api/bookings/mine` | ✅ | My bookings |
| GET | `/api/bookings/{ref}` | ✅ | Single booking |
| PUT | `/api/bookings/{ref}/cancel` | ✅ | Cancel booking |
| GET | `/api/bookings` | 🔒 Admin | All bookings |
| GET | `/api/admin/stats` | 🔒 Admin | Dashboard stats |
| GET | `/api/admin/users` | 🔒 Admin | All users |
| GET | `/api/admin/theatres` | 🔒 Admin | Theatre records |
| POST | `/api/admin/theatres` | 🔒 Admin | Add a theatre and first screen |
| GET | `/api/admin/screens` | 🔒 Admin | Screens for show scheduling |
| GET | `/api/admin/shows` | 🔒 Admin | All showtimes |
| POST | `/api/admin/shows` | 🔒 Admin | Schedule a showtime |
| DELETE | `/api/admin/shows/{id}` | 🔒 Admin | Remove a show without booking history |
| GET | `/api/admin/tmdb/movies?language=Malayalam` | 🔒 Admin | Discover recent/upcoming TMDB releases |
| POST | `/api/admin/tmdb/movies/{tmdbId}?language=Malayalam` | 🔒 Admin | Import a TMDB movie and poster path |

---

## 🛠️ VS Code Extensions to Install

Search in Extensions panel (`Ctrl+Shift+X`):

- **Extension Pack for Java** (Microsoft) — Java language support
- **Spring Boot Extension Pack** (VMware) — run Spring apps
- **MySQL** (Jun Han) — browse database in VS Code
- **GitLens** — git history viewer

---

## 🎭 Features

- ✅ Java Spring Boot backend (genuine Java project)
- ✅ MySQL database with JPA
- ✅ JWT authentication (BCrypt passwords)
- ✅ TMDB poster lookup for movies missing artwork (requires internet and an API key)
- ✅ Kerala theatre records stored in MySQL; verify venue names and addresses before use
- ✅ Live seat map — see booked seats across all users
- ✅ Duplicate booking prevention (database unique constraint)
- ✅ Admin panel — manage movies, theatres and showtimes; view all bookings and revenue stats
- ✅ Import current/upcoming language-filtered movie listings from TMDB
- ✅ Printable tickets with unique booking reference
- ✅ Seed catalogue spans Malayalam, English, Tamil, and Hindi
- ✅ Single JAR deployment
