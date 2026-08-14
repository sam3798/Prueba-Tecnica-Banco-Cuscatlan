package sv.bancocuscatlan.coworking.domain.state;

import sv.bancocuscatlan.coworking.domain.Reserva;
import sv.bancocuscatlan.coworking.domain.ReservationStatus;

public class PendingPaymentState extends AbstractReservationState {

    @Override
    public ReservationStatus status() {
        return ReservationStatus.PENDING_PAYMENT;
    }

    @Override
    public void confirm(Reserva reserva) {
        transition(reserva, ReservationStatus.CONFIRMED);
    }

    @Override
    public void cancel(Reserva reserva) {
        transition(reserva, ReservationStatus.CANCELLED);
    }
}
