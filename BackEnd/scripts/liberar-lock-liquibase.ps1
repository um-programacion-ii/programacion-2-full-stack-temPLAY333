# Script para liberar el lock de Liquibase
# Este lock puede quedar activo si la aplicacion se cierra de forma abrupta
# Ejecutar: .\scripts\liberar-lock-liquibase.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  LIBERAR LOCK DE LIQUIBASE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar si el contenedor esta corriendo
$container = docker ps --filter "name=microservices-mysql-dev" --format "{{.Names}}"

if ($container -ne "microservices-mysql-dev") {
    Write-Host "[ERROR] El contenedor microservices-mysql-dev no esta corriendo" -ForegroundColor Red
    Write-Host "Ejecuta: docker compose up -d mysql-dev" -ForegroundColor Yellow
    exit 1
}

Write-Host "Verificando estado del lock..." -ForegroundColor Yellow
$lockStatus = docker exec microservices-mysql-dev mysql -u root -prootpass -e "SELECT * FROM DATABASECHANGELOGLOCK;" MicroservicesFinal 2>&1 | Where-Object { $_ -notmatch "Warning" -and $_ -notmatch "insecure" -and $_ -match "LOCKED" }

if ($lockStatus -match "LOCKED.*1") {
    Write-Host "[WARNING] Lock activo detectado" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Liberando lock..." -ForegroundColor Green

    $result = docker exec microservices-mysql-dev mysql -u root -prootpass -e "UPDATE DATABASECHANGELOGLOCK SET LOCKED = 0, LOCKGRANTED = NULL, LOCKEDBY = NULL WHERE ID = 1;" MicroservicesFinal 2>&1 | Where-Object { $_ -notmatch "Warning" -and $_ -notmatch "insecure" }

    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] Lock liberado correctamente" -ForegroundColor Green
    } else {
        Write-Host "[ERROR] Error al liberar el lock: $result" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "[OK] No hay lock activo" -ForegroundColor Green
}

Write-Host ""
Write-Host "Estado actual del lock:" -ForegroundColor Cyan
docker exec microservices-mysql-dev mysql -u root -prootpass -e "SELECT * FROM DATABASECHANGELOGLOCK;" MicroservicesFinal 2>&1 | Where-Object { $_ -notmatch "Warning" -and $_ -notmatch "insecure" }

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "[OK] Proceso completado" -ForegroundColor Green
Write-Host "Ahora puedes reiniciar la aplicacion sin problemas" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
