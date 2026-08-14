package sv.bancocuscatlan.coworking.payment;

public class PaymentValidationResponse {

    private boolean approved;
    private String message;
    private boolean fallbackUsed;

    public PaymentValidationResponse() {
    }

    public PaymentValidationResponse(boolean approved, String message, boolean fallbackUsed) {
        this.approved = approved;
        this.message = message;
        this.fallbackUsed = fallbackUsed;
    }

    public static PaymentValidationResponse approved(String message) {
        return new PaymentValidationResponse(true, message, false);
    }

    public static PaymentValidationResponse rejected(String message) {
        return new PaymentValidationResponse(false, message, false);
    }

    public static PaymentValidationResponse fallback(String message) {
        return new PaymentValidationResponse(false, message, true);
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isFallbackUsed() {
        return fallbackUsed;
    }

    public void setFallbackUsed(boolean fallbackUsed) {
        this.fallbackUsed = fallbackUsed;
    }
}
