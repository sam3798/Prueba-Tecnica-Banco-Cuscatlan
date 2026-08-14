package sv.bancocuscatlan.coworking.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sv.bancocuscatlan.coworking.config.AppInfrastructureConfig;
import sv.bancocuscatlan.coworking.domain.Espacio;
import sv.bancocuscatlan.coworking.domain.Reserva;
import sv.bancocuscatlan.coworking.domain.ReservationStatus;
import sv.bancocuscatlan.coworking.dto.reporte.OcupacionEspacioResponse;
import sv.bancocuscatlan.coworking.exception.BusinessException;
import sv.bancocuscatlan.coworking.repository.EspacioRepository;
import sv.bancocuscatlan.coworking.repository.ReservaRepository;

@Service
public class ReporteService {

    private final EspacioRepository espacioRepository;
    private final ReservaRepository reservaRepository;

    public ReporteService(EspacioRepository espacioRepository, ReservaRepository reservaRepository) {
        this.espacioRepository = espacioRepository;
        this.reservaRepository = reservaRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = AppInfrastructureConfig.OCCUPANCY_CACHE,
            key = "#desde.toString() + '-' + #hasta.toString()")
    public List<OcupacionEspacioResponse> calcularOcupacion(Instant desde, Instant hasta) {
        if (!hasta.isAfter(desde)) {
            throw new BusinessException("El rango de fechas es inválido");
        }

        long minutosPeriodo = Duration.between(desde, hasta).toMinutes();
        if (minutosPeriodo <= 0) {
            throw new BusinessException("El rango de fechas debe cubrir al menos un minuto");
        }

        List<ReservationStatus> estadosOcupan = List.of(
                ReservationStatus.PENDING,
                ReservationStatus.PENDING_PAYMENT,
                ReservationStatus.CONFIRMED);

        List<Reserva> reservas = reservaRepository.findForOccupancy(desde, hasta, estadosOcupan);
        Map<Long, Long> minutosPorEspacio = new HashMap<>();
        Map<Long, String> nombres = new HashMap<>();

        for (Reserva reserva : reservas) {
            Espacio espacio = reserva.getEspacio();
            nombres.put(espacio.getId(), espacio.getNombre());

            Instant overlapStart = reserva.getInicio().isBefore(desde) ? desde : reserva.getInicio();
            Instant overlapEnd = reserva.getFin().isAfter(hasta) ? hasta : reserva.getFin();
            long minutos = Math.max(0, Duration.between(overlapStart, overlapEnd).toMinutes());
            minutosPorEspacio.merge(espacio.getId(), minutos, Long::sum);
        }

        List<OcupacionEspacioResponse> result = new ArrayList<>();
        for (Espacio espacio : espacioRepository.findAll()) {
            long minutosReservados = minutosPorEspacio.getOrDefault(espacio.getId(), 0L);
            BigDecimal porcentaje = BigDecimal.valueOf(minutosReservados)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(minutosPeriodo), 2, RoundingMode.HALF_UP);

            result.add(new OcupacionEspacioResponse(
                    espacio.getId(),
                    espacio.getNombre(),
                    porcentaje,
                    minutosReservados,
                    minutosPeriodo));
        }

        result.sort((a, b) -> a.getEspacioId().compareTo(b.getEspacioId()));
        return result;
    }
}
