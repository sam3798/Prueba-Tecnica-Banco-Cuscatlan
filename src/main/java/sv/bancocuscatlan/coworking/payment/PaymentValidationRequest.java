package sv.bancocuscatlan.coworking.payment;

import java.math.BigDecimal;

public class PaymentValidationRequest {

    private Long reservationId;
    private BigDecimal amount;
    private String paymentMethod;

    public PaymentValidationRequest() {
    }

    public PaymentValidationRequest(Long reservationId, BigDecimal amount, String paymentMethod) {
        this.reservationId = reservationId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
