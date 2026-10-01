# Matriz de casos de prueba — Ae6

**Proyecto:** `semana7-proyecto-base-ae6` · **Rama:** `ae6/suite-pruebas` · **Clase principal bajo prueba:** `ReservaService`

## 1. Reglas de negocio identificadas

Las reglas se obtuvieron leyendo el código productivo (`ReservaService`, `Reserva`) sin modificarlo.

| ID | Regla | Método | Fuente en el código |
|---|---|---|---|
| R1 | Una reserva se puede cancelar solo con **2 horas o más** de anticipación. | `puedeCancelar(int)` | `return horasAnticipacion >= 2;` |
| R2 | Descuento por tipo: **VIP 15 %**, **ESTUDIANTE 10 %**, cualquier otro tipo paga el total. La comparación **no distingue mayúsculas**. | `calcularTotal(String, double)` | `equalsIgnoreCase("VIP")`, `equalsIgnoreCase("ESTUDIANTE")` |
| R3 | Un total base **negativo** es inválido y se rechaza con `IllegalArgumentException` **antes** de aplicar cualquier descuento. | `calcularTotal(String, double)` | `if (totalBase < 0) throw ...` |
| R4 | Confirmar exige reserva **no nula** y horario **disponible** (servicio externo). Si se confirma: el estado pasa a `CONFIRMADA`, se **guarda** y se **notifica**. | `confirmar(Reserva)` | guardas + `reserva.confirmar(); repository.guardar(); notificador.enviarConfirmacion();` |
| R5 | Una reserva requiere **id no vacío**; si el tipo es `null` se asume `NORMAL`; toda reserva nace `PENDIENTE`. | `Reserva(String, String)` | constructor de `Reserva` |

## 2. Técnicas de diseño aplicadas

- **Partición de equivalencia:** horas suficientes / insuficientes; tipos con descuento / sin descuento; total válido / inválido; horario disponible / no disponible.
- **Análisis de valores límite:** para R1 se prueban 1, **2** y 3 horas alrededor de la frontera `>= 2`; para R3 se prueban `0` (válido) y `-0.01` / `-1` (inválidos).
- **Tabla de decisión** para R4: `reserva == null` × `disponible` → excepción de argumento / excepción de estado / confirmación completa.
- **Priorización por riesgo:** *Alto* = error con impacto económico o de integridad (cobrar mal, confirmar sin cupo, notificar algo no guardado); *Medio* = comportamiento alternativo que podría romperse en un refactor; *Bajo* = caso de apoyo.

## 3. Matriz de casos

| ID | Regla | Método | Escenario | Entrada | Esperado | Tipo | Riesgo | Justificación | Prueba JUnit |
|---|---|---|---|---|---|---|---|---|---|
| CP-01 | R1 | `puedeCancelar` | Anticipación holgada | `5` | `true` | Normal | Medio | Camino feliz de la regla. | `Cancelacion.cincoHorasPermitenCancelar` |
| CP-02 | R1 | `puedeCancelar` | Límite exacto | `2` | `true` | Límite | **Alto** | Detecta el error típico de cambiar `>=` por `>` (off-by-one). | `Cancelacion.dosHorasExactasPermitenCancelar` |
| CP-03 | R1 | `puedeCancelar` | Justo debajo del límite | `1` | `false` | Límite | **Alto** | Par del CP-02: juntos fijan la frontera exacta. | `Cancelacion.unaHoraNoPermiteCancelar` |
| CP-04 | R1 | `puedeCancelar` | Justo encima del límite | `3` | `true` | Límite | Bajo | Confirma que la regla no es una igualdad (`== 2`). | `Cancelacion.conAnticipacionSuficientePermiteCancelar` |
| CP-05 | R1 | `puedeCancelar` | Sin anticipación | `0` | `false` | Límite | Medio | Cancelación en la misma hora del servicio. | `Cancelacion.sinAnticipacionSuficienteNoPermiteCancelar` |
| CP-06 | R1 | `puedeCancelar` | Hora ya pasada | `-1` | `false` | Inválido | Medio | Entrada fuera de dominio: debe rechazarse de forma segura (sin permitir cancelar). | `Cancelacion.sinAnticipacionSuficienteNoPermiteCancelar` |
| CP-07 | R2 | `calcularTotal` | Cliente NORMAL | `"NORMAL", 100` | `100.0` | Normal | Medio | Sin descuento: protege contra descuentos accidentales. | `CalculoTotal.clienteNormalPagaTotalSinDescuento` |
| CP-08 | R2 | `calcularTotal` | Cliente VIP | `"VIP", 100` | `85.0` | Alternativo | **Alto** | Impacto económico directo (15 %). | `CalculoTotal.aplicaDescuentoSegunTipo` |
| CP-09 | R2 | `calcularTotal` | Cliente ESTUDIANTE | `"ESTUDIANTE", 100` | `90.0` | Alternativo | **Alto** | Impacto económico directo (10 %). | `CalculoTotal.aplicaDescuentoSegunTipo` |
| CP-10 | R2 | `calcularTotal` | Tipo en minúsculas / mixto | `"vip", 200` · `"Estudiante", 50` | `170.0` · `45.0` | Alternativo | Medio | La regla usa `equalsIgnoreCase`; un refactor a `equals` rompería a clientes reales. | `CalculoTotal.aplicaDescuentoSegunTipo` |
| CP-11 | R2 | `calcularTotal` | Tipo no contemplado | `"CORPORATIVO", 100` | `100.0` | Alternativo | Bajo | Un tipo desconocido no debe recibir descuento. | `CalculoTotal.tipoNoContempladoNoRecibeDescuento` |
| CP-12 | R3 | `calcularTotal` | Total cero | `"VIP", 0` | `0.0` | Límite | Medio | `0` es el menor valor válido: no debe lanzar excepción. | `CalculoTotal.totalCeroEsValido` |
| CP-13 | R3 | `calcularTotal` | Total negativo | `"NORMAL", -1` | `IllegalArgumentException("Total base inválido")` | Excepción | **Alto** | Evita cobrar montos negativos. | `CalculoTotal.totalNegativoLanzaExcepcion` |
| CP-14 | R3 | `calcularTotal` | Negativo mínimo con descuento | `"VIP", -0.01` | `IllegalArgumentException` | Límite / Inválido | **Alto** | La validación debe ocurrir **antes** del descuento, aun para VIP. | `CalculoTotal.totalLigeramenteNegativoSeRechazaAunqueTengaDescuento` |
| CP-15 | R4 | `confirmar` | Horario disponible | `R-001` + stub `true` | Estado `CONFIRMADA`; `guardar` y luego `enviarConfirmacion` una sola vez | Normal | **Alto** | Camino principal del negocio; el orden evita notificar algo no persistido. | `Confirmacion.reservaDisponibleSeConfirmaGuardaYNotifica` |
| CP-16 | R4 | `confirmar` | Horario no disponible | `R-002` + stub `false` | `IllegalStateException("Horario no disponible")`; estado `PENDIENTE`; sin guardar ni notificar | Alternativo / Excepción | **Alto** | Evita sobreventa y correos falsos. | `Confirmacion.horarioNoDisponibleLanzaExcepcionYNoGuardaNiNotifica` |
| CP-17 | R4 | `confirmar` | Reserva nula | `null` | `IllegalArgumentException("Reserva obligatoria")`; sin llamar a ningún colaborador | Inválido / Excepción | **Alto** | La guarda debe ejecutarse antes de consultar el servicio externo. | `Confirmacion.reservaNulaLanzaExcepcionSinConsultarServicios` |

**Total de casos de diseño inicial:** 17 (6 de cancelación, 8 de cálculo de total, 3 de confirmación).
Distribución por tipo: Normal 3 · Alternativo 5 · Límite 6 · Inválido/Excepción 5 (CP-14 y CP-16 combinan dos tipos).

## 4. Casos incorporados a partir del análisis de JaCoCo

Después de la primera ejecución con cobertura (ver `docs/02_ANALISIS_COBERTURA_PLANTILLA.md`) se añadieron estos casos. La columna **Hallazgo** indica qué evidencia los motivó.

| ID | Regla | Método | Escenario | Entrada | Esperado | Tipo | Riesgo | Hallazgo que lo motivó | Prueba JUnit |
|---|---|---|---|---|---|---|---|---|---|
| CP-21 | R5 | `Reserva(...)` | Id nulo | `null, "VIP"` | `IllegalArgumentException("Id obligatorio")` | Inválido / Excepción | **Alto** | Línea 10 de `Reserva`: 2 de 4 ramas sin cubrir; línea 11 (`throw`) en rojo. | `ReservaTest.idNuloOEnBlancoLanzaExcepcion` |
| CP-22 | R5 | `Reserva(...)` | Id vacío o en blanco | `""` · `"   "` | `IllegalArgumentException("Id obligatorio")` | Límite / Inválido | **Alto** | Misma rama: `isBlank()` distingue `""` de `"   "`. | `ReservaTest.idNuloOEnBlancoLanzaExcepcion` |
| CP-23 | R5 | `Reserva(...)` | Tipo nulo | `"R-010", null` | tipo `NORMAL`, estado `PENDIENTE` | Alternativo | Medio | Línea 14: rama `tipo == null` sin cubrir. | `ReservaTest.tipoNuloSeAsumeNormalYNacePendiente` |
| CP-24 | R5 | `cancelar()` | Cancelar reserva | `R-011` | estado `CANCELADA` | Normal | Medio | `cancelar()` con 0 % de cobertura (líneas 35-36 en rojo). | `ReservaTest.cancelarCambiaEstadoACancelada` |
