package edu.uees.testing.service;

import edu.uees.testing.availability.DisponibilidadClient;
import edu.uees.testing.domain.Reserva;
import edu.uees.testing.notification.Notificador;
import edu.uees.testing.repository.ReservaRepository;

public class ReservaService {

    private final DisponibilidadClient disponibilidad;
    private final ReservaRepository repository;
    private final Notificador notificador;

    public ReservaService(
            DisponibilidadClient disponibilidad,
            ReservaRepository repository,
            Notificador notificador) {
        this.disponibilidad = disponibilidad;
        this.repository = repository;
        this.notificador = notificador;
    }

    public boolean puedeCancelar(int horasAnticipacion) {
        return horasAnticipacion >= 2;
    }

    public double calcularTotal(String tipo, double totalBase) {
        if (totalBase < 0) {
            throw new IllegalArgumentException("Total base inválido");
        }

        if ("VIP".equalsIgnoreCase(tipo)) {
            return totalBase * 0.85;
        }

        if ("ESTUDIANTE".equalsIgnoreCase(tipo)) {
            return totalBase * 0.90;
        }

        return totalBase;
    }

    public void confirmar(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("Reserva obligatoria");
        }

        if (!disponibilidad.estaDisponible(reserva)) {
            throw new IllegalStateException("Horario no disponible");
        }

        reserva.confirmar();
        repository.guardar(reserva);
        notificador.enviarConfirmacion(reserva);
    }
}
