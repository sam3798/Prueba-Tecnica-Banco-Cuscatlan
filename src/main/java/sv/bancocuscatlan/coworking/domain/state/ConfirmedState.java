package sv.bancocuscatlan.coworking.domain.state;

import sv.bancocuscatlan.coworking.domain.Reserva;
import sv.bancocuscatlan.coworking.domain.ReservationStatus;

public class ConfirmedState extends AbstractReservationState {

    @Override
    public ReservationStatus status() {
        return ReservationStatus.CONFIRMED;
    }

    @Override
    public void cancel(Reserva reserva) {
        transition(reserva, ReservationStatus.CANCELLED);
    }

    @Override
    public void complete(Reserva reserva) {
        transition(reserva, ReservationStatus.COMPLETED);
    }
}
