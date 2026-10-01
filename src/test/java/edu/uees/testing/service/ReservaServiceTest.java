package edu.uees.testing.service;

import edu.uees.testing.availability.DisponibilidadClient;
import edu.uees.testing.notification.Notificador;
import edu.uees.testing.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
