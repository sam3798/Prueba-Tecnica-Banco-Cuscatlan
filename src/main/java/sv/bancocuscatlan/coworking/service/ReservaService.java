package sv.bancocuscatlan.coworking.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sv.bancocuscatlan.coworking.config.AppInfrastructureConfig;
import sv.bancocuscatlan.coworking.domain.Espacio;
import sv.bancocuscatlan.coworking.domain.Reserva;
import sv.bancocuscatlan.coworking.domain.ReservationStatus;
import sv.bancocuscatlan.coworking.domain.Role;
import sv.bancocuscatlan.coworking.domain.Usuario;
import sv.bancocuscatlan.coworking.domain.state.ReservationLifecycle;
import sv.bancocuscatlan.coworking.dto.reserva.ConfirmReservationRequest;
import sv.bancocuscatlan.coworking.dto.reserva.ReservaRequest;
import sv.bancocuscatlan.coworking.dto.reserva.ReservaResponse;
import sv.bancocuscatlan.coworking.event.ReservationConfirmedEvent;
import sv.bancocuscatlan.coworking.exception.BusinessException;
import sv.bancocuscatlan.coworking.exception.OverlappingReservationException;
import sv.bancocuscatlan.coworking.exception.ResourceNotFoundException;
import sv.bancocuscatlan.coworking.mapper.ReservaMapper;
import sv.bancocuscatlan.coworking.payment.PaymentValidationClient;
import sv.bancocuscatlan.coworking.payment.PaymentValidationRequest;
import sv.bancocuscatlan.coworking.payment.PaymentValidationResponse;
import sv.bancocuscatlan.coworking.repository.EspacioRepository;
import sv.bancocuscatlan.coworking.repository.ReservaRepository;
import sv.bancocuscatlan.coworking.security.UsuarioPrincipal;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final EspacioRepository espacioRepository;
    private final ReservaMapper reservaMapper;
    private final ReservationLifecycle reservationLifecycle;
    private final PaymentValidationClient paymentValidationClient;
    private final ApplicationEventPublisher eventPublisher;

    public ReservaService(
            ReservaRepository reservaRepository,
            EspacioRepository espacioRepository,
            ReservaMapper reservaMapper,
            ReservationLifecycle reservationLifecycle,
            PaymentValidationClient paymentValidationClient,
            ApplicationEventPublisher eventPublisher) {
        this.reservaRepository = reservaRepository;
        this.espacioRepository = espacioRepository;
        this.reservaMapper = reservaMapper;
        this.reservationLifecycle = reservationLifecycle;
        this.paymentValidationClient = paymentValidationClient;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    @CacheEvict(cacheNames = AppInfrastructureConfig.OCCUPANCY_CACHE, allEntries = true)
    public ReservaResponse create(ReservaRequest request, UsuarioPrincipal principal) {
        validateRange(request.getInicio(), request.getFin());

        Espacio espacio = espacioRepository.findByIdForUpdate(request.getEspacioId())
                .orElseThrow(() -> new ResourceNotFoundException("Espacio no encontrado: " + request.getEspacioId()));

        if (!espacio.isActivo()) {
            throw new BusinessException("El espacio no está activo");
        }

        if (reservaRepository.existsOverlap(espacio.getId(), request.getInicio(), request.getFin())) {
            throw new OverlappingReservationException(
                    "Ya existe una reserva que se solapa para el espacio en el rango indicado");
        }

        Reserva reserva = new Reserva();
        reserva.setUsuario(principal.getUsuario());
        reserva.setEspacio(espacio);
        reserva.setInicio(request.getInicio());
        reserva.setFin(request.getFin());
        reserva.setEstado(ReservationStatus.PENDING);
        reserva.setMontoTotal(calculateAmount(espacio.getTarifaHora(), request.getInicio(), request.getFin()));

        Reserva saved = reservaRepository.save(reserva);
        return reservaMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> findAll(UsuarioPrincipal principal) {
        List<Reserva> reservas = isAdmin(principal)
                ? reservaRepository.findAllWithDetails()
                : reservaRepository.findAllByUsuarioIdWithDetails(principal.getUsuario().getId());
        return reservas.stream().map(reservaMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ReservaResponse findById(Long id, UsuarioPrincipal principal) {
        Reserva reserva = getWithDetails(id);
        assertCanView(reserva, principal);
        return reservaMapper.toResponse(reserva);
    }

    @Transactional
    @CacheEvict(cacheNames = AppInfrastructureConfig.OCCUPANCY_CACHE, allEntries = true)
    public ReservaResponse confirm(Long id, ConfirmReservationRequest request, UsuarioPrincipal principal) {
        Reserva reserva = getWithDetails(id);
        assertCanManage(reserva, principal);

        PaymentValidationResponse payment = paymentValidationClient.validate(
                new PaymentValidationRequest(reserva.getId(), reserva.getMontoTotal(), request.getPaymentMethod()));

        if (payment.isFallbackUsed()) {
            reservationLifecycle.markPendingPayment(reserva);
        } else if (payment.isApproved()) {
            reservationLifecycle.confirm(reserva);
            eventPublisher.publishEvent(new ReservationConfirmedEvent(reserva));
        } else {
            throw new BusinessException("Pago rechazado: " + payment.getMessage());
        }

        return reservaMapper.toResponse(reservaRepository.save(reserva));
    }

    @Transactional
    @CacheEvict(cacheNames = AppInfrastructureConfig.OCCUPANCY_CACHE, allEntries = true)
    public ReservaResponse cancel(Long id, UsuarioPrincipal principal) {
        Reserva reserva = getWithDetails(id);
        assertCanManage(reserva, principal);
        reservationLifecycle.cancel(reserva);
        return reservaMapper.toResponse(reservaRepository.save(reserva));
    }

    @Transactional
    @CacheEvict(cacheNames = AppInfrastructureConfig.OCCUPANCY_CACHE, allEntries = true)
    public ReservaResponse complete(Long id, UsuarioPrincipal principal) {
        if (!isAdmin(principal)) {
            throw new AccessDeniedException("Solo un ADMIN puede completar reservas");
        }
        Reserva reserva = getWithDetails(id);
        reservationLifecycle.complete(reserva);
        return reservaMapper.toResponse(reservaRepository.save(reserva));
    }

    private Reserva getWithDetails(Long id) {
        return reservaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada: " + id));
    }

    private void assertCanView(Reserva reserva, UsuarioPrincipal principal) {
        if (!isAdmin(principal) && !owns(reserva, principal.getUsuario())) {
            throw new AccessDeniedException("No tienes acceso a esta reserva");
        }
    }

    private void assertCanManage(Reserva reserva, UsuarioPrincipal principal) {
        if (!isAdmin(principal) && !owns(reserva, principal.getUsuario())) {
            throw new AccessDeniedException("No puedes gestionar esta reserva");
        }
    }

    private boolean owns(Reserva reserva, Usuario usuario) {
        return reserva.getUsuario().getId().equals(usuario.getId());
    }

    private boolean isAdmin(UsuarioPrincipal principal) {
        return principal.getUsuario().getRole() == Role.ADMIN;
    }

    private void validateRange(Instant inicio, Instant fin) {
        if (!fin.isAfter(inicio)) {
            throw new BusinessException("La fecha/hora de fin debe ser posterior al inicio");
        }
        if (Duration.between(inicio, fin).toMinutes() < 30) {
            throw new BusinessException("La reserva mínima es de 30 minutos");
        }
    }

    private BigDecimal calculateAmount(BigDecimal tarifaHora, Instant inicio, Instant fin) {
        long minutes = Duration.between(inicio, fin).toMinutes();
        BigDecimal hours = BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        return tarifaHora.multiply(hours).setScale(2, RoundingMode.HALF_UP);
    }
}
