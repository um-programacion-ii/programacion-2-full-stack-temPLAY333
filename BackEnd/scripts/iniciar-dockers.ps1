# Script para iniciar todos los contenedores Docker
# Ejecutar: .\scripts\iniciar-dockers.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  INICIANDO CONTENEDORES DOCKER" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$dockerComposeFile = "docker-compose.yml"

if (-Not (Test-Path $dockerComposeFile)) {
    Write-Host "ERROR: No se encuentra el archivo docker-compose.yml" -ForegroundColor Red
    Write-Host "Asegúrate de ejecutar este script desde la carpeta BackEnd" -ForegroundColor Yellow
    exit 1
}

Write-Host "Iniciando contenedores..." -ForegroundColor Yellow
docker compose up -d

Write-Host ""
Write-Host "Esperando que los contenedores estén listos..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  CONTENEDORES EN EJECUCIÓN" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Bases de datos disponibles:" -ForegroundColor Yellow
Write-Host ""
Write-Host "  1. MySQL DESARROLLO:" -ForegroundColor Green
Write-Host "     - Host: localhost:3306" -ForegroundColor White
Write-Host "     - Usuario: root / Password: rootpass" -ForegroundColor White
Write-Host "     - BD: MicroservicesFinal" -ForegroundColor White
Write-Host ""
Write-Host "  2. MySQL PRODUCCIÓN:" -ForegroundColor Green
Write-Host "     - Host: localhost:3307" -ForegroundColor White
Write-Host "     - Usuario: root / Password: prodroot123" -ForegroundColor White
Write-Host "     - BD: MicroservicesFinal_prod" -ForegroundColor White
Write-Host ""
Write-Host "  3. Redis CACHE:" -ForegroundColor Green
Write-Host "     - Host: localhost:6379" -ForegroundColor White
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Para ver los datos ejecuta:" -ForegroundColor Yellow
Write-Host "  .\scripts\ver-bd-desarrollo.ps1" -ForegroundColor White
Write-Host "  .\scripts\ver-bd-produccion.ps1" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan

