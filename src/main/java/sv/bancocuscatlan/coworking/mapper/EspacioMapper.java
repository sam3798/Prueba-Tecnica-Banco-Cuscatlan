package sv.bancocuscatlan.coworking.mapper;

import org.springframework.stereotype.Component;

import sv.bancocuscatlan.coworking.domain.Espacio;
import sv.bancocuscatlan.coworking.dto.espacio.EspacioRequest;
import sv.bancocuscatlan.coworking.dto.espacio.EspacioResponse;

@Component
public class EspacioMapper {

    public Espacio toEntity(EspacioRequest request) {
        Espacio espacio = new Espacio();
        apply(request, espacio);
        return espacio;
    }

    public void updateEntity(EspacioRequest request, Espacio espacio) {
        apply(request, espacio);
    }

    public EspacioResponse toResponse(Espacio espacio) {
        EspacioResponse response = new EspacioResponse();
        response.setId(espacio.getId());
        response.setNombre(espacio.getNombre());
        response.setTipo(espacio.getTipo());
        response.setCapacidad(espacio.getCapacidad());
        response.setUbicacion(espacio.getUbicacion());
        response.setTarifaHora(espacio.getTarifaHora());
        response.setActivo(espacio.isActivo());
        response.setCreatedAt(espacio.getCreatedAt());
        response.setUpdatedAt(espacio.getUpdatedAt());
        return response;
    }

    private void apply(EspacioRequest request, Espacio espacio) {
        espacio.setNombre(request.getNombre());
        espacio.setTipo(request.getTipo());
        espacio.setCapacidad(request.getCapacidad());
        espacio.setUbicacion(request.getUbicacion());
        espacio.setTarifaHora(request.getTarifaHora());
        espacio.setActivo(request.getActivo() == null || request.getActivo());
    }
}
