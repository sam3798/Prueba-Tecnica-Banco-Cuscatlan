package sv.bancocuscatlan.coworking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.payment")
public class PaymentProperties {

    private String baseUrl = "http://localhost:8080";
    private long timeoutMs = 2000;
    private boolean forceFailure = false;
    private long simulatedDelayMs = 0;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public boolean isForceFailure() {
        return forceFailure;
    }

    public void setForceFailure(boolean forceFailure) {
        this.forceFailure = forceFailure;
    }

    public long getSimulatedDelayMs() {
        return simulatedDelayMs;
    }

    public void setSimulatedDelayMs(long simulatedDelayMs) {
        this.simulatedDelayMs = simulatedDelayMs;
    }
}
