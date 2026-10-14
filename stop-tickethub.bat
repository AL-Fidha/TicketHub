@echo off
title Stop TicketHub Server
echo [*] Terminating any running TicketHub backend servers on ports 8081 and 8080...
powershell -Command "Get-NetTCPConnection -LocalPort 8081,8080 -ErrorAction SilentlyContinue | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }"
echo [*] TicketHub server stopped successfully.
timeout /t 3
