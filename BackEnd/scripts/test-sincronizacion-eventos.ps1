# Script para probar la sincronización de eventos desde Proxy a Backend
# Autor: Sistema Backend
# Descripción: Fuerza la sincronización manual de eventos desde la Cátedra

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Test de Sincronización de Eventos" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8080"
$syncEndpoint = "$baseUrl/api/eventos-sync"

# Verificar estado del servicio
Write-Host "[1/3] Verificando estado del servicio de sincronización..." -ForegroundColor Yellow
try {
    $statusResponse = Invoke-RestMethod -Uri "$syncEndpoint/status" -Method Get -ContentType "application/json"
    Write-Host "✓ Servicio activo" -ForegroundColor Green
    Write-Host "  Mensaje: $($statusResponse.message)" -ForegroundColor Gray
    Write-Host "  Configuración: $($statusResponse.scheduleInfo)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "✗ Error al verificar estado del servicio" -ForegroundColor Red
    Write-Host "  $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "  ¿Está el Backend ejecutándose en $baseUrl?" -ForegroundColor Yellow
    Write-Host ""
    exit 1
}

# Forzar sincronización manual
Write-Host "[2/3] Forzando sincronización manual de eventos..." -ForegroundColor Yellow
try {
    $syncResponse = Invoke-RestMethod -Uri "$syncEndpoint/manual" -Method Post -ContentType "application/json"
    Write-Host "✓ Sincronización completada" -ForegroundColor Green
    Write-Host "  Respuesta: $syncResponse" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "✗ Error durante la sincronización" -ForegroundColor Red
    Write-Host "  $($_.Exception.Message)" -ForegroundColor Red

    # Intentar obtener más detalles del error
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $reader.BaseStream.Position = 0
        $reader.DiscardBufferedData()
        $responseBody = $reader.ReadToEnd()
        Write-Host "  Detalles: $responseBody" -ForegroundColor Red
    }
    Write-Host ""
}

# Verificar eventos en BD local
Write-Host "[3/3] Consultando eventos desde BD local..." -ForegroundColor Yellow
try {
    $eventosResponse = Invoke-RestMethod -Uri "$baseUrl/api/eventos-consulta/resumidos" -Method Get -ContentType "application/json"

    if ($eventosResponse.Count -gt 0) {
        Write-Host "✓ Se encontraron $($eventosResponse.Count) eventos en la BD local" -ForegroundColor Green
        Write-Host ""
        Write-Host "Primeros eventos:" -ForegroundColor Cyan
        $eventosResponse | Select-Object -First 5 | ForEach-Object {
            Write-Host "  - ID: $($_.id) | Título: $($_.titulo) | Precio: `$$($_.precioEntrada)" -ForegroundColor Gray
        }
    } else {
        Write-Host "⚠ No se encontraron eventos en la BD local" -ForegroundColor Yellow
        Write-Host "  Esto puede significar que el Proxy no tiene eventos o hubo un error" -ForegroundColor Yellow
    }
    Write-Host ""
} catch {
    Write-Host "✗ Error al consultar eventos" -ForegroundColor Red
    Write-Host "  $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test completado" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Para ver los datos en la BD, puedes usar SQLyog con:" -ForegroundColor Gray
Write-Host "  Host: localhost" -ForegroundColor Gray
Write-Host "  Puerto: 3306" -ForegroundColor Gray
Write-Host "  Usuario: devuser" -ForegroundColor Gray
Write-Host "  Contraseña: devpass" -ForegroundColor Gray
Write-Host "  Base de datos: MicroservicesFinal" -ForegroundColor Gray
Write-Host ""

