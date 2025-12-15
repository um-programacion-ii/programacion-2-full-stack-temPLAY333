# Script para eliminar el constraint unico incorrecto de evento.evento_tipo_id
# Este constraint impide que multiples eventos compartan el mismo tipo (incorrecto para relacion Many-to-One)
# Ejecutar: .\scripts\fix-constraint-unique-evento.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  ELIMINAR CONSTRAINT UNICO INCORRECTO" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar si el contenedor esta corriendo
$container = docker ps --filter "name=microservices-mysql-dev" --format "{{.Names}}"

if ($container -ne "microservices-mysql-dev") {
    Write-Host "ERROR: El contenedor microservices-mysql-dev no esta corriendo" -ForegroundColor Red
    Write-Host "Ejecuta: docker compose up -d mysql-dev" -ForegroundColor Yellow
    exit 1
}

Write-Host "Conectando a: microservices-mysql-dev (puerto 3306)" -ForegroundColor Yellow
Write-Host ""

# Verificar si el constraint existe
Write-Host "1. Verificando si el constraint existe..." -ForegroundColor Green
$sqlQuery = "SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA = 'MicroservicesFinal' AND TABLE_NAME = 'evento' AND CONSTRAINT_NAME = 'ux_evento__evento_tipo_id';"
$result = docker exec microservices-mysql-dev mysql -u root -prootpass -N -e $sqlQuery MicroservicesFinal 2>&1 | Where-Object { $_ -notmatch "Warning" -and $_ -notmatch "insecure" }

$constraintCount = 0
if ($result) {
    $trimmed = $result.ToString().Trim()
    $parsed = 0
    if ([int]::TryParse($trimmed, [ref]$parsed)) {
        $constraintCount = $parsed
    }
}

# Tambien verificar si el indice unico existe
if ($constraintCount -eq 0) {
    $indexQuery = "SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = 'MicroservicesFinal' AND TABLE_NAME = 'evento' AND INDEX_NAME = 'ux_evento__evento_tipo_id' AND NON_UNIQUE = 0;"
    $indexResult = docker exec microservices-mysql-dev mysql -u root -prootpass -N -e $indexQuery MicroservicesFinal 2>&1 | Where-Object { $_ -notmatch "Warning" -and $_ -notmatch "insecure" }
    if ($indexResult) {
        $trimmed = $indexResult.ToString().Trim()
        $parsed = 0
        if ([int]::TryParse($trimmed, [ref]$parsed) -and $parsed -gt 0) {
            $constraintCount = $parsed
        }
    }
}

if ($constraintCount -eq 0) {
    Write-Host "[OK] El constraint unico ya no existe. No es necesario eliminarlo." -ForegroundColor Green
    exit 0
}

Write-Host "  El constraint existe (encontrado: $constraintCount)" -ForegroundColor Yellow
Write-Host ""

# Paso 1: Buscar y eliminar la foreign key constraint primero
Write-Host "2. Buscando foreign key constraint relacionada..." -ForegroundColor Green
$fkQuery = "SELECT CONSTRAINT_NAME FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA = 'MicroservicesFinal' AND TABLE_NAME = 'evento' AND CONSTRAINT_TYPE = 'FOREIGN KEY' AND CONSTRAINT_NAME LIKE '%evento_tipo%';"
$fkResult = docker exec microservices-mysql-dev mysql -u root -prootpass -N -e $fkQuery MicroservicesFinal 2>&1

$fkName = ""
if ($fkResult) {
    $fkName = $fkResult.ToString().Trim()
}

if ($fkName -and $fkName -ne "" -and -not $fkName.StartsWith("ERROR")) {
    Write-Host "  Encontrada foreign key: $fkName" -ForegroundColor Yellow
    Write-Host "  Eliminando foreign key constraint..." -ForegroundColor Green

    $dropFkQuery = "ALTER TABLE evento DROP FOREIGN KEY $fkName;"
    $dropFkResult = docker exec microservices-mysql-dev mysql -u root -prootpass -e $dropFkQuery MicroservicesFinal 2>&1

    $hasError = $false
    if ($dropFkResult) {
        $hasError = $dropFkResult.ToString() -match "ERROR"
    }

    if ($LASTEXITCODE -eq 0 -and -not $hasError) {
        Write-Host "[OK] Foreign key eliminada correctamente" -ForegroundColor Green
    } else {
        Write-Host "[WARNING] Error al eliminar la foreign key: $dropFkResult" -ForegroundColor Yellow
    }
} else {
    Write-Host "  No se encontro foreign key constraint relacionada (puede que ya no exista)" -ForegroundColor Yellow
}

# Paso 2: Eliminar el indice unico
Write-Host ""
Write-Host "3. Eliminando indice unico 'ux_evento__evento_tipo_id'..." -ForegroundColor Green

$dropIndexQuery = "ALTER TABLE evento DROP INDEX ux_evento__evento_tipo_id;"
$dropIndexResult = docker exec microservices-mysql-dev mysql -u root -prootpass -e $dropIndexQuery MicroservicesFinal 2>&1

$hasError = $false
if ($dropIndexResult) {
    $hasError = $dropIndexResult.ToString() -match "ERROR"
}

if ($LASTEXITCODE -eq 0 -and -not $hasError) {
    Write-Host "[OK] Indice unico eliminado correctamente" -ForegroundColor Green
} else {
    Write-Host "[WARNING] Error al eliminar el indice unico. Intentando con DROP INDEX..." -ForegroundColor Yellow

    $dropIndexQuery2 = "DROP INDEX ux_evento__evento_tipo_id ON evento;"
    $dropIndexResult2 = docker exec microservices-mysql-dev mysql -u root -prootpass -e $dropIndexQuery2 MicroservicesFinal 2>&1

    $hasError2 = $false
    if ($dropIndexResult2) {
        $hasError2 = $dropIndexResult2.ToString() -match "ERROR"
    }

    if ($LASTEXITCODE -eq 0 -and -not $hasError2) {
        Write-Host "[OK] Indice unico eliminado correctamente (segundo intento)" -ForegroundColor Green
    } else {
        Write-Host "[ERROR] Error al eliminar el indice unico" -ForegroundColor Red
        Write-Host "  Detalles: $dropIndexResult2" -ForegroundColor Red
        Write-Host ""
        Write-Host "Para verificar manualmente, ejecuta:" -ForegroundColor Yellow
        Write-Host "  docker exec -it microservices-mysql-dev mysql -u root -prootpass MicroservicesFinal" -ForegroundColor White
        Write-Host "  SHOW INDEX FROM evento;" -ForegroundColor White
        exit 1
    }
}

# Paso 3: Recrear la foreign key constraint (sin unique) solo si fue eliminada
Write-Host ""
if ($fkName -and $fkName -ne "" -and -not $fkName.StartsWith("ERROR")) {
    Write-Host "4. Recreando foreign key constraint (sin unique)..." -ForegroundColor Green

    $addFkQuery = "ALTER TABLE evento ADD CONSTRAINT fk_evento__evento_tipo_id FOREIGN KEY (evento_tipo_id) REFERENCES evento_tipo(id);"
    $addFkResult = docker exec microservices-mysql-dev mysql -u root -prootpass -e $addFkQuery MicroservicesFinal 2>&1

    $hasError3 = $false
    if ($addFkResult) {
        $hasError3 = $addFkResult.ToString() -match "ERROR"
    }

    if ($LASTEXITCODE -eq 0 -and -not $hasError3) {
        Write-Host "[OK] Foreign key recreada correctamente (sin unique)" -ForegroundColor Green
    } else {
        Write-Host "[WARNING] No se pudo recrear la foreign key (puede que ya exista): $addFkResult" -ForegroundColor Yellow
    }
} else {
    Write-Host "4. Saltando recreacion de foreign key (no fue eliminada)" -ForegroundColor Yellow
}

# Verificar resultado
Write-Host ""
Write-Host "5. Verificando resultado..." -ForegroundColor Green
$verifyQuery = "SELECT CONSTRAINT_NAME, CONSTRAINT_TYPE FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA = 'MicroservicesFinal' AND TABLE_NAME = 'evento' AND CONSTRAINT_NAME LIKE '%evento_tipo%';"
$verifyOutput = docker exec microservices-mysql-dev mysql -u root -prootpass -e $verifyQuery MicroservicesFinal 2>&1 | Where-Object { $_ -notmatch "Warning" -and $_ -notmatch "insecure" }
if ($verifyOutput) {
    Write-Host $verifyOutput
} else {
    Write-Host "  No se encontraron constraints relacionados con evento_tipo" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "[OK] Proceso completado" -ForegroundColor Green
Write-Host "Ahora multiples eventos pueden compartir el mismo evento_tipo_id" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
