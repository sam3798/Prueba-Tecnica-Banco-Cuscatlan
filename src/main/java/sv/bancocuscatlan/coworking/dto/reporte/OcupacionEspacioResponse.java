package sv.bancocuscatlan.coworking.dto.reporte;

import java.math.BigDecimal;

public class OcupacionEspacioResponse {

    private Long espacioId;
    private String espacioNombre;
    private BigDecimal ocupacionPorcentaje;
    private long minutosReservados;
    private long minutosPeriodo;

    public OcupacionEspacioResponse() {
    }

    public OcupacionEspacioResponse(
            Long espacioId,
            String espacioNombre,
            BigDecimal ocupacionPorcentaje,
            long minutosReservados,
            long minutosPeriodo) {
        this.espacioId = espacioId;
        this.espacioNombre = espacioNombre;
        this.ocupacionPorcentaje = ocupacionPorcentaje;
        this.minutosReservados = minutosReservados;
        this.minutosPeriodo = minutosPeriodo;
    }

    public Long getEspacioId() {
        return espacioId;
    }

    public void setEspacioId(Long espacioId) {
        this.espacioId = espacioId;
    }

    public String getEspacioNombre() {
        return espacioNombre;
    }

    public void setEspacioNombre(String espacioNombre) {
        this.espacioNombre = espacioNombre;
    }

    public BigDecimal getOcupacionPorcentaje() {
        return ocupacionPorcentaje;
    }

    public void setOcupacionPorcentaje(BigDecimal ocupacionPorcentaje) {
        this.ocupacionPorcentaje = ocupacionPorcentaje;
    }

    public long getMinutosReservados() {
        return minutosReservados;
    }

    public void setMinutosReservados(long minutosReservados) {
        this.minutosReservados = minutosReservados;
    }

    public long getMinutosPeriodo() {
        return minutosPeriodo;
    }

    public void setMinutosPeriodo(long minutosPeriodo) {
        this.minutosPeriodo = minutosPeriodo;
    }
}
