# Script para ver datos de la Base de Datos de DESARROLLO
# Ejecutar: .\scripts\ver-bd-desarrollo.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  BASE DE DATOS DE DESARROLLO" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Conectando a: microservices-mysql-dev (puerto 3306)" -ForegroundColor Yellow
Write-Host ""

# Verificar si el contenedor está corriendo
$container = docker ps --filter "name=microservices-mysql-dev" --format "{{.Names}}"

if ($container -ne "microservices-mysql-dev") {
    Write-Host "ERROR: El contenedor microservices-mysql-dev no está corriendo" -ForegroundColor Red
    Write-Host "Ejecuta: docker compose -f src/main/docker/mysql.yml up -d" -ForegroundColor Yellow
    exit 1
}

Write-Host "1. Tablas disponibles:" -ForegroundColor Green
docker exec microservices-mysql-dev mysql -u root -prootpass -e "USE MicroservicesFinal; SHOW TABLES;" 2>$null

Write-Host ""
Write-Host "2. Cantidad de registros:" -ForegroundColor Green
docker exec microservices-mysql-dev mysql -u root -prootpass -e "
USE MicroservicesFinal;
SELECT
    (SELECT COUNT(*) FROM evento) as total_eventos,
    (SELECT COUNT(*) FROM evento_tipo) as total_tipos,
    (SELECT COUNT(*) FROM integrante) as total_integrantes,
    (SELECT COUNT(*) FROM venta) as total_ventas,
    (SELECT COUNT(*) FROM asiento) as total_asientos;
" 2>$null

Write-Host ""
Write-Host "3. Eventos disponibles:" -ForegroundColor Green
docker exec microservices-mysql-dev mysql -u root -prootpass -e "
USE MicroservicesFinal;
SELECT e.id, e.titulo, e.fecha, e.precio_entrada, et.nombre as tipo
FROM evento e
LEFT JOIN evento_tipo et ON e.evento_tipo_id = et.id
ORDER BY e.fecha DESC
LIMIT 10;
" 2>$null

Write-Host ""
Write-Host "4. Tipos de eventos:" -ForegroundColor Green
docker exec microservices-mysql-dev mysql -u root -prootpass -e "
USE MicroservicesFinal;
SELECT * FROM evento_tipo;
" 2>$null

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Para acceso interactivo ejecuta:" -ForegroundColor Yellow
Write-Host "docker exec -it microservices-mysql-dev mysql -u root -prootpass MicroservicesFinal" -ForegroundColor White
Write-Host "  o con usuario de aplicación:" -ForegroundColor Yellow
Write-Host "docker exec -it microservices-mysql-dev mysql -u devuser -pdevpass MicroservicesFinal" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan

