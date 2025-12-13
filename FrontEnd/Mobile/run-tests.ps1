#!/usr/bin/env pwsh
# Script para ejecutar tests y mostrar resumen

Write-Host "🧪 Ejecutando tests..." -ForegroundColor Cyan
Write-Host ""

$output = & .\gradlew test 2>&1 | Out-String

# Extraer resultado
if ($output -match "(\d+) tests completed, (\d+) failed") {
    $total = $matches[1]
    $failed = $matches[2]
    $passed = $total - $failed
    $percentage = [math]::Round(($passed / $total) * 100, 2)

    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor White
    Write-Host "📊 RESULTADO DE TESTS" -ForegroundColor Yellow
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor White
    Write-Host ""
    Write-Host "  Total:   $total tests" -ForegroundColor White
    Write-Host "  ✅ Pasan: $passed tests ($percentage%)" -ForegroundColor Green
    Write-Host "  ❌ Fallan: $failed tests" -ForegroundColor Red
    Write-Host ""
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor White

    if ($failed -eq 0) {
        Write-Host ""
        Write-Host "🎉 ¡Todos los tests pasan!" -ForegroundColor Green
        Write-Host ""
    } else {
        Write-Host ""
        Write-Host "⚠️  Algunos tests fallan. Ver reporte en:" -ForegroundColor Yellow
        Write-Host "   build/reports/tests/testDebugUnitTest/index.html" -ForegroundColor Gray
        Write-Host ""
    }
} elseif ($output -match "(\d+) tests completed") {
    $total = $matches[1]
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor White
    Write-Host "📊 RESULTADO DE TESTS" -ForegroundColor Yellow
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor White
    Write-Host ""
    Write-Host "  ✅ Todos los $total tests pasan!" -ForegroundColor Green
    Write-Host ""
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor White
    Write-Host ""
    Write-Host "🎉 ¡100% de éxito!" -ForegroundColor Green
    Write-Host ""
} else {
    Write-Host "❌ No se pudo determinar el resultado" -ForegroundColor Red
    Write-Host ""
    Write-Host "Ver output completo arriba" -ForegroundColor Gray
}

# Mostrar si BUILD SUCCESSFUL o FAILED
if ($output -match "BUILD SUCCESSFUL") {
    Write-Host "✅ BUILD SUCCESSFUL" -ForegroundColor Green
} elseif ($output -match "BUILD FAILED") {
    Write-Host "❌ BUILD FAILED" -ForegroundColor Red
}

Write-Host ""

