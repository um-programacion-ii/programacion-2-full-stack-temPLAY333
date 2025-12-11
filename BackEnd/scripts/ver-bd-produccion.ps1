# Script para ver datos de la Base de Datos de PRODUCCIÓN
# Ejecutar: .\scripts\ver-bd-produccion.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  BASE DE DATOS DE PRODUCCIÓN" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Conectando a: microservices-mysql-prod (puerto 3307)" -ForegroundColor Yellow
Write-Host ""

# Verificar si el contenedor está corriendo
$container = docker ps --filter "name=microservices-mysql-prod" --format "{{.Names}}"

if ($container -ne "microservices-mysql-prod") {
    Write-Host "ERROR: El contenedor microservices-mysql-prod no está corriendo" -ForegroundColor Red
    exit 1
}

Write-Host "1. Tablas disponibles:" -ForegroundColor Green
docker exec microservices-mysql-prod mysql -u root -pprodroot123 -e "USE MicroservicesFinal_prod; SHOW TABLES;" 2>$null

Write-Host ""
Write-Host "2. Cantidad de registros:" -ForegroundColor Green
docker exec microservices-mysql-prod mysql -u root -pprodroot123 -e "
USE MicroservicesFinal_prod;
SELECT
    (SELECT COUNT(*) FROM evento) as total_eventos,
    (SELECT COUNT(*) FROM evento_tipo) as total_tipos,
    (SELECT COUNT(*) FROM integrante) as total_integrantes,
    (SELECT COUNT(*) FROM venta) as total_ventas,
    (SELECT COUNT(*) FROM asiento) as total_asientos;
" 2>$null

Write-Host ""
Write-Host "3. Eventos disponibles:" -ForegroundColor Green
docker exec microservices-mysql-prod mysql -u root -pprodroot123 -e "
USE MicroservicesFinal_prod;
SELECT e.id, e.titulo, e.fecha, e.precio_entrada, et.nombre as tipo
FROM evento e
LEFT JOIN evento_tipo et ON e.evento_tipo_id = et.id
ORDER BY e.fecha DESC
LIMIT 10;
" 2>$null

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Para acceso interactivo ejecuta:" -ForegroundColor Yellow
Write-Host "docker exec -it microservices-mysql-prod mysql -u root -pprodroot123 MicroservicesFinal_prod" -ForegroundColor White
Write-Host "  o con usuario de aplicación:" -ForegroundColor Yellow
Write-Host "docker exec -it microservices-mysql-prod mysql -u produser -pprodpass123 MicroservicesFinal_prod" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan

