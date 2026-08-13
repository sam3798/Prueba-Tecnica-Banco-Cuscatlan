package sv.bancocuscatlan.coworking.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sv.bancocuscatlan.coworking.domain.Espacio;
import sv.bancocuscatlan.coworking.dto.espacio.EspacioRequest;
import sv.bancocuscatlan.coworking.dto.espacio.EspacioResponse;
import sv.bancocuscatlan.coworking.exception.ResourceNotFoundException;
import sv.bancocuscatlan.coworking.mapper.EspacioMapper;
import sv.bancocuscatlan.coworking.repository.EspacioRepository;

@Service
public class EspacioService {

    private final EspacioRepository espacioRepository;
    private final EspacioMapper espacioMapper;

    public EspacioService(EspacioRepository espacioRepository, EspacioMapper espacioMapper) {
        this.espacioRepository = espacioRepository;
        this.espacioMapper = espacioMapper;
    }

    @Transactional(readOnly = true)
    public List<EspacioResponse> findAll() {
        return espacioRepository.findAll().stream()
                .map(espacioMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EspacioResponse findById(Long id) {
        return espacioMapper.toResponse(getEspacioOrThrow(id));
    }

    @Transactional
    public EspacioResponse create(EspacioRequest request) {
        Espacio espacio = espacioMapper.toEntity(request);
        return espacioMapper.toResponse(espacioRepository.save(espacio));
    }

    @Transactional
    public EspacioResponse update(Long id, EspacioRequest request) {
        Espacio espacio = getEspacioOrThrow(id);
        espacioMapper.updateEntity(request, espacio);
        return espacioMapper.toResponse(espacioRepository.save(espacio));
    }

    @Transactional
    public void delete(Long id) {
        Espacio espacio = getEspacioOrThrow(id);
        espacioRepository.delete(espacio);
    }

    private Espacio getEspacioOrThrow(Long id) {
        return espacioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Espacio no encontrado: " + id));
    }
}
