package sv.bancocuscatlan.coworking.payment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;

import sv.bancocuscatlan.coworking.config.PaymentProperties;

@ExtendWith(MockitoExtension.class)
class PaymentValidationClientTest {

    @Mock
    private UnstablePaymentGateway paymentGateway;
    @Mock
    private CircuitBreakerFactory<?, ?> circuitBreakerFactory;
    @Mock
    private CircuitBreaker circuitBreaker;

    @Test
    void returnsFallbackWhenCircuitBreakerTriggers() {
        PaymentProperties properties = new PaymentProperties();
        properties.setForceFailure(true);

        when(circuitBreakerFactory.create("paymentService")).thenReturn(circuitBreaker);
        when(circuitBreaker.run(any(Supplier.class), any(Function.class))).thenAnswer(invocation -> {
            Function<Throwable, PaymentValidationResponse> fallback = invocation.getArgument(1);
            return fallback.apply(new IllegalStateException("down"));
        });

        PaymentValidationClient client = new PaymentValidationClient(paymentGateway, properties, circuitBreakerFactory);
        PaymentValidationResponse response = client.validate(
                new PaymentValidationRequest(1L, BigDecimal.TEN, "CARD"));

        assertTrue(response.isFallbackUsed());
        assertFalse(response.isApproved());
    }

    @Test
    void returnsApprovedWhenGatewaySucceeds() {
        PaymentProperties properties = new PaymentProperties();
        when(circuitBreakerFactory.create("paymentService")).thenReturn(circuitBreaker);
        when(circuitBreaker.run(any(Supplier.class), any(Function.class))).thenAnswer(invocation -> {
            Supplier<PaymentValidationResponse> supplier = invocation.getArgument(0);
            return supplier.get();
        });
        when(paymentGateway.validate(any(), anyBoolean(), anyLong()))
                .thenReturn(PaymentValidationResponse.approved("OK"));

        PaymentValidationClient client = new PaymentValidationClient(paymentGateway, properties, circuitBreakerFactory);
        PaymentValidationResponse response = client.validate(
                new PaymentValidationRequest(1L, BigDecimal.TEN, "CARD"));

        assertTrue(response.isApproved());
        assertFalse(response.isFallbackUsed());
    }
}
