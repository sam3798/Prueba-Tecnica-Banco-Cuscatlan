package sv.bancocuscatlan.coworking.dto.reserva;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;

public class ReservaRequest {

    @NotNull
    private Long espacioId;

    @NotNull
    private Instant inicio;

    @NotNull
    private Instant fin;

    public Long getEspacioId() {
        return espacioId;
    }

    public void setEspacioId(Long espacioId) {
        this.espacioId = espacioId;
    }

    public Instant getInicio() {
        return inicio;
    }

    public void setInicio(Instant inicio) {
        this.inicio = inicio;
    }

    public Instant getFin() {
        return fin;
    }

    public void setFin(Instant fin) {
        this.fin = fin;
    }
}
