# Ae6 · Ejecuta "mvn clean test" desde cero, guarda la salida como evidencia
# y la registra en un commit. Uso en Windows (PowerShell):
#   powershell -ExecutionPolicy Bypass -File scripts\generar_evidencia.ps1
$ErrorActionPreference = "Continue"
Set-Location (Join-Path $PSScriptRoot "..")

New-Item -ItemType Directory -Force -Path "docs/evidencias/jacoco" | Out-Null
$salida = & mvn clean test 2>&1 | ForEach-Object { "$_" }
$codigo = $LASTEXITCODE
$salida | ForEach-Object { Write-Host $_ }
$salida | Out-File -FilePath "docs/evidencias/mvn-clean-test.txt" -Encoding utf8

if ($codigo -ne 0) {
    Write-Host ""
    Write-Host "mvn clean test FALLO (codigo $codigo). Revisa docs/evidencias/mvn-clean-test.txt."
    Write-Host "No se hizo commit. Corrige antes de generar la evidencia final."
    exit $codigo
}

Copy-Item "target/site/jacoco/jacoco.csv" "docs/evidencias/jacoco/jacoco_mvn.csv" -Force
git add docs/evidencias/mvn-clean-test.txt docs/evidencias/jacoco/jacoco_mvn.csv
git commit -m "docs: agregar evidencia de mvn clean test y reporte jacoco"

Write-Host ""
Write-Host "=== Evidencia generada ==="
Select-String -Path "docs/evidencias/mvn-clean-test.txt" -Pattern "Tests run:|BUILD" | Select-Object -Last 3 | ForEach-Object { Write-Host $_.Line }
Write-Host "Reporte HTML: target/site/jacoco/index.html"
Write-Host ""
git log --oneline --decorate
