# Script para probar el endpoint de registro de alumno
# POST {SERVER_URL}/api/v1/agregar_usuario

# Config
$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot ".."))
$EnvPath = Join-Path $ProjectRoot ".env"
$server = "http://192.168.194.250:8080"
if (Test-Path $EnvPath) {
    Get-Content $EnvPath | ForEach-Object {
        if ($_ -match '^\s*SERVER_URL\s*=\s*(.*?)\s*$') { $server = $matches[1] }
    }
}

$url = "$server/api/v1/agregar_usuario"

# Body de ejemplo provisto
$body = @{
    username = "juan"
    password = "juan123"
    firstName = "Juan"
    lastName = "Perez"
    email = "juan@perez.com.ar"
    nombreAlumno = "Juan Perez"
    descripcionProyecto = "Proyecto de Juan Perez"
} | ConvertTo-Json

$headers = @{ "Content-Type" = "application/json" }

Write-Host "=== Enviando petición a: $url ===" -ForegroundColor Cyan
Write-Host "Body:" -ForegroundColor Yellow
Write-Host $body
Write-Host ""

try {
    $resp = Invoke-RestMethod -Uri $url -Method Post -Body $body -Headers $headers -ErrorAction Stop
    Write-Host "=== RESPUESTA EXITOSA ===" -ForegroundColor Green
    ($resp | ConvertTo-Json -Depth 6) | Write-Host
}
catch {
    Write-Host "=== ERROR ===" -ForegroundColor Red
    $status = $null; try { $status = $_.Exception.Response.StatusCode.value__ } catch {}
    if ($status) { Write-Host "StatusCode: $status" -ForegroundColor Red }
    Write-Host ("Mensaje: {0}" -f $_.Exception.Message) -ForegroundColor Red
    try { $respBody = ($_ | Select-Object -ExpandProperty ErrorDetails).Message; if ($respBody) { Write-Host $respBody } } catch {}
}
