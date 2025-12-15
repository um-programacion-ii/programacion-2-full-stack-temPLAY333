# Script para RESETEAR completamente la base de datos de DESARROLLO
# ADVERTENCIA: Esto eliminará TODOS los datos de la base de datos
# Ejecutar: .\scripts\reset-database-dev.ps1

Write-Host "========================================" -ForegroundColor Red
Write-Host "  [ADVERTENCIA] RESET COMPLETO DE BASE DE DATOS" -ForegroundColor Red
Write-Host "========================================" -ForegroundColor Red
Write-Host ""
Write-Host "Este script eliminará TODAS las tablas y datos de la BD de desarrollo" -ForegroundColor Yellow
Write-Host ""

# Verificar si el contenedor está corriendo
$container = docker ps --filter "name=microservices-mysql-dev" --format "{{.Names}}"

if ($container -ne "microservices-mysql-dev") {
    Write-Host "ERROR: El contenedor microservices-mysql-dev no está corriendo" -ForegroundColor Red
    Write-Host "Ejecuta: docker compose up -d mysql-dev" -ForegroundColor Yellow
    exit 1
}

# Confirmación
$confirm = Read-Host "¿Estás seguro de que quieres eliminar TODOS los datos? (escribe 'SI' para confirmar)"
if ($confirm -ne "SI") {
    Write-Host "Operación cancelada" -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "Eliminando todas las tablas..." -ForegroundColor Yellow

# Desactivar foreign key checks temporalmente
Write-Host "  Desactivando foreign key checks..." -ForegroundColor Gray
docker exec microservices-mysql-dev mysql -u root -prootpass -e "SET FOREIGN_KEY_CHECKS = 0;" MicroservicesFinal 2>&1 | Out-Null

# Obtener lista de tablas
$tablesResult = docker exec microservices-mysql-dev mysql -u root -prootpass -N -e "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = 'MicroservicesFinal' AND TABLE_TYPE = 'BASE TABLE';" MicroservicesFinal 2>&1

if ($tablesResult -and -not $tablesResult.StartsWith("ERROR")) {
    $tables = $tablesResult -split "`n" | Where-Object { $_.Trim() -ne "" }

    foreach ($table in $tables) {
        $tableName = $table.Trim()
        if ($tableName) {
            Write-Host "  Eliminando tabla: $tableName" -ForegroundColor Gray
            docker exec microservices-mysql-dev mysql -u root -prootpass -e "DROP TABLE IF EXISTS `"$tableName`";" MicroservicesFinal 2>&1 | Out-Null
        }
    }
} else {
    Write-Host "  No se encontraron tablas o error al listarlas: $tablesResult" -ForegroundColor Yellow
}

# Limpiar tablas de Liquibase también
Write-Host ""
Write-Host "Limpiando tablas de Liquibase..." -ForegroundColor Yellow
docker exec microservices-mysql-dev mysql -u root -prootpass -e "DROP TABLE IF EXISTS DATABASECHANGELOG;" MicroservicesFinal 2>&1 | Out-Null
docker exec microservices-mysql-dev mysql -u root -prootpass -e "DROP TABLE IF EXISTS DATABASECHANGELOGLOCK;" MicroservicesFinal 2>&1 | Out-Null

# Reactivar foreign key checks
Write-Host "  Reactivando foreign key checks..." -ForegroundColor Gray
docker exec microservices-mysql-dev mysql -u root -prootpass -e "SET FOREIGN_KEY_CHECKS = 1;" MicroservicesFinal 2>&1 | Out-Null

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "[OK] Base de datos reseteada completamente" -ForegroundColor Green
Write-Host ""
Write-Host "Próximos pasos:" -ForegroundColor Yellow
Write-Host "1. Reinicia la aplicación Spring Boot" -ForegroundColor White
Write-Host "2. Liquibase recreará todas las tablas automáticamente" -ForegroundColor White
Write-Host "3. El constraint único incorrecto NO se recreará" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan
