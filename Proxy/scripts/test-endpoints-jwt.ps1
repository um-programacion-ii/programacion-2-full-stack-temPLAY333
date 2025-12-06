# Script para probar endpoints protegidos con JWT
# Carga automáticamente el token desde .env (en la raíz del proyecto)

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

if (-not $SERVER_URL) { $SERVER_URL = "http://localhost:8080" }
if (-not $API_BASE)    { $API_BASE    = "/api" }

$API_V1 = "$SERVER_URL$API_BASE/endpoints/v1"

if (-not $JWT_TOKEN) {
    Write-Host "Aviso: JWT_TOKEN no encontrado en .env. Podrás hacer login (opción 2) pero el resto puede fallar con 401." -ForegroundColor Yellow
}

$jsonContent = 'application/json'
# Construir headers compatibles con PS 5.1 (sin if inline)
$headersAuth = @{ "Content-Type" = $jsonContent }
if ($JWT_TOKEN) { $headersAuth["Authorization"] = "Bearer $JWT_TOKEN" }
$headersNoAuth = @{ "Content-Type" = $jsonContent }

function Show-Menu {
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "PRUEBA DE ENDPOINTS" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Servidor: $SERVER_URL" -ForegroundColor Yellow
    Write-Host "API Base: $API_BASE" -ForegroundColor Yellow
    Write-Host "Token cargado: " -NoNewline; if ($JWT_TOKEN) { Write-Host "SI" -ForegroundColor Green } else { Write-Host "NO" -ForegroundColor Red }
    Write-Host ""
    Write-Host "1. Ver mi cuenta (GET $API_BASE/account) [sin payload]" -ForegroundColor White
    Write-Host "2. Login de usuario (POST $API_BASE/authenticate) [Payload 2]" -ForegroundColor White
    Write-Host "3. Listado eventos resumidos (GET $API_BASE/endpoints/v1/eventos-resumidos) [Payload 3]" -ForegroundColor White
    Write-Host "4. Listado eventos completos (GET $API_BASE/endpoints/v1/eventos) [Payload 4]" -ForegroundColor White
    Write-Host "5. Ver evento por ID (GET $API_BASE/endpoints/v1/evento/{id}) [Payload 5]" -ForegroundColor White
    Write-Host "6. Bloquear asientos (POST $API_BASE/endpoints/v1/bloquear-asientos) [Payload 6]" -ForegroundColor White
    Write-Host "7. Realizar venta (POST $API_BASE/endpoints/v1/realizar-venta) [Payload 7]" -ForegroundColor White
    Write-Host "8. Listar ventas (GET $API_BASE/endpoints/v1/listar-ventas) [Payload 8]" -ForegroundColor White
    Write-Host "9. Ver venta por ID (GET $API_BASE/endpoints/v1/listar-venta/{id}) [Payload 9]" -ForegroundColor White
    Write-Host "0. Salir" -ForegroundColor Gray
    Write-Host ""
}

function Invoke-JsonRequest {
    param(
        [string]$Url,
        [string]$Method = 'Get',
        [bool]$UseAuth = $true,
        [string]$Body = $null,
        [string]$Description = ''
    )

    Write-Host ""; Write-Host ">>> $Description" -ForegroundColor Cyan
    Write-Host ("    {0} {1}" -f $Method, $Url) -ForegroundColor Gray

    $useHeaders = $headersNoAuth
    if ($UseAuth) { $useHeaders = $headersAuth }

    $params = @{ Uri = $Url; Method = $Method; Headers = $useHeaders; ErrorAction = 'Stop' }
    if ($Body) { $params.Body = $Body }

    try {
        $resp = Invoke-RestMethod @params
        Write-Host "EXITO" -ForegroundColor Green
        try { ($resp | ConvertTo-Json -Depth 8) | Write-Host } catch { Write-Host $resp }
    }
    catch {
        $statusCode = $null; try { $statusCode = $_.Exception.Response.StatusCode.value__ } catch {}
        if ($statusCode) { Write-Host "ERROR (Status: $statusCode)" -ForegroundColor Red } else { Write-Host "ERROR" -ForegroundColor Red }
        $msg = $_.Exception.Message; Write-Host "Mensaje: $msg" -ForegroundColor Red
        if ($statusCode -eq 401) { Write-Host "Token invalido o expirado" -ForegroundColor Yellow }
        if ($statusCode -eq 403) { Write-Host "Sin permisos para este recurso" -ForegroundColor Yellow }
    }

    Write-Host ""; Write-Host "Presiona Enter para continuar..." -ForegroundColor Gray
    [void](Read-Host)
}

# Preparar payloads por defecto
Ensure-Dir -Path $PayloadDir
Ensure-PayloadFile -FilePath (Join-Path $PayloadDir 'payload-2-login.json') -DefaultJson '{
  "username": "",
  "password": "",
  "rememberMe": false
}'
Ensure-PayloadFile -FilePath (Join-Path $PayloadDir 'payload-6-bloquear-asientos.json') -DefaultJson '{
  "eventoId": 0,
  "asientos": [
    "A1",
    "A2"
  ],
  "motivo": "",
  "minutosBloqueo": 15
}'
Ensure-PayloadFile -FilePath (Join-Path $PayloadDir 'payload-7-realizar-venta.json') -DefaultJson '{
  "eventoId": 0,
  "asientos": [
    "A1",
    "A2"
  ],
  "medioPago": "",
  "datosComprador": {
    "nombre": "",
    "email": ""
  }
}'

# Loop
do {
    Clear-Host
    Show-Menu
    $option = Read-Host "Selecciona una opcion"

    switch ($option) {
        '1' {
            $url = "$SERVER_URL$API_BASE/account"
            Invoke-JsonRequest -Url $url -Method 'Get' -UseAuth:$true -Description 'Ver mi cuenta (/account)'
        }
        '2' {
            $url = "$SERVER_URL$API_BASE/authenticate"
            $file = Join-Path $PayloadDir 'payload-2-login.json'
            $body = Load-Or-Edit-Payload -FilePath $file
            Invoke-JsonRequest -Url $url -Method 'Post' -UseAuth:$false -Body $body -Description 'Login de usuario'
        }
        '3' {
            $url = "$API_V1/eventos-resumidos"
            Invoke-JsonRequest -Url $url -Method 'Get' -UseAuth:$true -Description 'Listado eventos resumidos'
        }
        '4' {
            $url = "$API_V1/eventos"
            Invoke-JsonRequest -Url $url -Method 'Get' -UseAuth:$true -Description 'Listado eventos completos'
        }
        '5' {
            $id = Read-Host 'Ingresa el ID del evento'
            $url = "$API_V1/evento/$id"
            Invoke-JsonRequest -Url $url -Method 'Get' -UseAuth:$true -Description "Datos completos del evento $id"
        }
        '6' {
            $url = "$API_V1/bloquear-asientos"
            $file = Join-Path $PayloadDir 'payload-6-bloquear-asientos.json'
            $body = Load-Or-Edit-Payload -FilePath $file
            Invoke-JsonRequest -Url $url -Method 'Post' -UseAuth:$true -Body $body -Description 'Bloqueo de asientos por evento'
        }
        '7' {
            $url = "$API_V1/realizar-venta"
            $file = Join-Path $PayloadDir 'payload-7-realizar-venta.json'
            $body = Load-Or-Edit-Payload -FilePath $file
            Invoke-JsonRequest -Url $url -Method 'Post' -UseAuth:$true -Body $body -Description 'Venta de asientos por evento'
        }
        '8' {
            $url = "$API_V1/listar-ventas"
            Invoke-JsonRequest -Url $url -Method 'Get' -UseAuth:$true -Description 'Listado completo de ventas por alumno'
        }
        '9' {
            $id = Read-Host 'Ingresa el ID de la venta'
            $url = "$API_V1/listar-venta/$id"
            Invoke-JsonRequest -Url $url -Method 'Get' -UseAuth:$true -Description "Datos de la venta $id"
        }
        '0' { Write-Host "Saliendo..." -ForegroundColor Gray }
        default { Write-Host "Opcion invalida" -ForegroundColor Red; Start-Sleep -Seconds 1 }
    }
}
while ($option -ne '0')
