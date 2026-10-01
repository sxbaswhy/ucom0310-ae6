#!/usr/bin/env bash
# Ae6 · Ejecuta "mvn clean test" desde cero, guarda la salida como evidencia
# y la registra en un commit. Uso (macOS / Linux / Git Bash en Windows):
#   bash scripts/generar_evidencia.sh
set -uo pipefail
cd "$(dirname "$0")/.."

mkdir -p docs/evidencias/jacoco
mvn clean test 2>&1 | tee docs/evidencias/mvn-clean-test.txt
codigo=${PIPESTATUS[0]}

if [ "$codigo" -ne 0 ]; then
  echo ""
  echo "mvn clean test FALLO (codigo $codigo). Revisa docs/evidencias/mvn-clean-test.txt."
  echo "No se hizo commit. Corrige antes de generar la evidencia final."
  exit "$codigo"
fi

cp target/site/jacoco/jacoco.csv docs/evidencias/jacoco/jacoco_mvn.csv
git add docs/evidencias/mvn-clean-test.txt docs/evidencias/jacoco/jacoco_mvn.csv
git commit -m "docs: agregar evidencia de mvn clean test y reporte jacoco"

echo ""
echo "=== Evidencia generada ==="
grep -E "Tests run:|BUILD" docs/evidencias/mvn-clean-test.txt | tail -3
echo "Reporte HTML: target/site/jacoco/index.html"
echo ""
git log --oneline --decorate
