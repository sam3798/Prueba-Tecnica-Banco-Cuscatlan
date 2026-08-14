package sv.bancocuscatlan.coworking.domain.state;

import sv.bancocuscatlan.coworking.domain.ReservationStatus;

public class CompletedState extends AbstractReservationState {

    @Override
    public ReservationStatus status() {
        return ReservationStatus.COMPLETED;
    }
}
