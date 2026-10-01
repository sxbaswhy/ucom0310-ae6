package edu.uees.testing.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas de la regla R5 (integridad de {@link Reserva}).
 *
 * <p>Se añadieron a partir del análisis de JaCoCo: en la primera ejecución
 * {@code Reserva} tenía 50 % de ramas cubiertas (la validación del id y el
 * tipo por defecto nunca se ejercitaban) y {@code cancelar()} tenía 0 %.</p>
 */
@DisplayName("R5 · Reserva (dominio)")
class ReservaTest {

    @ParameterizedTest(name = "CP-21/CP-22 · id=\"{0}\" se rechaza")
    @NullSource
    @ValueSource(strings = {"", "   "})
    @DisplayName("CP-21/CP-22 · Id nulo, vacío o en blanco lanza IllegalArgumentException")
    void idNuloOEnBlancoLanzaExcepcion(String idInvalido) {
        // Act
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> new Reserva(idInvalido, "VIP"));

        // Assert
        assertEquals("Id obligatorio", error.getMessage());
    }

    @Test
    @DisplayName("CP-23 · Tipo nulo se asume NORMAL y la reserva nace PENDIENTE")
    void tipoNuloSeAsumeNormalYNacePendiente() {
        // Act
        Reserva reserva = new Reserva("R-010", null);

        // Assert
        assertAll(
                () -> assertEquals("R-010", reserva.getId()),
                () -> assertEquals("NORMAL", reserva.getTipo()),
                () -> assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado()));
    }

    @Test
    @DisplayName("CP-24 · Cancelar una reserva cambia su estado a CANCELADA")
    void cancelarCambiaEstadoACancelada() {
        // Arrange
        Reserva reserva = new Reserva("R-011", "ESTUDIANTE");

        // Act
        reserva.cancelar();

        // Assert
        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
    }
}
