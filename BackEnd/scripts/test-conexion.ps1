# Script de diagnostico de conexion ZeroTier

Write-Host "========================================"
Write-Host "DIAGNOSTICO DE CONEXION ZEROTIER"
Write-Host "========================================"
Write-Host ""

# 1. ID de ZeroTier
Write-Host "[1] Tu ID de ZeroTier:"
try {
    $idLine = Get-Content "C:\ProgramData\ZeroTier\One\identity.public" -ErrorAction Stop | Select-Object -First 1
    $ztId = ($idLine -split ':')[0]
    Write-Host "    $ztId" -ForegroundColor Green
} catch {
    Write-Host "    No se pudo leer el ID (¿ZeroTier instalado?)" -ForegroundColor Yellow
}
Write-Host ""

# 2. Servicio ZeroTier
Write-Host "[2] Estado del servicio ZeroTier:"
$service = Get-Service -Name "ZeroTierOneService" -ErrorAction SilentlyContinue
if ($service) {
    if ($service.Status -eq 'Running') {
        Write-Host "    CORRIENDO" -ForegroundColor Green
    } else {
        Write-Host "    DETENIDO" -ForegroundColor Red
    }
} else {
    Write-Host "    NO ENCONTRADO" -ForegroundColor Red
}
Write-Host ""

# 3. Tus IPs
Write-Host "[3] Tus IPs actuales:"
Get-NetIPAddress -AddressFamily IPv4 | Where-Object {$_.IPAddress -ne "127.0.0.1"} | ForEach-Object {
    if ($_.IPAddress -like "192.168.194.*") {
        Write-Host "    $($_.IPAddress) - $($_.InterfaceAlias) [ZEROTIER]" -ForegroundColor Green
    } else {
        Write-Host "    $($_.IPAddress) - $($_.InterfaceAlias)"
    }
}
Write-Host ""

# 4. Adaptador ZeroTier
Write-Host "[4] Adaptador ZeroTier:"
$adapter = Get-NetAdapter | Where-Object {$_.InterfaceDescription -like "*ZeroTier*"}
if ($adapter) {
    Write-Host "    Nombre: $($adapter.Name)" -ForegroundColor Green
    Write-Host "    Estado: $($adapter.Status)" -ForegroundColor $(if($adapter.Status -eq 'Up'){'Green'}else{'Red'})

    $ztIP = Get-NetIPAddress -InterfaceAlias $adapter.Name -AddressFamily IPv4 -ErrorAction SilentlyContinue
    if ($ztIP) {
        Write-Host "    IP: $($ztIP.IPAddress)" -ForegroundColor Green
    } else {
        Write-Host "    IP: NO ASIGNADA (No autorizado en la red)" -ForegroundColor Yellow
    }
} else {
    Write-Host "    NO ENCONTRADO" -ForegroundColor Red
}
Write-Host ""

# 5. Ping al servidor
Write-Host "[5] Ping a 192.168.194.250:"
$ping = Test-Connection -ComputerName 192.168.194.250 -Count 1 -Quiet -ErrorAction SilentlyContinue
if ($ping) {
    Write-Host "    EXITOSO" -ForegroundColor Green
} else {
    Write-Host "    FALLO" -ForegroundColor Red
}
Write-Host ""

# 6. Puerto 8080
Write-Host "[6] Puerto 8080:"
$port = Test-NetConnection -ComputerName 192.168.194.250 -Port 8080 -WarningAction SilentlyContinue -InformationLevel Quiet
if ($port) {
    Write-Host "    ACCESIBLE" -ForegroundColor Green
} else {
    Write-Host "    NO ACCESIBLE" -ForegroundColor Red
}
Write-Host ""

# 7. Resumen
Write-Host "========================================"
Write-Host "RESUMEN"
Write-Host "========================================"

if (-not $adapter) {
    Write-Host "PROBLEMA: ZeroTier no esta instalado o no tiene adaptador" -ForegroundColor Red
} elseif ($adapter.Status -ne 'Up') {
    Write-Host "PROBLEMA: Adaptador ZeroTier deshabilitado" -ForegroundColor Red
} elseif (-not $ztIP) {
    Write-Host "PROBLEMA: No tienes IP en ZeroTier" -ForegroundColor Yellow
    Write-Host "  -> Debes ser autorizado en la red por el admin" -ForegroundColor Yellow
    Write-Host "  -> Tu ID: $ztId" -ForegroundColor Yellow
} elseif (-not $ping) {
    Write-Host "PROBLEMA: No hay conexion con el servidor" -ForegroundColor Yellow
    Write-Host "  -> El servidor puede estar apagado" -ForegroundColor Yellow
    Write-Host "  -> O el firewall bloquea la conexion" -ForegroundColor Yellow
} elseif (-not $port) {
    Write-Host "PROBLEMA: Puerto 8080 no accesible" -ForegroundColor Yellow
    Write-Host "  -> El servidor Spring Boot no esta corriendo" -ForegroundColor Yellow
} else {
    Write-Host "TODO OK: Puedes usar el endpoint" -ForegroundColor Green
}

Write-Host ""
