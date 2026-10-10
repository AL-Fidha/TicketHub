-- ============================================================
--  TICKET HUB — MySQL Schema
--  Run ONLY if you prefer manual DB setup over Spring auto-ddl.
--  Spring Boot will auto-create tables on first run.
--  Use this file for reference or manual setup.
-- ============================================================

CREATE DATABASE IF NOT EXISTS tickethub
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE tickethub;

CREATE TABLE IF NOT EXISTS users (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  name       VARCHAR(120)  NOT NULL,
  email      VARCHAR(180)  NOT NULL UNIQUE,
  phone      VARCHAR(20),
  password   VARCHAR(255)  NOT NULL,
  role       ENUM('customer','admin') NOT NULL DEFAULT 'customer',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS movies (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  title        VARCHAR(200) NOT NULL,
  genre        VARCHAR(120) NOT NULL,
  language     ENUM('Malayalam','English','Tamil','Hindi','Telugu') NOT NULL,
  duration_min INT          NOT NULL,
  rating       DECIMAL(3,1) DEFAULT 0.0,
  description  TEXT,
  tmdb_id      BIGINT,
  poster_path  VARCHAR(255),
  status       ENUM('NOW_SHOWING','NEW','COMING_SOON') DEFAULT 'NOW_SHOWING',
  created_at   DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS theatres (
  id       BIGINT AUTO_INCREMENT PRIMARY KEY,
  name     VARCHAR(150) NOT NULL,
  location VARCHAR(250) NOT NULL,
  city     VARCHAR(80)  NOT NULL
);

CREATE TABLE IF NOT EXISTS screens (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  theatre_id  BIGINT  NOT NULL,
  name        VARCHAR(50) NOT NULL,
  total_seats INT NOT NULL DEFAULT 80,
  rows        INT NOT NULL DEFAULT 8,
  cols        INT NOT NULL DEFAULT 10,
  FOREIGN KEY (theatre_id) REFERENCES theatres(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS shows (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  movie_id   BIGINT NOT NULL,
  screen_id  BIGINT NOT NULL,
  show_date  DATE   NOT NULL,
  show_time  TIME   NOT NULL,
  price_std  DECIMAL(8,2) NOT NULL DEFAULT 220.00,
  price_prem DECIMAL(8,2) NOT NULL DEFAULT 380.00,
  FOREIGN KEY (movie_id)  REFERENCES movies(id)  ON DELETE CASCADE,
  FOREIGN KEY (screen_id) REFERENCES screens(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bookings (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  booking_ref  VARCHAR(25)   NOT NULL UNIQUE,
  user_id      BIGINT        NOT NULL,
  show_id      BIGINT        NOT NULL,
  total_amount DECIMAL(10,2) NOT NULL,
  status       ENUM('Confirmed','Cancelled','Pending') DEFAULT 'Confirmed',
  booked_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)  ON DELETE CASCADE,
  FOREIGN KEY (show_id) REFERENCES shows(id)  ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS booking_seats (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  booking_id BIGINT      NOT NULL,
  show_id    BIGINT      NOT NULL,
  seat_label VARCHAR(10) NOT NULL,
  seat_type  ENUM('standard','premium') NOT NULL DEFAULT 'standard',
  price      DECIMAL(8,2) NOT NULL,
  FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
  FOREIGN KEY (show_id)    REFERENCES shows(id)    ON DELETE CASCADE,
  UNIQUE KEY uq_show_seat (show_id, seat_label)
);
