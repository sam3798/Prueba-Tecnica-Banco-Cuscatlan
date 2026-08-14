package sv.bancocuscatlan.coworking.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sv.bancocuscatlan.coworking.config.JwtProperties;
import sv.bancocuscatlan.coworking.domain.Role;
import sv.bancocuscatlan.coworking.domain.Usuario;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("coworking-test-secret-key-must-be-long-enough-256bits");
        properties.setExpirationMs(3_600_000);
        jwtService = new JwtService(properties);
    }

    @Test
    void generateAndValidateToken() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("admin");
        usuario.setRole(Role.ADMIN);

        String token = jwtService.generateToken(usuario);

        assertEquals("admin", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, "admin"));
    }
}
