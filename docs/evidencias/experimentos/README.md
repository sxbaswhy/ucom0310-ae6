# Experimentos de calidad de la suite

Estos experimentos se ejecutaron sobre **copias temporales** del proyecto. El código productivo de la rama no se modificó.

## 1. Cobertura sin aserciones (`SinAsercionesTest.java.txt`)

Una única prueba llama a todos los métodos de `ReservaService` y **no verifica nada**.

| Escenario | Pruebas | ReservaService: líneas | ReservaService: ramas |
|---|---|---|---|
| Código original | 1/1 en verde | 21/21 (100 %) | 12/12 (100 %) |
| Código con 2 defectos (`> 2` y VIP `0.80`) | **1/1 en verde** | 21/21 (100 %) | 12/12 (100 %) |

Conclusión: el 100 % de cobertura es compatible con una prueba que no detecta errores.

## 2. Mutación manual

Para cada mutante se aplicó un único cambio en `ReservaService.java` y se ejecutó la suite completa (26 pruebas).

| Mutante | Cambio | Resultado | Pruebas que lo detectan |
|---|---|---|---|
| M1 | `horasAnticipacion >= 2` → `> 2` | Detectado | CP-02 |
| M2 | `totalBase * 0.85` → `* 0.80` | Detectado | CP-08, CP-10 |
| M3 | `"VIP".equalsIgnoreCase(tipo)` → `"VIP".equals(tipo)` | Detectado | CP-10 |
| M4 | `totalBase < 0` → `totalBase <= 0` | Detectado | CP-12 |
| M5 | `totalBase < 0` → `totalBase < -1` | Detectado | CP-13, CP-14 |
| M6 | `enviarConfirmacion` antes de `guardar` | Detectado | CP-15, CP-19, CP-20 |
| M7 | eliminar `if (reserva == null) throw ...` | Detectado | CP-17 |

**7 de 7 mutantes detectados.**
