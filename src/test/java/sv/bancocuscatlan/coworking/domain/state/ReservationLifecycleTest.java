package sv.bancocuscatlan.coworking.domain.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sv.bancocuscatlan.coworking.domain.Espacio;
import sv.bancocuscatlan.coworking.domain.Reserva;
import sv.bancocuscatlan.coworking.domain.ReservationStatus;
import sv.bancocuscatlan.coworking.domain.Usuario;
import sv.bancocuscatlan.coworking.exception.InvalidReservationStateException;

class ReservationLifecycleTest {

    private ReservationLifecycle lifecycle;
    private Reserva reserva;

    @BeforeEach
    void setUp() {
        lifecycle = new ReservationLifecycle();
        reserva = new Reserva();
        reserva.setUsuario(new Usuario());
        reserva.setEspacio(new Espacio());
        reserva.setInicio(Instant.now().plusSeconds(3600));
        reserva.setFin(Instant.now().plusSeconds(7200));
        reserva.setMontoTotal(BigDecimal.TEN);
        reserva.setEstado(ReservationStatus.PENDING);
    }

    @Test
    void confirmFromPendingGoesToConfirmed() {
        lifecycle.confirm(reserva);
        assertEquals(ReservationStatus.CONFIRMED, reserva.getEstado());
    }

    @Test
    void markPendingPaymentFromPending() {
        lifecycle.markPendingPayment(reserva);
        assertEquals(ReservationStatus.PENDING_PAYMENT, reserva.getEstado());
    }

    @Test
    void cancelFromConfirmed() {
        reserva.setEstado(ReservationStatus.CONFIRMED);
        lifecycle.cancel(reserva);
        assertEquals(ReservationStatus.CANCELLED, reserva.getEstado());
    }

    @Test
    void completeFromConfirmed() {
        reserva.setEstado(ReservationStatus.CONFIRMED);
        lifecycle.complete(reserva);
        assertEquals(ReservationStatus.COMPLETED, reserva.getEstado());
    }

    @Test
    void cannotConfirmCancelledReservation() {
        reserva.setEstado(ReservationStatus.CANCELLED);
        assertThrows(InvalidReservationStateException.class, () -> lifecycle.confirm(reserva));
    }
}
