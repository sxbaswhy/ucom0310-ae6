package edu.uees.testing.notification;

import edu.uees.testing.domain.Reserva;

public interface Notificador {
    void enviarConfirmacion(Reserva reserva);
}
