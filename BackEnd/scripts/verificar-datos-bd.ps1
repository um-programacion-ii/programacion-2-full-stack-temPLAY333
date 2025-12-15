# Script para verificar datos en ambas bases de datos (dev y prod)
# Ejecutar: .\scripts\verificar-datos-bd.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  VERIFICACION DE DATOS EN BD" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar contenedores
$devContainer = docker ps --filter "name=microservices-mysql-dev" --format "{{.Names}}" 2>&1
$prodContainer = docker ps --filter "name=microservices-mysql-prod" --format "{{.Names}}" 2>&1

Write-Host "1. BASE DE DATOS DE DESARROLLO (puerto 3306)" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow

if ($devContainer -eq "microservices-mysql-dev") {
    Write-Host "[OK] Contenedor dev esta corriendo" -ForegroundColor Green

    Write-Host ""
    Write-Host "Cantidad de registros:" -ForegroundColor Green
    docker exec microservices-mysql-dev mysql -u root -prootpass -e "
    USE MicroservicesFinal;
    SELECT
        (SELECT COUNT(*) FROM evento) as total_eventos,
        (SELECT COUNT(*) FROM evento_tipo) as total_tipos,
        (SELECT COUNT(*) FROM integrante) as total_integrantes,
        (SELECT COUNT(*) FROM venta) as total_ventas,
        (SELECT COUNT(*) FROM asiento) as total_asientos;
    " 2>&1 | Where-Object { $_ -notmatch "Warning" -and $_ -notmatch "insecure" }

    Write-Host ""
    Write-Host "Ultimos 5 eventos:" -ForegroundColor Green
    docker exec microservices-mysql-dev mysql -u root -prootpass -e "
    USE MicroservicesFinal;
    SELECT e.id, e.titulo, e.fecha, et.nombre as tipo
    FROM evento e
    LEFT JOIN evento_tipo et ON e.evento_tipo_id = et.id
    ORDER BY e.id DESC
    LIMIT 5;
    " 2>&1 | Where-Object { $_ -notmatch "Warning" -and $_ -notmatch "insecure" }
} else {
    Write-Host "[ERROR] Contenedor dev NO esta corriendo" -ForegroundColor Red
    Write-Host "Ejecuta: docker compose up -d mysql-dev" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "2. BASE DE DATOS DE PRODUCCION (puerto 3307)" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow

if ($prodContainer -eq "microservices-mysql-prod") {
    Write-Host "[OK] Contenedor prod esta corriendo" -ForegroundColor Green

    Write-Host ""
    Write-Host "Cantidad de registros:" -ForegroundColor Green
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
    Write-Host "Ultimos 5 eventos:" -ForegroundColor Green
    docker exec microservices-mysql-prod mysql -u root -prootroot123 -e "
    USE MicroservicesFinal_prod;
    SELECT e.id, e.titulo, e.fecha, et.nombre as tipo
    FROM evento e
    LEFT JOIN evento_tipo et ON e.evento_tipo_id = et.id
    ORDER BY e.id DESC
    LIMIT 5;
    " 2>&1 | Where-Object { $_ -notmatch "Warning" -and $_ -notmatch "insecure" }
} else {
    Write-Host "[ERROR] Contenedor prod NO esta corriendo" -ForegroundColor Red
    Write-Host "Ejecuta: docker compose up -d mysql-prod" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "NOTA: Para verificar que perfil esta activo en la aplicacion," -ForegroundColor Yellow
Write-Host "revisa los logs de inicio. Deberias ver algo como:" -ForegroundColor Yellow
Write-Host "  Profile(s): [dev] o Profile(s): [prod]" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan
