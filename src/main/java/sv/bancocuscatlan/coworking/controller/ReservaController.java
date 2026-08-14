package sv.bancocuscatlan.coworking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import sv.bancocuscatlan.coworking.dto.reserva.ConfirmReservationRequest;
import sv.bancocuscatlan.coworking.dto.reserva.ReservaRequest;
import sv.bancocuscatlan.coworking.dto.reserva.ReservaResponse;
import sv.bancocuscatlan.coworking.security.UsuarioPrincipal;
import sv.bancocuscatlan.coworking.service.ReservaService;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping
    public ResponseEntity<ReservaResponse> create(
            @Valid @RequestBody ReservaRequest request,
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.create(request, principal));
    }

    @GetMapping
    public ResponseEntity<List<ReservaResponse>> findAll(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return ResponseEntity.ok(reservaService.findAll(principal));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponse> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        return ResponseEntity.ok(reservaService.findById(id, principal));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<ReservaResponse> confirm(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmReservationRequest request,
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        return ResponseEntity.ok(reservaService.confirm(id, request, principal));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ReservaResponse> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        return ResponseEntity.ok(reservaService.cancel(id, principal));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ReservaResponse> complete(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        return ResponseEntity.ok(reservaService.complete(id, principal));
    }
}
