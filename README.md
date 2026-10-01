# UEES UCOM0310 — Semana 7 — Ae6: suite de pruebas, cobertura y Pull Request

Proyecto Maven del módulo de reservas con la suite de pruebas de la actividad evaluada **Ae6**.
Rama de trabajo: `ae6/suite-pruebas` → Pull Request hacia `main`.

## Requisitos
- Java 21
- Maven 3.9+
- Git

## Cómo verificar

```bash
mvn clean test
```

Resultado esperado: **26 pruebas, 0 fallos, 0 errores** y `BUILD SUCCESS`.

En la fase `test` también se genera el reporte de cobertura de JaCoCo:

```
target/site/jacoco/index.html
```

Cobertura esperada: **100 %** de instrucciones, ramas, líneas y métodos (138/138 instrucciones, 18/18 ramas).

> Con Java 21 Mockito puede mostrar los avisos `Mockito is currently self-attaching...` y `A Java agent has been loaded dynamically`. Son esperados y no afectan el resultado.

### Generar la evidencia de ejecución

El script ejecuta `mvn clean test`, guarda la salida en `docs/evidencias/mvn-clean-test.txt` y la registra en un commit. Si alguna prueba falla, no hace commit.

```bash
# macOS / Linux / Git Bash
bash scripts/generar_evidencia.sh
```

```powershell
# Windows PowerShell
powershell -ExecutionPolicy Bypass -File scripts\generar_evidencia.ps1
```

## Estructura de la suite

| Archivo | Regla | Casos |
|---|---|---|
| `src/test/java/.../service/ReservaServiceTest.java` → `Cancelacion` | R1 · cancelar con ≥ 2 h | CP-01 a CP-06 |
| `src/test/java/.../service/ReservaServiceTest.java` → `CalculoTotal` | R2/R3 · descuentos y total válido | CP-07 a CP-14, CP-18 |
| `src/test/java/.../service/ReservaServiceTest.java` → `Confirmacion` | R4 · confirmar con Stub/Mock | CP-15 a CP-17, CP-19, CP-20 |
| `src/test/java/.../domain/ReservaTest.java` | R5 · integridad de `Reserva` | CP-21 a CP-24 |

Todas las pruebas siguen la estructura **AAA** (Arrange, Act, Assert) y su `@DisplayName` indica el ID del caso en la matriz.

**Dobles de prueba (solo en `confirmar`):**
- `DisponibilidadClient` → **Stub**: devuelve disponible / no disponible para fijar el escenario.
- `ReservaRepository` y `Notificador` → **Mock**: se verifica que se guarde y luego se notifique una sola vez, y que no ocurra cuando no corresponde.
- `Reserva` → objeto real (no se mockea el dominio).

## Documentación de Ae6

| Documento | Contenido |
|---|---|
| `docs/01_MATRIZ_CASOS_PLANTILLA.md` | Reglas R1-R5, técnicas de diseño y matriz de 24 casos |
| `docs/02_ANALISIS_COBERTURA_PLANTILLA.md` | Métricas JaCoCo antes/después, huecos, pruebas añadidas y mutación manual |
| `docs/03_PULL_REQUEST_PLANTILLA.md` | Descripción del Pull Request |
| `docs/04_AUTORREVISION.md` | Autorrevisión técnica y correcciones aplicadas |
| `docs/evidencias/` | Capturas y CSV de JaCoCo, experimentos y salida de `mvn clean test` |

## Regla de trabajo
No modifiques el código productivo solo para hacer pasar una prueba sin justificar el cambio.
Primero diseña el caso, luego implementa la prueba y finalmente interpreta el resultado.
En Ae6 **no se modificó el código productivo** (`src/main`). Los riesgos encontrados se documentan como limitaciones.
