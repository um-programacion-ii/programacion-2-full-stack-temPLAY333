# Script para ver datos de la Base de Datos de PRODUCCION
# Ejecutar: .\scripts\ver-bd-produccion.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  BASE DE DATOS DE PRODUCCION" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Conectando a: microservices-mysql-prod (puerto 3307)" -ForegroundColor Yellow
Write-Host ""

# Verificar si el contenedor esta corriendo
$container = docker ps --filter "name=microservices-mysql-prod" --format "{{.Names}}"

if ($container -ne "microservices-mysql-prod") {
    Write-Host "ERROR: El contenedor microservices-mysql-prod no esta corriendo" -ForegroundColor Red
    Write-Host "Ejecuta: docker compose up -d mysql-prod" -ForegroundColor Yellow
    exit 1
}

Write-Host "1. Tablas disponibles:" -ForegroundColor Green
docker exec microservices-mysql-prod mysql -u root -prootroot123 -e "USE MicroservicesFinal_prod; SHOW TABLES;" 2>&1 | Where-Object { $_ -notmatch "Warning" -and $_ -notmatch "insecure" }

Write-Host ""
Write-Host "2. Cantidad de registros:" -ForegroundColor Green
docker exec microservices-mysql-prod mysql -u root -prootroot123 -e "
USE MicroservicesFinal_prod;
SELECT
    (SELECT COUNT(*) FROM evento) as total_eventos,
    (SELECT COUNT(*) FROM evento_tipo) as total_tipos,
    (SELECT COUNT(*) FROM integrante) as total_integrantes,
    (SELECT COUNT(*) FROM venta) as total_ventas,
    (SELECT COUNT(*) FROM asiento) as total_asientos;
" 2>&1 | Where-Object { $_ -notmatch "Warning" -and $_ -notmatch "insecure" }

Write-Host ""
Write-Host "3. Eventos disponibles:" -ForegroundColor Green
docker exec microservices-mysql-prod mysql -u root -prootroot123 -e "
USE MicroservicesFinal_prod;
SELECT e.id, e.titulo, e.fecha, e.precio_entrada, et.nombre as tipo
FROM evento e
LEFT JOIN evento_tipo et ON e.evento_tipo_id = et.id
ORDER BY e.fecha DESC
LIMIT 10;
" 2>&1 | Where-Object { $_ -notmatch "Warning" -and $_ -notmatch "insecure" }

Write-Host ""
Write-Host "4. Tipos de eventos:" -ForegroundColor Green
docker exec microservices-mysql-prod mysql -u root -prootroot123 -e "
USE MicroservicesFinal_prod;
SELECT * FROM evento_tipo;
" 2>&1 | Where-Object { $_ -notmatch "Warning" -and $_ -notmatch "insecure" }

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Para acceso interactivo ejecuta:" -ForegroundColor Yellow
Write-Host "docker exec -it microservices-mysql-prod mysql -u root -prootroot123 MicroservicesFinal_prod" -ForegroundColor White
Write-Host "  o con usuario de aplicacion:" -ForegroundColor Yellow
Write-Host "docker exec -it microservices-mysql-prod mysql -u produser -pprodpass123 MicroservicesFinal_prod" -ForegroundColor White
Write-Host ""
Write-Host "Para DBeaver:" -ForegroundColor Yellow
Write-Host "  Host: localhost" -ForegroundColor White
Write-Host "  Port: 3307" -ForegroundColor White
Write-Host "  Database: MicroservicesFinal_prod" -ForegroundColor White
Write-Host "  Username: produser" -ForegroundColor White
Write-Host "  Password: prodpass123" -ForegroundColor White
Write-Host "  Driver Property: allowPublicKeyRetrieval=true" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan
