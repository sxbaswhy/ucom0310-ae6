package edu.uees.testing.availability;

import edu.uees.testing.domain.Reserva;

public interface DisponibilidadClient {
    boolean estaDisponible(Reserva reserva);
}
