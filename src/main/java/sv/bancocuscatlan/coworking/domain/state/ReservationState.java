package sv.bancocuscatlan.coworking.domain.state;

import sv.bancocuscatlan.coworking.domain.Reserva;
import sv.bancocuscatlan.coworking.domain.ReservationStatus;

public interface ReservationState {

    ReservationStatus status();

    void confirm(Reserva reserva);

    void markPendingPayment(Reserva reserva);

    void cancel(Reserva reserva);

    void complete(Reserva reserva);
}
