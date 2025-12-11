# Script para iniciar el Backend en modo PRODUCCIÓN
# Base de datos: MicroservicesFinal_prod (puerto 3307)
# Sincronización: Manual (NO al iniciar), cada hora después

# Cambiar título de la ventana para identificación fácil
$host.UI.RawUI.WindowTitle = "🔶 BACKEND API - PROD - Puerto 8081"

Write-Host ""
Write-Host "╔════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║                                                ║" -ForegroundColor Cyan
Write-Host "║       🔶 BACKEND API - Modo PROD 🔶           ║" -ForegroundColor Red
Write-Host "║                                                ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
Write-Host "Configuración:" -ForegroundColor Yellow
Write-Host "Configuración:" -ForegroundColor Yellow
Write-Host "  • Perfil: prod" -ForegroundColor Gray
Write-Host "  • Puerto: 8081" -ForegroundColor Gray
Write-Host "  • BD: MicroservicesFinal_prod (localhost:3307)" -ForegroundColor Gray
Write-Host "  • Usuario BD: produser / prodpass123" -ForegroundColor Gray
Write-Host "  • Proxy: http://localhost:8080" -ForegroundColor Gray
Write-Host "  • Sincronización al iniciar: NO" -ForegroundColor Red
Write-Host "  • Sincronización automática: Cada hora (después de iniciar)" -ForegroundColor Gray
Write-Host ""

# Verificar que Docker esté ejecutándose
Write-Host "[1/3] Verificando servicios Docker..." -ForegroundColor Yellow
$dockerContainers = docker ps --format "{{.Names}}" 2>$null

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Docker no está ejecutándose" -ForegroundColor Red
    Write-Host "  Inicia Docker Desktop e intenta nuevamente" -ForegroundColor Yellow
    exit 1
}

$requiredContainers = @("microservices-mysql-prod", "microservices-redis")
$missingContainers = @()

foreach ($container in $requiredContainers) {
    if ($dockerContainers -notcontains $container) {
        $missingContainers += $container
    }
}

if ($missingContainers.Count -gt 0) {
    Write-Host "⚠ Contenedores faltantes: $($missingContainers -join ', ')" -ForegroundColor Yellow
    Write-Host "  Ejecuta: docker-compose up -d" -ForegroundColor Yellow
    Write-Host ""

    $response = Read-Host "¿Deseas iniciar Docker Compose ahora? (s/n)"
    if ($response -eq "s" -or $response -eq "S") {
        Write-Host "Iniciando Docker Compose..." -ForegroundColor Cyan
        docker-compose up -d
        Start-Sleep -Seconds 5
    } else {
        exit 1
    }
}

Write-Host "✓ Docker está ejecutándose" -ForegroundColor Green
Write-Host ""

# Verificar que el Proxy esté ejecutándose (opcional)
Write-Host "[2/3] Verificando Proxy..." -ForegroundColor Yellow
try {
    $proxyResponse = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 2 -ErrorAction SilentlyContinue
    Write-Host "✓ Proxy está activo en puerto 8080" -ForegroundColor Green
} catch {
    Write-Host "⚠ Proxy no responde en http://localhost:8080" -ForegroundColor Yellow
    Write-Host "  El Backend iniciará pero NO podrá sincronizar eventos" -ForegroundColor Yellow
}
Write-Host ""

# Iniciar Backend con perfil prod
Write-Host "[3/3] Iniciando Backend en modo PROD..." -ForegroundColor Yellow
Write-Host ""
Write-Host "⚠ IMPORTANTE: Sincronización manual requerida" -ForegroundColor Yellow
Write-Host "  Después de iniciar, ejecuta:" -ForegroundColor Gray
Write-Host "  curl -X POST http://localhost:8081/api/eventos-sync/manual" -ForegroundColor Cyan
Write-Host ""
Write-Host "⏳ Esto tomará 30-60 segundos..." -ForegroundColor Gray
Write-Host ""
Write-Host "Logs:" -ForegroundColor Cyan
Write-Host "─────────────────────────────────────────────────────────────" -ForegroundColor DarkGray

# Ejecutar con perfil prod usando variable de entorno
$env:SPRING_PROFILES_ACTIVE = "prod"
.\mvnw.cmd spring-boot:run

# Nota: El script quedará ejecutando el Backend hasta que se presione Ctrl+C

