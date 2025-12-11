# Script para probar endpoints DEL PROXY (localhost:8080)
# El Proxy maneja la autenticación automáticamente

# Rutas
$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot ".."))
$EnvPath = Join-Path $ProjectRoot ".env"
$PayloadDir = Join-Path $PSScriptRoot "payloads"

function Load-EnvFile {
    param([string]$Path)
    if (!(Test-Path $Path)) { Write-Host "ERROR: .env no encontrado en $Path" -ForegroundColor Red; exit 1 }
    Get-Content $Path | ForEach-Object {
        if ($_ -match '^\s*([^#][^=]*?)\s*=\s*(.*?)\s*$') {
            Set-Variable -Name $matches[1] -Value $matches[2] -Scope Script
        }
    }
}

function Ensure-Dir {
    param([string]$Path)
    if (!(Test-Path $Path)) { New-Item -ItemType Directory -Path $Path | Out-Null }
}

function Ensure-PayloadFile {
    param([string]$FilePath, [string]$DefaultJson)
    if (!(Test-Path $FilePath)) { Set-Content -Path $FilePath -Value $DefaultJson -Encoding UTF8 }
}

function Load-Or-Edit-Payload {
    param([string]$FilePath)
    Write-Host "Payload en: $FilePath" -ForegroundColor Yellow
    $content = Get-Content -Path $FilePath -Raw -ErrorAction Stop
    Write-Host "Contenido actual:" -ForegroundColor Gray
    Write-Host $content
    $ans = Read-Host "¿Deseas editarlo en el Bloc de notas? (s/N)"
    if ($ans -match '^[sS]') {
        & notepad.exe $FilePath | Out-Null
        Start-Sleep -Milliseconds 200
        $content = Get-Content -Path $FilePath -Raw -ErrorAction Stop
    }
    return $content
}

Write-Host "=== Cargando configuracion ===" -ForegroundColor Cyan
Load-EnvFile -Path $EnvPath

# Configuración para el PROXY
$PROXY_URL = "http://localhost:8080"
$API_BASE = "/api"

Write-Host "DEBUG: PROXY_URL = $PROXY_URL" -ForegroundColor Gray
Write-Host "DEBUG: API_BASE = $API_BASE" -ForegroundColor Gray
Write-Host "NOTA: El Proxy maneja la autenticación JWT automáticamente" -ForegroundColor Cyan
Write-Host ""

$jsonContent = 'application/json'
$headers = @{ "Content-Type" = $jsonContent }

function Show-Menu {
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "PRUEBA DE ENDPOINTS DEL PROXY" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Proxy URL: $PROXY_URL" -ForegroundColor Yellow
    Write-Host "Autenticación: Automática (manejada por el Proxy)" -ForegroundColor Green
    Write-Host ""
    Write-Host "== USUARIOS ==" -ForegroundColor Magenta
    Write-Host "1. Login de usuario (POST $API_BASE/users/login)" -ForegroundColor White
    Write-Host ""
    Write-Host "== EVENTOS ==" -ForegroundColor Magenta
    Write-Host "2. Listado eventos resumidos (GET $API_BASE/eventos/resumidos)" -ForegroundColor White
    Write-Host "3. Listado eventos completos (GET $API_BASE/eventos)" -ForegroundColor White
    Write-Host "4. Ver evento por ID (GET $API_BASE/eventos/{id})" -ForegroundColor White
    Write-Host "5. Estado de asientos desde Redis (GET $API_BASE/eventos/{id}/asientos-estado)" -ForegroundColor White
    Write-Host "6. Bloquear asientos (POST $API_BASE/eventos/{id}/bloquear-asientos)" -ForegroundColor White
    Write-Host ""
    Write-Host "== VENTAS ==" -ForegroundColor Magenta
    Write-Host "7. Realizar venta (POST $API_BASE/ventas/realizar)" -ForegroundColor White
    Write-Host "8. Listar ventas (GET $API_BASE/ventas)" -ForegroundColor White
    Write-Host "9. Ver venta por ID (GET $API_BASE/ventas/{id})" -ForegroundColor White
    Write-Host ""
    Write-Host "== ADMINISTRACIÓN ==" -ForegroundColor Magenta
    Write-Host "A. Health check (GET /actuator/health)" -ForegroundColor White
    Write-Host "B. Estado del token JWT (GET /actuator/auth/status)" -ForegroundColor White
    Write-Host "C. Renovar token JWT (POST /actuator/auth/refresh)" -ForegroundColor White
    Write-Host ""
    Write-Host "0. Salir" -ForegroundColor Gray
    Write-Host ""
}

function Invoke-ProxyRequest {
    param(
        [string]$Url,
        [string]$Method = 'Get',
        [string]$Body = $null,
        [string]$Description = ''
    )

    Write-Host ""; Write-Host ">>> $Description" -ForegroundColor Cyan
    Write-Host ("    {0} {1}" -f $Method, $Url) -ForegroundColor Gray

    $params = @{ Uri = $Url; Method = $Method; Headers = $headers; ErrorAction = 'Stop' }
    if ($Body) { $params.Body = $Body; $params.ContentType = 'application/json' }

    try {
        $resp = Invoke-RestMethod @params
        Write-Host "EXITO" -ForegroundColor Green

        # Verificar si la respuesta es nula o vacía
        if ($null -eq $resp) {
            Write-Host "Respuesta: (null)" -ForegroundColor Yellow
        }
        elseif ($resp -is [System.Array] -and $resp.Count -eq 0) {
            Write-Host "Respuesta: Array vacío []" -ForegroundColor Yellow
        }
        elseif ($resp -is [System.Array]) {
            Write-Host "Respuesta: Array con $($resp.Count) elementos" -ForegroundColor Cyan
            try {
                $jsonOutput = ($resp | ConvertTo-Json -Depth 8 -Compress:$false)
                Write-Host $jsonOutput
            } catch {
                Write-Host $resp
            }
        }
        else {
            try {
                $jsonOutput = ($resp | ConvertTo-Json -Depth 8 -Compress:$false)
                Write-Host $jsonOutput
            } catch {
                Write-Host $resp
            }
        }
    }
    catch {
        $statusCode = $null
        try { $statusCode = $_.Exception.Response.StatusCode.value__ } catch {}

        if ($statusCode) {
            Write-Host "ERROR (Status: $statusCode)" -ForegroundColor Red
        } else {
            Write-Host "ERROR" -ForegroundColor Red
        }

        $msg = $_.Exception.Message
        Write-Host "Mensaje: $msg" -ForegroundColor Red

        if ($statusCode -eq 401) {
            Write-Host "No autorizado. El Proxy no pudo autenticarse con la cátedra." -ForegroundColor Yellow
            Write-Host "Verifica: GET /actuator/auth/status" -ForegroundColor Yellow
        }
        if ($statusCode -eq 404) {
            Write-Host "Endpoint no encontrado. Verifica la URL." -ForegroundColor Yellow
        }
        if ($statusCode -eq 500) {
            Write-Host "Error interno del servidor. Revisa los logs del Proxy." -ForegroundColor Yellow
        }
    }

    Write-Host ""; Write-Host "Presiona Enter para continuar..." -ForegroundColor Gray
    [void](Read-Host)
}

# Preparar payloads
Ensure-Dir -Path $PayloadDir
Ensure-PayloadFile -FilePath (Join-Path $PayloadDir 'proxy-login.json') -DefaultJson '{
  "username": "templay333",
  "password": "B0lud0t0t4l"
}'
Ensure-PayloadFile -FilePath (Join-Path $PayloadDir 'proxy-bloquear-asientos.json') -DefaultJson '{
  "eventoId": 1,
  "asientos": [
    {"fila": 1, "columna": 3},
    {"fila": 1, "columna": 4}
  ]
}'
Ensure-PayloadFile -FilePath (Join-Path $PayloadDir 'proxy-realizar-venta.json') -DefaultJson '{
  "eventoId": 1,
  "asientos": [
    {"fila": 1, "columna": 3}
  ],
  "username": "templay333"
}'

# Loop principal
do {
    Clear-Host
    Show-Menu
    $option = Read-Host "Selecciona una opcion"

    switch ($option) {
        '1' {
            $url = "$PROXY_URL$API_BASE/users/login"
            $file = Join-Path $PayloadDir 'proxy-login.json'
            $body = Load-Or-Edit-Payload -FilePath $file
            Invoke-ProxyRequest -Url $url -Method 'Post' -Body $body -Description 'Login de usuario'
        }
        '2' {
            $url = "$PROXY_URL$API_BASE/eventos/resumidos"
            Invoke-ProxyRequest -Url $url -Method 'Get' -Description 'Listado eventos resumidos'
        }
        '3' {
            $url = "$PROXY_URL$API_BASE/eventos"
            Invoke-ProxyRequest -Url $url -Method 'Get' -Description 'Listado eventos completos'
        }
        '4' {
            $id = Read-Host 'Ingresa el ID del evento'
            $url = "$PROXY_URL$API_BASE/eventos/$id"
            Invoke-ProxyRequest -Url $url -Method 'Get' -Description "Datos completos del evento $id"
        }
        '5' {
            $id = Read-Host 'Ingresa el ID del evento'
            $url = "$PROXY_URL$API_BASE/eventos/$id/asientos-estado"
            Invoke-ProxyRequest -Url $url -Method 'Get' -Description "Estado de asientos del evento $id (desde Redis)"
        }
        '6' {
            $id = Read-Host 'Ingresa el ID del evento'
            $url = "$PROXY_URL$API_BASE/eventos/$id/bloquear-asientos"
            $file = Join-Path $PayloadDir 'proxy-bloquear-asientos.json'
            $body = Load-Or-Edit-Payload -FilePath $file
            Invoke-ProxyRequest -Url $url -Method 'Post' -Body $body -Description 'Bloquear asientos'
        }
        '7' {
            $url = "$PROXY_URL$API_BASE/ventas/realizar"
            $file = Join-Path $PayloadDir 'proxy-realizar-venta.json'
            $body = Load-Or-Edit-Payload -FilePath $file
            Invoke-ProxyRequest -Url $url -Method 'Post' -Body $body -Description 'Realizar venta'
        }
        '8' {
            $url = "$PROXY_URL$API_BASE/ventas"
            Invoke-ProxyRequest -Url $url -Method 'Get' -Description 'Listado completo de ventas'
        }
        '9' {
            $id = Read-Host 'Ingresa el ID de la venta'
            $url = "$PROXY_URL$API_BASE/ventas/$id"
            Invoke-ProxyRequest -Url $url -Method 'Get' -Description "Datos de la venta $id"
        }
        'A' {
            $url = "$PROXY_URL/actuator/health"
            Invoke-ProxyRequest -Url $url -Method 'Get' -Description 'Health check del Proxy'
        }
        'B' {
            $url = "$PROXY_URL/actuator/auth/status"
            Invoke-ProxyRequest -Url $url -Method 'Get' -Description 'Estado del token JWT'
        }
        'C' {
            $url = "$PROXY_URL/actuator/auth/refresh"
            Invoke-ProxyRequest -Url $url -Method 'Post' -Description 'Renovar token JWT'
        }
        '0' {
            Write-Host "Saliendo..." -ForegroundColor Gray
        }
        default {
            Write-Host "Opcion invalida" -ForegroundColor Red
            Start-Sleep -Seconds 1
        }
    }
}
while ($option -ne '0')

