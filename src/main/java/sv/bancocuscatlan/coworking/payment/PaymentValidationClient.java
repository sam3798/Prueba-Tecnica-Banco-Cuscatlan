package sv.bancocuscatlan.coworking.payment;

import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;

import sv.bancocuscatlan.coworking.config.PaymentProperties;

@Service
public class PaymentValidationClient {

    private final UnstablePaymentGateway paymentGateway;
    private final PaymentProperties paymentProperties;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    public PaymentValidationClient(
            UnstablePaymentGateway paymentGateway,
            PaymentProperties paymentProperties,
            CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.paymentGateway = paymentGateway;
        this.paymentProperties = paymentProperties;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    public PaymentValidationResponse validate(PaymentValidationRequest request) {
        return circuitBreakerFactory.create("paymentService").run(
                () -> paymentGateway.validate(
                        request,
                        paymentProperties.isForceFailure(),
                        paymentProperties.getSimulatedDelayMs()),
                throwable -> PaymentValidationResponse.fallback(
                        "Servicio de pago no disponible: " + throwable.getClass().getSimpleName()));
    }
}
