# Análisis de cobertura — JaCoCo 0.8.12

Reporte generado por `mvn clean test` (el plugin `jacoco-maven-plugin` ejecuta `report` en la fase `test`).
Abrir: `target/site/jacoco/index.html`. Capturas y CSV en `docs/evidencias/jacoco/`.

Primero se midió la cobertura con los 17 casos del diseño inicial (CP-01 a CP-17). Luego se leyó el reporte, se añadieron 7 casos (CP-18 a CP-24) y se volvió a medir.

## Resultado observado

### Primera medición (después del commit `test: verificar confirmacion con stub de disponibilidad y mocks`)

| Clase | Instrucciones | Ramas | Líneas | Métodos |
|---|---|---|---|---|
| `ReservaService` | 74/74 (100 %) | 12/12 (100 %) | 21/21 (100 %) | 4/4 (100 %) |
| `Reserva` | 26/43 (**60 %**) | 3/6 (**50 %**) | 9/14 (64 %) | 3/6 (50 %) |
| `EstadoReserva` | 21/21 (100 %) | — | 4/4 | 1/1 |
| **Total** | **121/138 (87 %)** | **15/18 (83 %)** | **34/39 (87 %)** | **8/11 (73 %)** |

Capturas: `01_jacoco_inicial_resumen.png`, `02_jacoco_inicial_reserva_metodos.png`, `03_jacoco_inicial_reserva_fuente.png`.

### Medición final (rama `ae6/suite-pruebas`, 26 pruebas)

| Clase | Instrucciones | Ramas | Líneas | Métodos |
|---|---|---|---|---|
| `ReservaService` | 74/74 (100 %) | 12/12 (100 %) | 21/21 (100 %) | 4/4 (100 %) |
| `Reserva` | 43/43 (100 %) | 6/6 (100 %) | 14/14 (100 %) | 6/6 (100 %) |
| `EstadoReserva` | 21/21 (100 %) | — | 4/4 | 1/1 |
| **Total** | **138/138 (100 %)** | **18/18 (100 %)** | **39/39 (100 %)** | **11/11 (100 %)** |

Capturas: `04_jacoco_final_resumen.png`, `05_jacoco_final_reservaservice_metodos.png`, `06_jacoco_final_reservaservice_fuente.png`, `07_jacoco_final_reserva_metodos.png`.

**Clase analizada en detalle:** `ReservaService` (exigido) y `Reserva` (donde estaban los huecos).
Ramas de `ReservaService` por método: `confirmar` 4/4 (`reserva == null` y `estaDisponible`), `calcularTotal` 6/6 (tres `if`), `puedeCancelar` 2/2 (`>= 2`).

## Huecos relevantes

### Huecos que mostró el reporte (código no ejecutado)

1. **`Reserva(String, String)`, línea 10:** `if (id == null || id.isBlank())` tenía **2 de 4 ramas sin cubrir** (rombo amarillo) y la línea 11 (`throw`) en rojo. Ninguna prueba creaba una reserva con un id inválido, así que la validación de integridad del dominio **nunca se había ejecutado**. Es el hueco más relevante porque protege la identidad de toda reserva.
2. **`Reserva(String, String)`, línea 14:** `tipo == null ? "NORMAL" : tipo` tenía **1 de 2 ramas sin cubrir**: la regla "tipo por defecto NORMAL" no estaba probada.
3. **`cancelar()`** al **0 %** (líneas 35-36 en rojo): la transición a `CANCELADA` no se verificaba en ninguna parte.
4. `getId()` y `getTipo()` al 0 %. **No** se escribieron pruebas solo para estos getters. Quedaron cubiertos de forma natural al verificar el comportamiento del constructor (CP-23).

### Huecos que el reporte NO muestra (comportamiento no probado con 100 % de cobertura)

`ReservaService` ya estaba al 100 % en todas las métricas, pero leer el código junto con el reporte reveló riesgos que el porcentaje no ve:

5. **`calcularTotal(null, …)`:** la rama "sin descuento" estaba cubierta por `NORMAL`, pero nunca se probó `tipo == null`. Si alguien cambia `"VIP".equalsIgnoreCase(tipo)` por `tipo.equalsIgnoreCase("VIP")`, el código sigue al 100 % y lanza `NullPointerException` en producción.
6. **Falla del repositorio en `confirmar`:** JaCoCo cuenta líneas ejecutadas, no escenarios. Si `guardar()` lanza una excepción, ¿se notifica igual al cliente? Ninguna prueba lo decía.
7. **Falla del notificador en `confirmar`:** si el correo falla después de guardar, la reserva queda `CONFIRMADA` y persistida, pero el cliente no recibe aviso y la operación termina en excepción.

## Decisiones

### ¿Qué prueba nueva se añadió?

| Hueco | Caso | Prueba |
|---|---|---|
| 1 | CP-21, CP-22 | `ReservaTest.idNuloOEnBlancoLanzaExcepcion` (parametrizada: `null`, `""`, `"   "`) |
| 2 | CP-23 | `ReservaTest.tipoNuloSeAsumeNormalYNacePendiente` |
| 3 | CP-24 | `ReservaTest.cancelarCambiaEstadoACancelada` |
| 5 | CP-18 | `CalculoTotal.tipoNuloNoRecibeDescuento` |
| 6 | CP-19 | `Confirmacion.falloAlGuardarSePropagaYNoNotifica` (mock con `doThrow`) |
| 7 | CP-20 | `Confirmacion.falloAlNotificarSePropagaYLaReservaYaQuedoGuardada` (mock con `doThrow`) |

**Ejemplo principal:** CP-21/CP-22. El reporte marcaba en amarillo la línea 10 de `Reserva` con "2 of 4 branches missed". Las dos ramas faltantes eran `id == null → true` e `id.isBlank() → true`. La prueba parametrizada envía `null`, `""` y `"   "`, verifica la excepción y su mensaje, y deja la línea en verde con 4/4 ramas.

### ¿Qué riesgo protege?

- CP-21/22/23: que se creen reservas sin identificador o con tipo indefinido, lo que rompería la persistencia y el cálculo del total.
- CP-24: que la cancelación no cambie el estado (el cliente creería que canceló).
- CP-18: un `NullPointerException` al cobrar si el tipo llega vacío desde la interfaz.
- CP-19: **notificar una reserva que no se guardó**, el error más grave para el cliente.
- CP-20: deja documentado que hoy una falla de correo deja la reserva confirmada sin aviso. Ver *Limitaciones*.

### ¿Por qué no basta con el porcentaje?

1. **Cobertura mide ejecución, no verificación.** Como experimento se escribió una prueba **sin ninguna aserción** que solo llama a todos los métodos de `ReservaService`. JaCoCo reportó **100 % de líneas y ramas**, y esa prueba **siguió en verde aun después de introducir dos errores** en el código (`>= 2` cambiado por `> 2` y VIP al 20 %). Nuestra suite detecta ambos.
2. **Pruebas de mutación manual.** Se introdujeron 7 defectos uno a uno en `ReservaService` y se ejecutó la suite. **Todos fueron detectados**:

| Mutante | Cambio introducido | Pruebas que fallaron |
|---|---|---|
| M1 | `>= 2` → `> 2` | CP-02 |
| M2 | VIP `0.85` → `0.80` | CP-08, CP-10 |
| M3 | `equalsIgnoreCase` → `equals` (VIP) | CP-10 |
| M4 | `totalBase < 0` → `totalBase <= 0` | CP-12 |
| M5 | `totalBase < 0` → `totalBase < -1` | CP-13, CP-14 |
| M6 | notificar **antes** de guardar | CP-15, CP-19, CP-20 |
| M7 | eliminar la guarda `reserva == null` | CP-17 |

   M1 solo lo detecta CP-02 y M4 solo CP-12: sin los casos **límite** de la matriz, esos errores pasarían con el mismo 100 %.
3. **Los huecos 5, 6 y 7** existían con `ReservaService` al 100 %. Se encontraron leyendo el código frente a las reglas de negocio, no mirando el número.

**Conclusión:** el 100 % final es consecuencia de diseñar casos por regla, límite y riesgo, no un objetivo en sí mismo. El indicador útil fue el **detalle por línea y rama** del reporte (rombos amarillos y líneas rojas en `Reserva`) junto con el análisis de mutantes.
