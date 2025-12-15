# Script para probar webhooks del Backend (simulando el Proxy)
# Simula que el Proxy envía notificaciones de eventos de Kafka

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Test de Webhooks Backend ← Proxy" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$backendUrl = "http://localhost:8081"
$webhookEndpoint = "$backendUrl/api/webhooks/evento-cambio"
$payloadsDir = ".\scripts\payloads"

# Verificar que el Backend esté ejecutándose
Write-Host "[0/4] Verificando que el Backend esté activo..." -ForegroundColor Yellow
try {
    $healthResponse = Invoke-RestMethod -Uri "$backendUrl/api/webhooks/health" -Method Get
    Write-Host "✓ Webhook endpoint activo: $healthResponse" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "✗ Backend no está ejecutándose en $backendUrl" -ForegroundColor Red
    Write-Host "  Inicia el Backend con: mvn spring-boot:run" -ForegroundColor Yellow
    Write-Host ""
    exit 1
}

# Test 1: EVENTO_CAMBIADO
Write-Host "[1/4] Test: EVENTO_CAMBIADO" -ForegroundColor Yellow
try {
    $payloadFile = Join-Path $payloadsDir "webhook-evento-cambiado.json"
    $payload = Get-Content $payloadFile -Raw

    $response = Invoke-RestMethod -Uri $webhookEndpoint -Method Post `
        -ContentType "application/json" -Body $payload

    Write-Host "✓ Webhook EVENTO_CAMBIADO procesado" -ForegroundColor Green
    Write-Host "  Respuesta: $response" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "✗ Error al enviar webhook EVENTO_CAMBIADO" -ForegroundColor Red
    Write-Host "  $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

Start-Sleep -Seconds 2

# Test 2: ASIENTOS_BLOQUEADOS
Write-Host "[2/4] Test: ASIENTOS_BLOQUEADOS" -ForegroundColor Yellow
try {
    $payloadFile = Join-Path $payloadsDir "webhook-asientos-bloqueados.json"
    $payload = Get-Content $payloadFile -Raw

    $response = Invoke-RestMethod -Uri $webhookEndpoint -Method Post `
        -ContentType "application/json" -Body $payload

    Write-Host "✓ Webhook ASIENTOS_BLOQUEADOS procesado" -ForegroundColor Green
    Write-Host "  Respuesta: $response" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "✗ Error al enviar webhook ASIENTOS_BLOQUEADOS" -ForegroundColor Red
    Write-Host "  $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

Start-Sleep -Seconds 2

# Test 3: VENTA_COMPLETADA
Write-Host "[3/4] Test: VENTA_COMPLETADA" -ForegroundColor Yellow
try {
    $payloadFile = Join-Path $payloadsDir "webhook-venta-completada.json"
    $payload = Get-Content $payloadFile -Raw

    $response = Invoke-RestMethod -Uri $webhookEndpoint -Method Post `
        -ContentType "application/json" -Body $payload

    Write-Host "✓ Webhook VENTA_COMPLETADA procesado" -ForegroundColor Green
    Write-Host "  Respuesta: $response" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "✗ Error al enviar webhook VENTA_COMPLETADA" -ForegroundColor Red
    Write-Host "  $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

Start-Sleep -Seconds 2

# Test 4: Verificar eventos en BD
Write-Host "[4/4] Verificando eventos en BD local..." -ForegroundColor Yellow
try {
    $eventosResponse = Invoke-RestMethod -Uri "$backendUrl/api/eventos-consulta/resumidos" -Method Get

    Write-Host "✓ Eventos en BD local: $($eventosResponse.Count)" -ForegroundColor Green

    if ($eventosResponse.Count -gt 0) {
        Write-Host ""
        Write-Host "Eventos:" -ForegroundColor Cyan
        $eventosResponse | Select-Object -First 3 | ForEach-Object {
            Write-Host "  - ID: $($_.id) | Título: $($_.titulo)" -ForegroundColor Gray
        }
    }
    Write-Host ""
} catch {
    Write-Host "⚠ No se pudieron obtener eventos desde BD local" -ForegroundColor Yellow
    Write-Host "  $($_.Exception.Message)" -ForegroundColor Yellow
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Tests de Webhooks Completados" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📝 Revisa los logs del Backend para ver cómo se procesaron los eventos" -ForegroundColor Gray
Write-Host ""
Write-Host "Logs esperados:" -ForegroundColor Gray
Write-Host "  - 'Webhook recibido del Proxy - Topic: EVENTO_CAMBIADO, ...'" -ForegroundColor DarkGray
Write-Host "  - 'Procesando EVENTO_CAMBIADO: {\"\"eventoId\"\":1,...}'" -ForegroundColor DarkGray
Write-Host "  - 'Evento cambiado procesado - EventoId: 1'" -ForegroundColor DarkGray
Write-Host ""

