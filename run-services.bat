@echo off
echo Starting Inventory Service on port 8081...
start "Inventory Service (8081)" cmd /k "cd /d %~dp0inventory-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

echo Starting Payment Service on port 8082...
start "Payment Service (8082)" cmd /k "cd /d %~dp0payment-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

echo Starting Order Service on port 8080...
start "Order Service (8080)" cmd /k "cd /d %~dp0order-service && mvn spring-boot:run"

echo.
echo All microservices launched!
echo Order Service:     http://localhost:8080/swagger-ui.html
echo Inventory Service: http://localhost:8081/swagger-ui.html
echo Payment Service:   http://localhost:8082/swagger-ui.html
pause
