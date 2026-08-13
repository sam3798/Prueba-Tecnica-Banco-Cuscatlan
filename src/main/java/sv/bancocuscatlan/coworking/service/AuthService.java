package sv.bancocuscatlan.coworking.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sv.bancocuscatlan.coworking.domain.Role;
import sv.bancocuscatlan.coworking.domain.Usuario;
import sv.bancocuscatlan.coworking.dto.auth.AuthResponse;
import sv.bancocuscatlan.coworking.dto.auth.LoginRequest;
import sv.bancocuscatlan.coworking.dto.auth.RegisterRequest;
import sv.bancocuscatlan.coworking.exception.BusinessException;
import sv.bancocuscatlan.coworking.repository.UsuarioRepository;
import sv.bancocuscatlan.coworking.security.JwtService;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("El username ya está registrado");
        }
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("El email ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRole(Role.USER);
        usuario.setActive(true);

        Usuario saved = usuarioRepository.save(usuario);
        String token = jwtService.generateToken(saved);
        return new AuthResponse(token, saved.getUsername(), saved.getRole().name());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        String token = jwtService.generateToken(usuario);
        return new AuthResponse(token, usuario.getUsername(), usuario.getRole().name());
    }
}
