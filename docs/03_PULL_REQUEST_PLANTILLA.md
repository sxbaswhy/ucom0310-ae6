# Pull Request — Ae6

**Base:** `main` · **Compare:** `ae6/suite-pruebas`

**Título:**

```
test: completar suite, cobertura y evidencia de Ae6
```

**Descripción** (copiar desde aquí en GitHub):

---

## Objetivo

Proteger con pruebas automatizadas las reglas críticas del módulo de reservas (`ReservaService` y `Reserva`): cancelación con anticipación mínima, descuentos por tipo de cliente, validación del total, confirmación con servicios externos e integridad del dominio. Se usa una matriz de casos, JUnit 5, Stub/Mock y el análisis de cobertura de JaCoCo.

**No se modificó el código productivo** (`src/main` sin cambios).

## Cambios realizados

- `docs/01_MATRIZ_CASOS_PLANTILLA.md`: reglas R1-R5 y matriz de **24 casos** con tipo, riesgo y justificación (17 de diseño inicial + 7 incorporados tras el análisis de JaCoCo).
- `ReservaServiceTest`: suite organizada con `@Nested` por regla (`Cancelacion`, `CalculoTotal`, `Confirmacion`), estructura AAA, `@DisplayName` con el ID del caso y pruebas parametrizadas para límites y descuentos.
- `ReservaTest` (nuevo): validaciones del constructor de `Reserva` y transición a `CANCELADA`.
- Stub/Mock con Mockito **solo en `confirmar`**, que es el único método con dependencias externas.
- `docs/02_ANALISIS_COBERTURA_PLANTILLA.md` + `docs/evidencias/`: métricas antes y después, capturas, CSV y experimentos de mutación.
- `docs/04_AUTORREVISION.md`, `README.md` actualizado y `scripts/generar_evidencia.(sh|ps1)`.
- Se eliminó la prueba placeholder `entornoJUnitFunciona` (`assertTrue(true)`) porque no verificaba ningún comportamiento.

## Casos de prueba incorporados

| Área | Casos | Qué se verifica |
|---|---|---|
| Cancelación (R1) | CP-01 a CP-06 | 5 h ✔, **límite 2 h ✔**, **1 h ✘**, 3 h ✔, 0 h ✘, −1 h ✘ |
| Descuentos y total (R2/R3) | CP-07 a CP-14, CP-18 | NORMAL 100, VIP 85, ESTUDIANTE 90, minúsculas, tipo desconocido, tipo `null`, total 0 válido, −1 y −0.01 → `IllegalArgumentException` |
| Confirmación (R4) | CP-15 a CP-17, CP-19, CP-20 | disponible → `CONFIRMADA` + guardar→notificar; no disponible → `IllegalStateException` sin efectos; `null` → `IllegalArgumentException` sin llamar servicios; fallas del repositorio y del notificador |
| Dominio (R5) | CP-21 a CP-24 | id `null`/`""`/`"   "` rechazado, tipo `null` → NORMAL, estado inicial PENDIENTE, `cancelar()` |

**Stub / Mock**

| Dependencia | Rol | Justificación |
|---|---|---|
| `DisponibilidadClient` | **Stub** (`when(...).thenReturn(true/false)`) | Servicio externo de consulta. Solo fija el escenario; no se verifica cómo se le llama. |
| `ReservaRepository` | **Mock** (`verify`, `InOrder`, `doThrow`) | Guardar es un efecto obligatorio de la regla: se verifica que ocurra una vez, antes de notificar, y nunca si no hay cupo. |
| `Notificador` | **Mock** (`verify`, `verifyNoInteractions`, `doThrow`) | Evita correos falsos o duplicados: se verifica que se notifique solo después de guardar. |
| `Reserva` | Objeto **real** | Es dominio simple; se verifica su estado en lugar de mockearlo. |

## Cómo verificar

```bash
git switch ae6/suite-pruebas
mvn clean test
```

Esperado: `Tests run: 26, Failures: 0, Errors: 0, Skipped: 0` y `BUILD SUCCESS`.
Reporte de cobertura: `target/site/jacoco/index.html`.
La salida completa queda en `docs/evidencias/mvn-clean-test.txt` (generada con `scripts/generar_evidencia.sh` o `.ps1`).

## Cobertura

| Medición | Instrucciones | Ramas | Líneas | Métodos |
|---|---|---|---|---|
| Inicial (17 casos de diseño) | 87 % (121/138) | 83 % (15/18) | 87 % (34/39) | 72 % (8/11) |
| Final (24 casos, 26 ejecuciones) | **100 %** (138/138) | **100 %** (18/18) | **100 %** (39/39) | **100 %** (11/11) |

**Interpretación:**
- `ReservaService` estaba al 100 % desde la primera medición. El hueco real estaba en `Reserva`: 2 de 4 ramas sin cubrir en la validación de `id`, el tipo por defecto sin probar y `cancelar()` al 0 %. Por eso se añadieron CP-21 a CP-24.
- El 100 % no bastaba. Leyendo el código frente a las reglas aparecieron riesgos que el porcentaje no muestra: tipo `null` (CP-18) y fallas de colaboradores (CP-19, CP-20).
- Una prueba **sin aserciones** también obtiene 100 % en `ReservaService` y sigue en verde con defectos inyectados. La suite detecta **7 de 7 mutantes manuales** (`docs/evidencias/experimentos/`).

## Limitaciones conocidas

- **Falla del notificador (CP-20):** si el correo falla, la reserva ya quedó `CONFIRMADA` y guardada, pero `confirmar` termina en excepción y el cliente no recibe aviso. La prueba caracteriza el comportamiento actual. Se recomienda que el negocio decida entre reintento o compensación.
- **Falla del repositorio (CP-19):** no se notifica (correcto), pero el objeto `Reserva` en memoria ya cambió a `CONFIRMADA` antes de `guardar()`. Hoy no hay rollback del estado.
- `puedeCancelar` acepta horas negativas y devuelve `false` sin lanzar excepción. Se probó como comportamiento seguro, pero la regla no define si debería rechazarse.
- `calcularTotal` usa `double` para dinero. Las pruebas usan tolerancia `1e-9`; en producción convendría `BigDecimal`.
- Son pruebas unitarias: no hay pruebas de integración con una base de datos ni un servicio de correo reales.
- La mutación se hizo de forma manual (7 mutantes). Una herramienta como PIT daría una medición más exhaustiva.

## Autorrevisión

- [x] Compila y `mvn clean test` termina en verde (26/26)
- [x] Revisé *Files changed* línea por línea
- [x] Sin `target/`, `.class` ni `.exec` en el repositorio
- [x] Sin credenciales ni datos sensibles
- [x] Nombres de prueba expresivos y `@DisplayName` con el ID del caso
- [x] Las aserciones comprueban comportamiento (estado, valor, excepción y mensaje)
- [x] Casos límite presentes (2/1/3 h; total 0 / −0.01; id `""` / `"   "`)
- [x] Los mocks verifican solo interacciones relevantes (guardar→notificar; ausencia de efectos)
- [x] Cobertura interpretada (antes/después, huecos, mutación)
- [x] Commits pequeños y descriptivos
- [x] Documentación y README actualizados

Detalle completo en `docs/04_AUTORREVISION.md`.

## Uso de IA

Herramienta: **Claude (Anthropic)**. Se usó como asistente para proponer la matriz de casos, redactar las pruebas JUnit/Mockito, ejecutar el análisis de cobertura y de mutación, y redactar la documentación y el reporte. Los commits generados con asistencia incluyen la línea `Co-Authored-By: Claude`. Revisé el contenido, ejecuté `mvn clean test` en mi equipo y verifiqué que las pruebas, las cifras de cobertura y las conclusiones corresponden al código del repositorio.
