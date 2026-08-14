package sv.bancocuscatlan.coworking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import sv.bancocuscatlan.coworking.payment.PaymentValidationRequest;
import sv.bancocuscatlan.coworking.payment.PaymentValidationResponse;
import sv.bancocuscatlan.coworking.payment.UnstablePaymentGateway;

@RestController
@RequestMapping("/internal/mock/payment")
public class MockPaymentController {

    private final UnstablePaymentGateway paymentGateway;

    public MockPaymentController(UnstablePaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    @PostMapping("/validate")
    public ResponseEntity<PaymentValidationResponse> validate(
            @RequestBody PaymentValidationRequest request,
            @RequestParam(defaultValue = "false") boolean forceFailure,
            @RequestParam(defaultValue = "0") long delayMs) {
        return ResponseEntity.ok(paymentGateway.validate(request, forceFailure, delayMs));
    }
}
