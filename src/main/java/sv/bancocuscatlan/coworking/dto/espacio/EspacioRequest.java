package sv.bancocuscatlan.coworking.dto.espacio;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import sv.bancocuscatlan.coworking.domain.SpaceType;

public class EspacioRequest {

    @NotBlank
    @Size(max = 120)
    private String nombre;

    @NotNull
    private SpaceType tipo;

    @NotNull
    @Min(1)
    private Integer capacidad;

    @NotBlank
    @Size(max = 150)
    private String ubicacion;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal tarifaHora;

    private Boolean activo = true;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public SpaceType getTipo() {
        return tipo;
    }

    public void setTipo(SpaceType tipo) {
        this.tipo = tipo;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public BigDecimal getTarifaHora() {
        return tarifaHora;
    }

    public void setTarifaHora(BigDecimal tarifaHora) {
        this.tarifaHora = tarifaHora;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
