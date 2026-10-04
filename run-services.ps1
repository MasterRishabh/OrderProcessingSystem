param (
    [string]$Profile = ""
)

$argsSuffix = ""
if ($Profile -ne "") {
    $argsSuffix = " -Dspring-boot.run.profiles=$Profile"
    Write-Host "Running services with active Spring profile: $Profile" -ForegroundColor Magenta
}

# PowerShell Script to Launch All Microservices
Write-Host "Starting Inventory Service on port 8081..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\inventory-service'; mvn spring-boot:run$argsSuffix"

Start-Sleep -Seconds 5

Write-Host "Starting Payment Service on port 8082..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\payment-service'; mvn spring-boot:run$argsSuffix"

Start-Sleep -Seconds 5

Write-Host "Starting Order Service on port 8080..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\order-service'; mvn spring-boot:run$argsSuffix"

Write-Host "`nAll 3 services are launching in background windows!" -ForegroundColor Green
Write-Host "Order Service:     http://localhost:8080/swagger-ui.html" -ForegroundColor Yellow
Write-Host "Inventory Service: http://localhost:8081/swagger-ui.html" -ForegroundColor Yellow
Write-Host "Payment Service:   http://localhost:8082/swagger-ui.html" -ForegroundColor Yellow
