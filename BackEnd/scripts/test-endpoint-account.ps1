# Script para probar el endpoint /api/account
# Este endpoint devuelve los datos del usuario autenticado actual

param(
    [string]$Username = "admin",
    [string]$Password = "admin",
    [string]$BaseUrl = "http://localhost:8081"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Endpoint: GET /api/account" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Paso 1: Autenticarse y obtener el token JWT
Write-Host "[1/3] Autenticando como: $Username" -ForegroundColor Yellow

$loginBody = @{
    username = $Username
    password = $Password
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$BaseUrl/api/authenticate" `
        -Method POST `
        -ContentType "application/json" `
        -Body $loginBody `
        -ErrorAction Stop

    $token = $loginResponse.id_token

    if (-not $token) {
        Write-Host "[ERROR] No se recibio token en la respuesta" -ForegroundColor Red
        Write-Host "Respuesta completa:" -ForegroundColor Red
        $loginResponse | ConvertTo-Json -Depth 10
        exit 1
    }

    Write-Host "[OK] Token obtenido exitosamente" -ForegroundColor Green
    Write-Host "Token (primeros 50 caracteres): $($token.Substring(0, [Math]::Min(50, $token.Length)))..." -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "[ERROR] Error al autenticarse: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "Detalles: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
    exit 1
}

# Paso 2: Decodificar el JWT para ver qué usuario contiene
Write-Host "[2/3] Decodificando JWT para verificar usuario en el token..." -ForegroundColor Yellow

# El JWT tiene 3 partes separadas por puntos: header.payload.signature
$jwtParts = $token -split '\.'
if ($jwtParts.Length -ne 3) {
    Write-Host "[WARNING] JWT no tiene formato valido (3 partes)" -ForegroundColor Yellow
} else {
    # Decodificar el payload (segunda parte)
    $payloadBase64 = $jwtParts[1]

    # Agregar padding si es necesario (Base64 puede necesitar padding)
    $padding = $payloadBase64.Length % 4
    if ($padding -ne 0) {
        $payloadBase64 += "=" * (4 - $padding)
    }

    try {
        $payloadBytes = [System.Convert]::FromBase64String($payloadBase64)
        $payloadJson = [System.Text.Encoding]::UTF8.GetString($payloadBytes)
        $payload = $payloadJson | ConvertFrom-Json

        Write-Host "[OK] Payload del JWT:" -ForegroundColor Green
        Write-Host "  - sub (subject/login): $($payload.sub)" -ForegroundColor Cyan
        Write-Host "  - userId: $($payload.userId)" -ForegroundColor Cyan
        Write-Host "  - auth (authorities): $($payload.auth)" -ForegroundColor Cyan
        Write-Host "  - exp (expiration): $($payload.exp)" -ForegroundColor Cyan
        Write-Host ""

        if ($payload.sub -ne $Username) {
            Write-Host "[WARNING] El JWT contiene usuario '$($payload.sub)' pero se autentico como '$Username'" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "[WARNING] No se pudo decodificar el payload del JWT: $($_.Exception.Message)" -ForegroundColor Yellow
        Write-Host ""
    }
}

# Paso 3: Llamar al endpoint /api/account
Write-Host "[3/3] Llamando GET /api/account con el token..." -ForegroundColor Yellow

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

try {
    $accountResponse = Invoke-RestMethod -Uri "$BaseUrl/api/account" `
        -Method GET `
        -Headers $headers `
        -ErrorAction Stop

    Write-Host "[OK] Respuesta del endpoint /api/account:" -ForegroundColor Green
    Write-Host ""

    # Mostrar los datos del usuario
    Write-Host "Datos del usuario devueltos:" -ForegroundColor Cyan
    Write-Host "  - login: $($accountResponse.login)" -ForegroundColor White
    Write-Host "  - email: $($accountResponse.email)" -ForegroundColor White
    Write-Host "  - firstName: $($accountResponse.firstName)" -ForegroundColor White
    Write-Host "  - lastName: $($accountResponse.lastName)" -ForegroundColor White
    Write-Host "  - activated: $($accountResponse.activated)" -ForegroundColor White
    Write-Host "  - authorities: $($accountResponse.authorities -join ', ')" -ForegroundColor White
    Write-Host ""

    # Verificar si coincide con el usuario autenticado
    if ($accountResponse.login -ne $Username) {
        Write-Host "[ERROR] INCONSISTENCIA DETECTADA!" -ForegroundColor Red
        Write-Host "  - Se autentico como: $Username" -ForegroundColor Red
        Write-Host "  - JWT contiene (sub): $($payload.sub)" -ForegroundColor Red
        Write-Host "  - Endpoint devuelve usuario: $($accountResponse.login)" -ForegroundColor Red
        Write-Host ""
        Write-Host "Esto indica un problema en el backend:" -ForegroundColor Yellow
        Write-Host "  - El JWT puede tener el usuario incorrecto, O" -ForegroundColor Yellow
        Write-Host "  - SecurityUtils.getCurrentUserLogin() esta extrayendo mal el usuario del JWT, O" -ForegroundColor Yellow
        Write-Host "  - El repositorio esta devolviendo un usuario diferente" -ForegroundColor Yellow
    } else {
        Write-Host "[OK] Todo coincide correctamente:" -ForegroundColor Green
        Write-Host "  - Usuario autenticado: $Username" -ForegroundColor Green
        Write-Host "  - Usuario en JWT: $($payload.sub)" -ForegroundColor Green
        Write-Host "  - Usuario devuelto por /api/account: $($accountResponse.login)" -ForegroundColor Green
    }

    Write-Host ""
    Write-Host "Respuesta completa (JSON):" -ForegroundColor Cyan
    $accountResponse | ConvertTo-Json -Depth 10

} catch {
    Write-Host "[ERROR] Error al llamar /api/account: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "Detalles: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "Status Code: $statusCode" -ForegroundColor Red
    }
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test completado" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
