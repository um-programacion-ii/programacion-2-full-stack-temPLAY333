# Script para verificar si Liquibase se ejecuto en produccion
# Ejecutar: .\scripts\verificar-liquibase-prod.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  VERIFICAR LIQUIBASE EN PRODUCCION" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$container = docker ps --filter "name=microservices-mysql-prod" --format "{{.Names}}"

if ($container -ne "microservices-mysql-prod") {
    Write-Host "[ERROR] El contenedor microservices-mysql-prod no esta corriendo" -ForegroundColor Red
    Write-Host "Ejecuta: docker compose up -d mysql-prod" -ForegroundColor Yellow
    exit 1
}

Write-Host "1. Verificando tablas de Liquibase..." -ForegroundColor Green
$liquibaseTables = docker exec microservices-mysql-prod mysql -u root -pprodroot123 -e "SHOW TABLES LIKE 'DATABASECHANGELOG%';" MicroservicesFinal_prod 2>&1 | Where-Object { $_ -notmatch "Warning" -and $_ -notmatch "insecure" }

if ($liquibaseTables -match "DATABASECHANGELOG") {
    Write-Host "[OK] Tablas de Liquibase encontradas" -ForegroundColor Green

    Write-Host ""
    Write-Host "2. Verificando cambios aplicados..." -ForegroundColor Green
    docker exec microservices-mysql-prod mysql -u root -pprodroot123 -e "SELECT COUNT(*) as total_changes FROM DATABASECHANGELOG;" MicroservicesFinal_prod 2>&1 | Where-Object { $_ -notmatch "Warning" -and $_ -notmatch "insecure" }

    Write-Host ""
    Write-Host "3. Ultimos cambios aplicados:" -ForegroundColor Green
    docker exec microservices-mysql-prod mysql -u root -pprodroot123 -e "SELECT ID, AUTHOR, FILENAME, EXECTYPE, MD5SUM FROM DATABASECHANGELOG ORDER BY DATEEXECUTED DESC LIMIT 10;" MicroservicesFinal_prod 2>&1 | Where-Object { $_ -notmatch "Warning" -and $_ -notmatch "insecure" }
} else {
    Write-Host "[ERROR] Tablas de Liquibase NO encontradas" -ForegroundColor Red
    Write-Host "Liquibase NO se ha ejecutado en esta base de datos" -ForegroundColor Red
    Write-Host ""
    Write-Host "Solucion:" -ForegroundColor Yellow
    Write-Host "1. Verifica que la aplicacion este corriendo con perfil 'prod'" -ForegroundColor White
    Write-Host "2. Revisa los logs de la aplicacion para errores de Liquibase" -ForegroundColor White
    Write-Host "3. Verifica la conexion a la base de datos" -ForegroundColor White
    Write-Host "4. Ejecuta la aplicacion manualmente para que Liquibase se ejecute" -ForegroundColor White
}

Write-Host ""
Write-Host "4. Verificando tablas de la aplicacion..." -ForegroundColor Green
$appTables = docker exec microservices-mysql-prod mysql -u root -pprodroot123 -e "SHOW TABLES;" MicroservicesFinal_prod 2>&1 | Where-Object { $_ -notmatch "Warning" -and $_ -notmatch "insecure" -and $_ -notmatch "Tables_in" }

if ($appTables) {
    Write-Host "[OK] Tablas encontradas:" -ForegroundColor Green
    Write-Host $appTables
} else {
    Write-Host "[ERROR] No se encontraron tablas en la base de datos" -ForegroundColor Red
    Write-Host "La base de datos esta completamente vacia" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
