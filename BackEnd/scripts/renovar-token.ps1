# Script para renovar el token JWT automáticamente
# Lee credenciales desde .env (ubicado en BackEnd o en la raíz del repo)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "RENOVAR TOKEN JWT" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Ubicaciones
$BackEndRoot = (Resolve-Path (Join-Path $PSScriptRoot ".."))
$RepoRoot    = (Resolve-Path (Join-Path $BackEndRoot ".."))

# Resolver .env en BackEnd primero, luego en la raíz del repo
$EnvCandidates = @(
    (Join-Path $BackEndRoot ".env"),
    (Join-Path $RepoRoot ".env")
)
$EnvPath = $null
foreach ($candidate in $EnvCandidates) {
    if (Test-Path $candidate) { $EnvPath = $candidate; break }
}

function Load-EnvFile {
    param([string]$Path)
    $envVars = @{}
    if (Test-Path $Path) {
        Get-Content $Path | ForEach-Object {
            if ($_ -match '^\s*([^#][^=]*?)\s*=\s*(.*?)\s*$') { $envVars[$matches[1]] = $matches[2] }
        }
    } else {
        Write-Host "ERROR: No se encontró .env en: $Path" -ForegroundColor Red
        exit 1
    }
    return $envVars
}

if (-not $EnvPath) {
    Write-Host "ERROR: No se encontró archivo .env" -ForegroundColor Red
    Write-Host "Rutas probadas:" -ForegroundColor Yellow
    $EnvCandidates | ForEach-Object { Write-Host " - $_" -ForegroundColor Gray }
    exit 1
}

$envVars = Load-EnvFile -Path $EnvPath

if (-not $envVars.USERNAME -or -not $envVars.PASSWORD) {
    Write-Host "ERROR: Faltan USERNAME o PASSWORD en .env" -ForegroundColor Red
    exit 1
}

$serverUrl = $envVars.SERVER_URL; if ([string]::IsNullOrWhiteSpace($serverUrl)) { $serverUrl = "http://localhost:8080" }
$apiBase   = $envVars.API_BASE;   if ([string]::IsNullOrWhiteSpace($apiBase))   { $apiBase   = "/api" }

Write-Host "Usando .env: $EnvPath" -ForegroundColor Gray
Write-Host "Usuario: $($envVars.USERNAME)" -ForegroundColor Yellow
Write-Host "Servidor: $serverUrl" -ForegroundColor Yellow
Write-Host ""

# Body de login
$loginBody = @{ username = $envVars.USERNAME; password = $envVars.PASSWORD; rememberMe = $false } | ConvertTo-Json

Write-Host "Solicitando nuevo token..." -ForegroundColor Gray

try {
    $authParams = @{ Uri = "$serverUrl$apiBase/authenticate"; Method = 'Post'; Body = $loginBody; ContentType = 'application/json'; ErrorAction = 'Stop' }
    $response = Invoke-RestMethod @authParams
    $newToken = $response.id_token
    if ([string]::IsNullOrWhiteSpace($newToken)) { throw "La respuesta no contiene id_token" }

    Write-Host ""; Write-Host "✓ Token obtenido exitosamente" -ForegroundColor Green; Write-Host ""

    if ($envVars.JWT_TOKEN) {
        $prev = $envVars.JWT_TOKEN; $prevShort = $prev.Substring(0, [Math]::Min(50, $prev.Length))
        Write-Host "Token anterior (primeros 50): $prevShort..." -ForegroundColor Yellow
    }
    $newShort = $newToken.Substring(0, [Math]::Min(50, $newToken.Length))
    Write-Host "Token nuevo (primeros 50): $newShort..." -ForegroundColor Green
    Write-Host ""

    # Actualizar .env (reemplazo seguro de la línea JWT_TOKEN, agregando si no existe)
    Write-Host "Actualizando archivo .env..." -ForegroundColor Gray
    $lines = @(); if (Test-Path $EnvPath) { $lines = Get-Content $EnvPath }
    $found = $false
    for ($i = 0; $i -lt $lines.Count; $i++) { if ($lines[$i] -match '^\s*JWT_TOKEN\s*=') { $lines[$i] = "JWT_TOKEN=$newToken"; $found = $true; break } }
    if (-not $found) { $lines += "JWT_TOKEN=$newToken" }
    Set-Content -Path $EnvPath -Value ($lines -join "`r`n") -NoNewline

    Write-Host "✓ Archivo .env actualizado" -ForegroundColor Green
    Write-Host ""

    # Verificación básica del token
    Write-Host "Verificando nuevo token..." -ForegroundColor Gray
    $headers = @{ Authorization = "Bearer $newToken" }
    try {
        $acctParams = @{ Uri = "$serverUrl$apiBase/account"; Headers = $headers; ErrorAction = 'Stop' }
        $testResponse = Invoke-RestMethod @acctParams
        Write-Host "✓ Token verificado correctamente" -ForegroundColor Green
        Write-Host ""; Write-Host "Información del usuario:" -ForegroundColor Yellow
        Write-Host ("  Login: {0}" -f $testResponse.login)
        Write-Host ("  Email: {0}" -f $testResponse.email)
        Write-Host ("  Nombre: {0} {1}" -f $testResponse.firstName, $testResponse.lastName)
    } catch { Write-Host "Aviso: no se pudo verificar el token en /account" -ForegroundColor Yellow }

    Write-Host ""; Write-Host "========================================" -ForegroundColor Green
    Write-Host "TOKEN RENOVADO EXITOSAMENTE" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "El nuevo token ya está guardado en .env" -ForegroundColor White
}
catch {
    Write-Host ""; Write-Host "========================================" -ForegroundColor Red
    Write-Host "ERROR AL RENOVAR TOKEN" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    $statusCode = $null; try { $statusCode = $_.Exception.Response.StatusCode.value__ } catch {}
    if ($statusCode -eq 401) {
        Write-Host "Credenciales incorrectas (401)" -ForegroundColor Red
        Write-Host "Verifica USERNAME y PASSWORD en .env" -ForegroundColor Yellow
    } else {
        Write-Host ("Error: {0}" -f $_.Exception.Message) -ForegroundColor Red
        Write-Host "Posibles causas: servidor caído, sin red o endpoint cambiado" -ForegroundColor Yellow
    }
    exit 1
}

Write-Host ""; Write-Host "Presiona Enter para continuar..." -ForegroundColor Gray
[void](Read-Host)
