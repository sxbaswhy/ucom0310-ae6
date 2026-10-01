package edu.uees.testing.service;

import edu.uees.testing.availability.DisponibilidadClient;
import edu.uees.testing.domain.EstadoReserva;
import edu.uees.testing.domain.Reserva;
import edu.uees.testing.notification.Notificador;
import edu.uees.testing.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Suite Ae6 para {@link ReservaService}.
 *
 * <p>Cada prueba sigue la estructura AAA (Arrange, Act, Assert) y su
 * {@code @DisplayName} referencia el caso de la matriz
 * ({@code docs/01_MATRIZ_CASOS_PLANTILLA.md}).</p>
 *
 * <p>Los colaboradores externos se reemplazan por dobles de Mockito porque
 * el constructor del servicio los exige. En {@code puedeCancelar} y
 * {@code calcularTotal} no se configuran ni se verifican: esos métodos son
 * lógica pura y no dependen de ellos.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReservaService")
class ReservaServiceTest {

    @Mock
    private DisponibilidadClient disponibilidad;

    @Mock
    private ReservaRepository repository;

    @Mock
    private Notificador notificador;

    private ReservaService servicio;

    @BeforeEach
    void crearServicio() {
        servicio = new ReservaService(disponibilidad, repository, notificador);
    }

    @Test
    void entornoJUnitFunciona() {
        assertTrue(true);
    }

    @Nested
    @DisplayName("R1 · Cancelación con 2 horas o más de anticipación")
    class Cancelacion {

        @Test
        @DisplayName("CP-01 · 5 horas de anticipación permiten cancelar")
        void cincoHorasPermitenCancelar() {
            // Arrange
            int horas = 5;

            // Act
            boolean resultado = servicio.puedeCancelar(horas);

            // Assert
            assertTrue(resultado);
        }

        @Test
        @DisplayName("CP-02 · Límite exacto: 2 horas permiten cancelar")
        void dosHorasExactasPermitenCancelar() {
            // Arrange
            int horasEnElLimite = 2;

            // Act
            boolean resultado = servicio.puedeCancelar(horasEnElLimite);

            // Assert
            assertTrue(resultado, "2 horas es el límite inferior válido (>= 2)");
        }

        @Test
        @DisplayName("CP-03 · Justo debajo del límite: 1 hora no permite cancelar")
        void unaHoraNoPermiteCancelar() {
            // Arrange
            int horasBajoElLimite = 1;

            // Act
            boolean resultado = servicio.puedeCancelar(horasBajoElLimite);

            // Assert
            assertFalse(resultado, "1 hora queda por debajo del mínimo de 2");
        }

        @Test
        @DisplayName("CP-04 · Justo encima del límite: 3 horas permiten cancelar")
        void conAnticipacionSuficientePermiteCancelar() {
            // Arrange
            int horasSobreElLimite = 3;

            // Act
            boolean resultado = servicio.puedeCancelar(horasSobreElLimite);

            // Assert
            assertTrue(resultado);
        }

        @ParameterizedTest(name = "CP-05/CP-06 · {0} horas no permiten cancelar")
        @ValueSource(ints = {0, -1})
        @DisplayName("CP-05/CP-06 · Sin anticipación u hora ya pasada no permiten cancelar")
        void sinAnticipacionSuficienteNoPermiteCancelar(int horas) {
            // Act
            boolean resultado = servicio.puedeCancelar(horas);

            // Assert
            assertFalse(resultado);
        }
    }

    @Nested
    @DisplayName("R2/R3 · Cálculo del total con descuento por tipo")
    class CalculoTotal {

        /** Tolerancia para comparar montos en double sin depender del redondeo binario. */
        private static final double DELTA = 1e-9;

        @Test
        @DisplayName("CP-07 · Cliente NORMAL paga el total sin descuento")
        void clienteNormalPagaTotalSinDescuento() {
            // Arrange
            double totalBase = 100.0;

            // Act
            double total = servicio.calcularTotal("NORMAL", totalBase);

            // Assert
            assertEquals(100.0, total, DELTA);
        }

        @ParameterizedTest(name = "{0} · tipo={1}, base={2} → total={3}")
        @CsvSource({
                "CP-08, VIP,        100.0, 85.0",
                "CP-09, ESTUDIANTE, 100.0, 90.0",
                "CP-10, vip,        200.0, 170.0",
                "CP-10, Estudiante,  50.0, 45.0"
        })
        @DisplayName("CP-08/CP-09/CP-10 · Aplica 15 % a VIP y 10 % a ESTUDIANTE sin importar mayúsculas")
        void aplicaDescuentoSegunTipo(String caso, String tipo, double totalBase, double esperado) {
            // Act
            double total = servicio.calcularTotal(tipo, totalBase);

            // Assert
            assertEquals(esperado, total, DELTA, () -> caso + ": descuento incorrecto para " + tipo);
        }

        @Test
        @DisplayName("CP-11 · Un tipo no contemplado no recibe descuento")
        void tipoNoContempladoNoRecibeDescuento() {
            // Act
            double total = servicio.calcularTotal("CORPORATIVO", 100.0);

            // Assert
            assertEquals(100.0, total, DELTA);
        }

        @Test
        @DisplayName("CP-12 · Límite: total base 0 es válido y no lanza excepción")
        void totalCeroEsValido() {
            // Act
            double total = servicio.calcularTotal("VIP", 0.0);

            // Assert
            assertEquals(0.0, total, DELTA);
        }

        @Test
        @DisplayName("CP-13 · Total base negativo lanza IllegalArgumentException")
        void totalNegativoLanzaExcepcion() {
            // Act
            IllegalArgumentException error = assertThrows(
                    IllegalArgumentException.class,
                    () -> servicio.calcularTotal("NORMAL", -1));

            // Assert
            assertEquals("Total base inválido", error.getMessage());
        }

        @Test
        @DisplayName("CP-14 · Límite inválido: -0.01 se rechaza aunque el tipo tenga descuento")
        void totalLigeramenteNegativoSeRechazaAunqueTengaDescuento() {
            // Act + Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> servicio.calcularTotal("VIP", -0.01));
        }
    }

    /**
     * Pruebas de {@code confirmar}, el único método con dependencias externas.
     *
     * <ul>
     *   <li><b>Stub</b>: {@code DisponibilidadClient}. Solo se le programa la respuesta
     *       (disponible / no disponible) para fijar el escenario; no se verifica cómo se
     *       le llama porque es una consulta sin efectos secundarios.</li>
     *   <li><b>Mock</b>: {@code ReservaRepository} y {@code Notificador}. Sus llamadas son
     *       el efecto observable de la regla (persistir y avisar al cliente), así que se
     *       verifica que ocurran, en qué orden y que no ocurran cuando no deben.</li>
     *   <li>{@code Reserva} es un objeto de dominio simple: se usa la clase real y se
     *       comprueba su estado; no se mockea.</li>
     * </ul>
     */
    @Nested
    @DisplayName("R4 · Confirmación con servicio de disponibilidad, persistencia y notificación")
    class Confirmacion {

        @Test
        @DisplayName("CP-15 · Horario disponible: confirma, guarda y luego notifica una sola vez")
        void reservaDisponibleSeConfirmaGuardaYNotifica() {
            // Arrange
            Reserva reserva = new Reserva("R-001", "NORMAL");
            when(disponibilidad.estaDisponible(reserva)).thenReturn(true); // Stub

            // Act
            servicio.confirmar(reserva);

            // Assert: estado (objeto real)
            assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
            // Assert: interacciones relevantes (mocks), en el orden de negocio
            InOrder orden = inOrder(repository, notificador);
            orden.verify(repository).guardar(reserva);
            orden.verify(notificador).enviarConfirmacion(reserva);
            verifyNoMoreInteractions(repository, notificador);
        }

        @Test
        @DisplayName("CP-16 · Horario no disponible: lanza excepción, sigue PENDIENTE y no guarda ni notifica")
        void horarioNoDisponibleLanzaExcepcionYNoGuardaNiNotifica() {
            // Arrange
            Reserva reserva = new Reserva("R-002", "VIP");
            when(disponibilidad.estaDisponible(reserva)).thenReturn(false); // Stub

            // Act
            IllegalStateException error = assertThrows(
                    IllegalStateException.class,
                    () -> servicio.confirmar(reserva));

            // Assert
            assertEquals("Horario no disponible", error.getMessage());
            assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
            verifyNoInteractions(repository, notificador);
        }

        @Test
        @DisplayName("CP-17 · Reserva nula: lanza excepción sin consultar ningún servicio")
        void reservaNulaLanzaExcepcionSinConsultarServicios() {
            // Act
            IllegalArgumentException error = assertThrows(
                    IllegalArgumentException.class,
                    () -> servicio.confirmar(null));

            // Assert
            assertEquals("Reserva obligatoria", error.getMessage());
            verifyNoInteractions(disponibilidad, repository, notificador);
        }
    }
}
