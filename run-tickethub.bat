@echo off
title TicketHub Cinema Server (Port 8081)
color 0A
cd /d "%~dp0"

echo ==========================================================
echo     TICKETHUB -- KERALA CINEMA RESERVATION PLATFORM
echo ==========================================================
echo.
echo [*] Launching Spring Boot Backend on Port 8081...
echo [*] Database: MySQL localhost:3306 (tickethub)
echo [*] Once initialized, TicketHub will open automatically in your browser.
echo.
echo [*] NOTE: Leave this window open while using the website.
echo [*] Closing this window will stop the backend server.
echo ==========================================================
echo.

:: Automatically open browser after a brief delay for server initialization
start "" powershell -Command "Start-Sleep -Seconds 7; Start-Process 'http://localhost:8081'"

:: Run Spring Boot server
mvn spring-boot:run

pause
