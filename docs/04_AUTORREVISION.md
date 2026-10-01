# Autorrevisión técnica — Ae6

Revisión hecha antes de abrir el Pull Request sobre `git diff main..ae6/suite-pruebas`.

## 1. Checklist con evidencia

| # | Criterio | Estado | Evidencia / cómo se comprobó |
|---|---|---|---|
| 1 | Revisé *Files changed* línea por línea | ✅ | `git diff --stat main..HEAD`: 18 archivos. Solo pruebas, documentación, evidencias y scripts. |
| 2 | Código productivo sin cambios | ✅ | `git diff main..HEAD -- src/main` vacío. Ninguna prueba se hizo pasar modificando `src/main`. |
| 3 | No hay `target/` ni archivos generados | ✅ | `.gitignore` excluye `target/`. `git diff --name-only main..HEAD \| grep -E "target/\|\.class\|\.exec"` sin resultados. |
| 4 | No hay credenciales ni datos sensibles | ✅ | `git grep -i -E "password\|secret\|api_key"` sin resultados. |
| 5 | Nombres de prueba expresivos | ✅ | Métodos con nombre de comportamiento (`horarioNoDisponibleLanzaExcepcionYNoGuardaNiNotifica`) y `@DisplayName` con el ID del caso (`CP-16 · ...`). |
| 6 | Las aserciones comprueban comportamiento | ✅ | Cada prueba verifica valor, estado, excepción **y mensaje**, o efecto observable. 7/7 mutantes detectados (`docs/evidencias/experimentos/`). |
| 7 | Casos límite presentes | ✅ | R1: 1/**2**/3 h. R3: 0 y −0.01. R5: `""` y `"   "`. |
| 8 | Los mocks verifican interacciones relevantes | ✅ | Solo `guardar` y `enviarConfirmacion` (efectos de negocio). `DisponibilidadClient` es stub y no se verifica, salvo en CP-17 para asegurar que no se consulta con `null`. |
| 9 | Sin mocks innecesarios | ✅ | `Reserva` se usa real. En cancelación y descuentos los dobles no se configuran ni verifican; existen solo porque el constructor los exige. `MockitoExtension` usa *strict stubs*, que fallaría ante stubs sin usar. |
| 10 | La cobertura está interpretada | ✅ | `docs/02_ANALISIS_COBERTURA_PLANTILLA.md`: antes/después, huecos por línea/rama, huecos no visibles y prueba añadida por cada uno. |
| 11 | Los commits explican la evolución | ✅ | Prefijos `docs:`, `test:`, `refactor(test):`, `chore:`; un tema por commit; el cuerpo explica el porqué. |
| 12 | El PR indica cómo verificar | ✅ | Sección *Cómo verificar* con comandos y resultado esperado. |
| 13 | Ejecución desde cero | ✅ | `mvn clean test` → 26 pruebas, 0 fallos (`docs/evidencias/mvn-clean-test.txt`). |
| 14 | README actualizado | ✅ | Verificación, estructura de la suite, dobles de prueba y documentos. |

## 2. Problemas detectados y corregidos

| # | Problema | Riesgo | Corrección | Commit |
|---|---|---|---|---|
| A | La prueba base `entornoJUnitFunciona` solo hacía `assertTrue(true)`. | Prueba que siempre pasa: suma al conteo sin proteger nada e infla la suite. | Se eliminó. La suite real ya demuestra que JUnit funciona. | `refactor(test): eliminar prueba placeholder...` |
| B | Las pruebas parametrizadas no marcaban el bloque *Arrange*, a diferencia del resto. | Lectura AAA inconsistente. | Se añadió el comentario `// Arrange` indicando el origen de los datos (`@ValueSource` / `@CsvSource`). | mismo commit |
| C | La primera medición de JaCoCo mostró `Reserva` con 50 % de ramas y `cancelar()` al 0 %. | Validaciones del dominio nunca ejecutadas. | Se añadieron CP-21 a CP-24 (`ReservaTest`). | `test: cubrir validaciones de Reserva...` |
| D | `ReservaService` al 100 % sin probar tipo `null` ni fallas de colaboradores. | Falsa sensación de seguridad por el porcentaje. | Se añadieron CP-18, CP-19 y CP-20. | `test: agregar tipo nulo y fallas de colaboradores...` |

## 3. Decisiones revisadas y mantenidas

- **No verificar `disponibilidad.estaDisponible(...)` en CP-15.** El stub se programa con la reserva concreta (no `any()`), así que si el servicio consultara otra reserva la prueba fallaría igual. Añadir `verify` sería sobreespecificar.
- **`verifyNoMoreInteractions(repository, notificador)` en CP-15.** Se mantiene porque protege contra **guardados o correos duplicados**, que tienen impacto real para el cliente.
- **CP-20 caracteriza un comportamiento discutible** (reserva guardada sin aviso al cliente). No se cambió el código productivo porque la regla no define qué hacer. Se documenta como limitación en el PR.
- **Comparación de `double` con tolerancia `1e-9`**, para no depender del redondeo binario.

## 4. Pendientes fuera del alcance de Ae6

- Definir con el negocio la política ante fallas del notificador (reintento o compensación) y ante horas negativas en `puedeCancelar`.
- Migrar montos a `BigDecimal`.
- Incorporar pruebas de mutación automatizadas (PIT) en el `pom.xml`.
