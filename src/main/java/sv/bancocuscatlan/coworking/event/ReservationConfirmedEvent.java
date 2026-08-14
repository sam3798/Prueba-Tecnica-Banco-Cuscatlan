package sv.bancocuscatlan.coworking.event;

import sv.bancocuscatlan.coworking.domain.Reserva;

public class ReservationConfirmedEvent {

    private final Long reservationId;
    private final String username;
    private final String email;
    private final String espacioNombre;

    public ReservationConfirmedEvent(Reserva reserva) {
        this.reservationId = reserva.getId();
        this.username = reserva.getUsuario().getUsername();
        this.email = reserva.getUsuario().getEmail();
        this.espacioNombre = reserva.getEspacio().getNombre();
    }

    public Long getReservationId() {
        return reservationId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getEspacioNombre() {
        return espacioNombre;
    }
}
