# Script para verificar que perfil de Spring esta activo
# Busca en los logs de la aplicacion el perfil activo
# Ejecutar: .\scripts\verificar-perfil-activo.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  VERIFICAR PERFIL ACTIVO DE SPRING" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "El perfil activo se muestra en los logs de inicio de la aplicacion." -ForegroundColor Yellow
Write-Host "Busca una linea que diga:" -ForegroundColor Yellow
Write-Host "  Profile(s): [dev]  o  Profile(s): [prod]" -ForegroundColor White
Write-Host ""

Write-Host "Para verificar el perfil activo:" -ForegroundColor Green
Write-Host "1. Revisa los logs de la aplicacion Spring Boot" -ForegroundColor White
Write-Host "2. Busca la seccion que muestra 'Profile(s):'" -ForegroundColor White
Write-Host "3. O ejecuta la aplicacion con:" -ForegroundColor White
Write-Host "   mvn spring-boot:run -Dspring-boot.run.profiles=dev" -ForegroundColor Cyan
Write-Host "   mvn spring-boot:run -Dspring-boot.run.profiles=prod" -ForegroundColor Cyan
Write-Host ""

Write-Host "Configuracion de bases de datos:" -ForegroundColor Green
Write-Host "  DEV:  localhost:3306 -> MicroservicesFinal" -ForegroundColor White
Write-Host "  PROD: localhost:3307 -> MicroservicesFinal_prod" -ForegroundColor White
Write-Host ""

Write-Host "Para cambiar el perfil activo:" -ForegroundColor Yellow
Write-Host "  - Variable de entorno: SPRING_PROFILES_ACTIVE=dev" -ForegroundColor White
Write-Host "  - Argumento JVM: -Dspring.profiles.active=dev" -ForegroundColor White
Write-Host "  - En application.yml (no recomendado para prod)" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan
