package sv.bancocuscatlan.coworking.domain.state;

import sv.bancocuscatlan.coworking.domain.Reserva;
import sv.bancocuscatlan.coworking.domain.ReservationStatus;
import sv.bancocuscatlan.coworking.exception.InvalidReservationStateException;

abstract class AbstractReservationState implements ReservationState {

    protected void fail(String action) {
        throw new InvalidReservationStateException(
                "No se puede " + action + " una reserva en estado " + status());
    }

    @Override
    public void confirm(Reserva reserva) {
        fail("confirmar");
    }

    @Override
    public void markPendingPayment(Reserva reserva) {
        fail("marcar como pendiente de pago");
    }

    @Override
    public void cancel(Reserva reserva) {
        fail("cancelar");
    }

    @Override
    public void complete(Reserva reserva) {
        fail("completar");
    }

    protected void transition(Reserva reserva, ReservationStatus next) {
        reserva.setEstado(next);
    }
}
