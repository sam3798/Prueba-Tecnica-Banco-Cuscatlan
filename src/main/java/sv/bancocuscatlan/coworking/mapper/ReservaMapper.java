package sv.bancocuscatlan.coworking.mapper;

import org.springframework.stereotype.Component;

import sv.bancocuscatlan.coworking.domain.Reserva;
import sv.bancocuscatlan.coworking.dto.reserva.ReservaResponse;

@Component
public class ReservaMapper {

    public ReservaResponse toResponse(Reserva reserva) {
        ReservaResponse response = new ReservaResponse();
        response.setId(reserva.getId());
        response.setEspacioId(reserva.getEspacio().getId());
        response.setEspacioNombre(reserva.getEspacio().getNombre());
        response.setUsuarioId(reserva.getUsuario().getId());
        response.setUsername(reserva.getUsuario().getUsername());
        response.setInicio(reserva.getInicio());
        response.setFin(reserva.getFin());
        response.setEstado(reserva.getEstado());
        response.setMontoTotal(reserva.getMontoTotal());
        response.setCreatedAt(reserva.getCreatedAt());
        response.setUpdatedAt(reserva.getUpdatedAt());
        return response;
    }
}
