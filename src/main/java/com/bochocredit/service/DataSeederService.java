package com.bochocredit.service;

import com.bochocredit.entity.Rol;
import com.bochocredit.entity.Usuario;
import com.bochocredit.repository.RolRepository;
import com.bochocredit.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea el usuario administrador por defecto al arrancar la aplicación,
 * si todavía no existe. Equivale al `INSERT INTO users(...)` que el
 * Flask original ejecutaba dentro de `init_db()`.
 *
 * Se hace aquí (en vez de en una migración SQL) para no incrustar un
 * hash BCrypt fijo en el historial de Flyway.
 */
@Component
@RequiredArgsConstructor
public class DataSeederService implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.existsByUsername("admin")) {
            return;
        }

        Rol rolAdmin = rolRepository.findByNombreRol("ADMIN")
                .orElseGet(() -> rolRepository.save(Rol.builder().nombreRol("ADMIN").build()));

        Usuario admin = Usuario.builder()
                .nombreCompleto("Administrador")
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .email("admin@bochocredit.pe")
                .activo(true)
                .rol(rolAdmin)
                .build();

        usuarioRepository.save(admin);
    }
}
