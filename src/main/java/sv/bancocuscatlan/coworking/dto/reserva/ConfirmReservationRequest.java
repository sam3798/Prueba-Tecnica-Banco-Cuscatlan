package sv.bancocuscatlan.coworking.dto.reserva;

import jakarta.validation.constraints.NotBlank;

public class ConfirmReservationRequest {

    @NotBlank
    private String paymentMethod;

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
