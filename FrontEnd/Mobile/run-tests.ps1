#!/usr/bin/env pwsh
# Script para ejecutar tests y mostrar resumen

Write-Host "Ejecutando tests..." -ForegroundColor Cyan
Write-Host ""

# Ejecutar gradlew y capturar salida
$ErrorActionPreference = 'Continue'
.\gradlew test --console=plain 2>&1 | Tee-Object -Variable output | Out-Null

# Convertir array a string
$outputStr = $output -join "`n"

Write-Host ""
Write-Host "========================================" -ForegroundColor White
Write-Host "ANALIZANDO RESULTADOS..." -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor White
Write-Host ""

# Buscar en el archivo de reporte XML
$reportFile = "build\test-results\testDebugUnitTest\TEST-*.xml"
if (Test-Path $reportFile) {
    $xmlFiles = Get-ChildItem $reportFile
    $totalTests = 0
    $totalFailures = 0

    foreach ($file in $xmlFiles) {
        [xml]$xml = Get-Content $file
        if ($xml.testsuite) {
            $totalTests += [int]$xml.testsuite.tests
            $totalFailures += [int]$xml.testsuite.failures + [int]$xml.testsuite.errors
        }
    }

    if ($totalTests -gt 0) {
        $passed = $totalTests - $totalFailures
        $percentage = [math]::Round(($passed / $totalTests) * 100, 2)

        Write-Host "  Total:   $totalTests tests" -ForegroundColor White
        Write-Host "  Pasan:   $passed tests ($percentage%)" -ForegroundColor Green
        Write-Host "  Fallan:  $totalFailures tests" -ForegroundColor Red
        Write-Host ""
        Write-Host "========================================" -ForegroundColor White

        if ($totalFailures -eq 0) {
            Write-Host ""
            Write-Host "Todos los tests pasan!" -ForegroundColor Green
        } else {
            Write-Host ""
            Write-Host "Ver reporte detallado en:" -ForegroundColor Yellow
            Write-Host "  build\reports\tests\testDebugUnitTest\index.html" -ForegroundColor Gray
        }
    }
} else {
    # Fallback: buscar en output
    if ($outputStr -match "(\d+) tests completed, (\d+) failed") {
        $total = [int]$matches[1]
        $failed = [int]$matches[2]
        $passed = $total - $failed
        $percentage = [math]::Round(($passed / $total) * 100, 2)

        Write-Host "  Total:   $total tests" -ForegroundColor White
        Write-Host "  Pasan:   $passed tests ($percentage%)" -ForegroundColor Green
        Write-Host "  Fallan:  $failed tests" -ForegroundColor Red
    } elseif ($outputStr -match "(\d+) tests completed") {
        $total = [int]$matches[1]
        Write-Host "  Todos los $total tests pasan!" -ForegroundColor Green
    } else {
        Write-Host "No se encontraron resultados de tests." -ForegroundColor Yellow
        Write-Host "Los tests se ejecutaron pero no se pudo parsear el resultado." -ForegroundColor Gray
    }
}

Write-Host ""

# Mostrar estado del build
if ($outputStr -match "BUILD SUCCESSFUL") {
    Write-Host "BUILD SUCCESSFUL" -ForegroundColor Green
} elseif ($outputStr -match "BUILD FAILED") {
    Write-Host "BUILD FAILED" -ForegroundColor Red
}

Write-Host ""

