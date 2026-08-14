package sv.bancocuscatlan.coworking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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
import sv.bancocuscatlan.coworking.exception.OverlappingReservationException;
import sv.bancocuscatlan.coworking.mapper.ReservaMapper;
import sv.bancocuscatlan.coworking.payment.PaymentValidationClient;
import sv.bancocuscatlan.coworking.payment.PaymentValidationResponse;
import sv.bancocuscatlan.coworking.repository.EspacioRepository;
import sv.bancocuscatlan.coworking.repository.ReservaRepository;
import sv.bancocuscatlan.coworking.security.UsuarioPrincipal;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;
    @Mock
    private EspacioRepository espacioRepository;
    @Mock
    private ReservaMapper reservaMapper;
    @Mock
    private ReservationLifecycle reservationLifecycle;
    @Mock
    private PaymentValidationClient paymentValidationClient;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReservaService reservaService;

    private Usuario usuario;
    private UsuarioPrincipal principal;
    private Espacio espacio;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("user1");
        usuario.setEmail("user1@test.com");
        usuario.setRole(Role.USER);
        principal = new UsuarioPrincipal(usuario);

        espacio = new Espacio();
        espacio.setId(10L);
        espacio.setNombre("Sala A");
        espacio.setActivo(true);
        espacio.setTarifaHora(new BigDecimal("20.00"));
    }

    @Test
    void createThrowsWhenOverlapExists() {
        ReservaRequest request = new ReservaRequest();
        request.setEspacioId(10L);
        request.setInicio(Instant.parse("2026-08-20T10:00:00Z"));
        request.setFin(Instant.parse("2026-08-20T12:00:00Z"));

        when(espacioRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(espacio));
        when(reservaRepository.existsOverlap(eq(10L), any(), any())).thenReturn(true);

        assertThrows(OverlappingReservationException.class, () -> reservaService.create(request, principal));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void confirmUsesFallbackWhenPaymentCircuitOpens() {
        Reserva reserva = buildReserva(ReservationStatus.PENDING);
        ConfirmReservationRequest confirmRequest = new ConfirmReservationRequest();
        confirmRequest.setPaymentMethod("CARD");

        when(reservaRepository.findByIdWithDetails(5L)).thenReturn(Optional.of(reserva));
        when(paymentValidationClient.validate(any())).thenReturn(PaymentValidationResponse.fallback("CB open"));
        when(reservaRepository.save(reserva)).thenReturn(reserva);
        when(reservaMapper.toResponse(reserva)).thenReturn(new ReservaResponse());

        reservaService.confirm(5L, confirmRequest, principal);

        verify(reservationLifecycle).markPendingPayment(reserva);
        verify(reservationLifecycle, never()).confirm(reserva);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void confirmPublishesEventWhenPaymentApproved() {
        Reserva reserva = buildReserva(ReservationStatus.PENDING);
        ConfirmReservationRequest confirmRequest = new ConfirmReservationRequest();
        confirmRequest.setPaymentMethod("CARD");

        when(reservaRepository.findByIdWithDetails(5L)).thenReturn(Optional.of(reserva));
        when(paymentValidationClient.validate(any())).thenReturn(PaymentValidationResponse.approved("OK"));
        when(reservaRepository.save(reserva)).thenReturn(reserva);
        when(reservaMapper.toResponse(reserva)).thenReturn(new ReservaResponse());

        reservaService.confirm(5L, confirmRequest, principal);

        verify(reservationLifecycle).confirm(reserva);
        verify(eventPublisher).publishEvent(isA(ReservationConfirmedEvent.class));
    }

    private Reserva buildReserva(ReservationStatus status) {
        Reserva reserva = new Reserva();
        reserva.setId(5L);
        reserva.setUsuario(usuario);
        reserva.setEspacio(espacio);
        reserva.setInicio(Instant.parse("2026-08-20T10:00:00Z"));
        reserva.setFin(Instant.parse("2026-08-20T12:00:00Z"));
        reserva.setMontoTotal(new BigDecimal("40.00"));
        reserva.setEstado(status);
        return reserva;
    }
}
