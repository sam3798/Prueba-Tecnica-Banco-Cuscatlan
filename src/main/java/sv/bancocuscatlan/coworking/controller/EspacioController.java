package sv.bancocuscatlan.coworking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import sv.bancocuscatlan.coworking.dto.espacio.EspacioRequest;
import sv.bancocuscatlan.coworking.dto.espacio.EspacioResponse;
import sv.bancocuscatlan.coworking.service.EspacioService;

@RestController
@RequestMapping("/api/espacios")
public class EspacioController {

    private final EspacioService espacioService;

    public EspacioController(EspacioService espacioService) {
        this.espacioService = espacioService;
    }

    @GetMapping
    public ResponseEntity<List<EspacioResponse>> findAll() {
        return ResponseEntity.ok(espacioService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EspacioResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(espacioService.findById(id));
    }

    @PostMapping
    public ResponseEntity<EspacioResponse> create(@Valid @RequestBody EspacioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(espacioService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EspacioResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody EspacioRequest request) {
        return ResponseEntity.ok(espacioService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        espacioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
