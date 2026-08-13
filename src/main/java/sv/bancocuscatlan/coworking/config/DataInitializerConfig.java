package sv.bancocuscatlan.coworking.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import sv.bancocuscatlan.coworking.domain.Role;
import sv.bancocuscatlan.coworking.domain.Usuario;
import sv.bancocuscatlan.coworking.repository.UsuarioRepository;

@Configuration
public class DataInitializerConfig {

    @Bean
    @Profile("dev")
    CommandLineRunner seedAdmin(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!usuarioRepository.existsByUsername("admin")) {
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setEmail("admin@coworking.local");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                admin.setActive(true);
                usuarioRepository.save(admin);
            }
        };
    }
}
