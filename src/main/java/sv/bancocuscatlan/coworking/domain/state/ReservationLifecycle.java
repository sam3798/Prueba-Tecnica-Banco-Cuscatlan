package sv.bancocuscatlan.coworking.domain.state;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import sv.bancocuscatlan.coworking.domain.Reserva;
import sv.bancocuscatlan.coworking.domain.ReservationStatus;

@Component
public class ReservationLifecycle {

    private final Map<ReservationStatus, ReservationState> states = new EnumMap<>(ReservationStatus.class);

    public ReservationLifecycle() {
        states.put(ReservationStatus.PENDING, new PendingState());
        states.put(ReservationStatus.PENDING_PAYMENT, new PendingPaymentState());
        states.put(ReservationStatus.CONFIRMED, new ConfirmedState());
        states.put(ReservationStatus.CANCELLED, new CancelledState());
        states.put(ReservationStatus.COMPLETED, new CompletedState());
    }

    public void confirm(Reserva reserva) {
        current(reserva).confirm(reserva);
    }

    public void markPendingPayment(Reserva reserva) {
        current(reserva).markPendingPayment(reserva);
    }

    public void cancel(Reserva reserva) {
        current(reserva).cancel(reserva);
    }

    public void complete(Reserva reserva) {
        current(reserva).complete(reserva);
    }

    private ReservationState current(Reserva reserva) {
        ReservationState state = states.get(reserva.getEstado());
        if (state == null) {
            throw new IllegalStateException("Estado de reserva no soportado: " + reserva.getEstado());
        }
        return state;
    }
}
