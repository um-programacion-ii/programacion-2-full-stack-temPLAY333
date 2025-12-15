# Script de prueba para verificar conexion a MySQL en Docker
# Ejecutar: .\scripts\test-docker-mysql.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  PRUEBA DE CONEXION A MYSQL EN DOCKER" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar si Docker esta corriendo
Write-Host "1. Verificando Docker..." -ForegroundColor Green
$dockerVersion = docker --version 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Docker no esta instalado o no esta en el PATH" -ForegroundColor Red
    exit 1
}
Write-Host "[OK] Docker encontrado: $dockerVersion" -ForegroundColor Green
Write-Host ""

# Verificar si el contenedor esta corriendo
Write-Host "2. Verificando contenedor MySQL..." -ForegroundColor Green
$container = docker ps --filter "name=microservices-mysql-dev" --format "{{.Names}}" 2>&1

if ($container -ne "microservices-mysql-dev") {
    Write-Host "[ERROR] El contenedor microservices-mysql-dev no esta corriendo" -ForegroundColor Red
    Write-Host ""
    Write-Host "Para iniciarlo, ejecuta:" -ForegroundColor Yellow
    Write-Host "  docker compose up -d mysql-dev" -ForegroundColor White
    Write-Host "  o" -ForegroundColor Yellow
    Write-Host "  docker compose -f docker-compose.yml up -d mysql-dev" -ForegroundColor White
    exit 1
}
Write-Host "[OK] Contenedor encontrado: $container" -ForegroundColor Green
Write-Host ""

# Probar conexion simple
Write-Host "3. Probando conexion a MySQL..." -ForegroundColor Green
$testQuery = "SELECT 1 as test;"
$testResult = docker exec microservices-mysql-dev mysql -u root -prootpass -e $testQuery MicroservicesFinal 2>&1

$hasError = $false
if ($testResult) {
    $hasError = $testResult.ToString() -match "ERROR"
}

if ($LASTEXITCODE -eq 0 -and -not $hasError) {
    Write-Host "[OK] Conexion exitosa" -ForegroundColor Green
} else {
    Write-Host "[ERROR] Error de conexion: $testResult" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Listar tablas
Write-Host "4. Listando tablas..." -ForegroundColor Green
$showTablesQuery = "SHOW TABLES;"
$tablesOutput = docker exec microservices-mysql-dev mysql -u root -prootpass -e $showTablesQuery MicroservicesFinal 2>&1 | Where-Object { $_ -notmatch "Warning" -and $_ -notmatch "insecure" }
Write-Host $tablesOutput

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "[OK] Todas las pruebas pasaron" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
