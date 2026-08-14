package sv.bancocuscatlan.coworking.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import sv.bancocuscatlan.coworking.event.ReservationConfirmedEvent;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReservationConfirmed(ReservationConfirmedEvent event) {
        log.info(
                "Simulando envío de correo a {} ({}), reserva #{} confirmada para espacio '{}'",
                event.getEmail(),
                event.getUsername(),
                event.getReservationId(),
                event.getEspacioNombre());
    }
}
