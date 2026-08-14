package sv.bancocuscatlan.coworking.payment;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

@Service
public class UnstablePaymentGateway {

    public PaymentValidationResponse validate(
            PaymentValidationRequest request,
            boolean forceFailure,
            long delayMs) {

        if (delayMs > 0) {
            try {
                TimeUnit.MILLISECONDS.sleep(delayMs);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Pago interrumpido", ex);
            }
        }

        if (forceFailure) {
            throw new IllegalStateException("Simulated payment gateway failure");
        }

        if (request.getPaymentMethod() == null || request.getPaymentMethod().isBlank()) {
            return PaymentValidationResponse.rejected("Método de pago requerido");
        }

        return PaymentValidationResponse.approved("Pago validado correctamente");
    }
}
