# run-app.ps1
# Script para compilar, instalar y ejecutar EventTickets
# Sin necesidad de abrir Android Studio

Write-Host "EventTickets - Build & Run" -ForegroundColor Cyan
Write-Host ""

$projectPath = "C:\Users\totob\IdeaProjects\Final\FrontEnd\Mobile"
$packageName = "com.eventtickets.mobile"
$adbPath = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"

# 1. Ir al proyecto
Set-Location $projectPath

# 2. Verificar que el emulador esté corriendo
Write-Host "[*] Verificando emulador..." -ForegroundColor Yellow
$devices = & $adbPath devices
if ($devices -match "device$") {
    Write-Host "[OK] Emulador detectado" -ForegroundColor Green
} else {
    Write-Host "[ERROR] No hay emulador corriendo" -ForegroundColor Red
    Write-Host ""
    Write-Host "   Inicia el emulador primero:" -ForegroundColor Yellow
    Write-Host "   1. Abre Android Studio" -ForegroundColor Gray
    Write-Host "   2. Tools -> Device Manager" -ForegroundColor Gray
    Write-Host "   3. Click en tu emulador" -ForegroundColor Gray
    Write-Host ""
    Write-Host "   O por comando:" -ForegroundColor Yellow
    Write-Host "   cd `$env:LOCALAPPDATA\Android\Sdk\emulator" -ForegroundColor Gray
    Write-Host "   .\emulator.exe -avd <nombre-avd>" -ForegroundColor Gray
    exit 1
}

# 3. Compilar
Write-Host ""
Write-Host "[*] Compilando..." -ForegroundColor Yellow
.\gradlew assembleDebug --quiet

if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] Compilacion exitosa" -ForegroundColor Green
} else {
    Write-Host "[ERROR] Error en compilacion" -ForegroundColor Red
    Write-Host ""
    Write-Host "   Revisa los errores arriba" -ForegroundColor Yellow
    Write-Host "   O ejecuta: .\gradlew assembleDebug" -ForegroundColor Gray
    exit 1
}

# 4. Instalar
Write-Host ""
Write-Host "[*] Instalando en emulador..." -ForegroundColor Yellow
.\gradlew installDebug --quiet

if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] Instalacion exitosa" -ForegroundColor Green
} else {
    Write-Host "[ERROR] Error en instalacion" -ForegroundColor Red
    exit 1
}

# 5. Ejecutar
Write-Host ""
Write-Host "[*] Iniciando app..." -ForegroundColor Yellow
& $adbPath shell am start -n "$packageName/.MainActivity" | Out-Null

Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "[OK] App iniciada correctamente!" -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Revisa el emulador" -ForegroundColor Yellow
Write-Host ""
Write-Host "Tips:" -ForegroundColor Cyan
Write-Host "   - Para ver logs: adb logcat *:E" -ForegroundColor Gray
Write-Host "   - Para reinstalar: .\run-app.ps1" -ForegroundColor Gray
Write-Host "   - Para limpiar: .\gradlew clean" -ForegroundColor Gray
Write-Host ""

