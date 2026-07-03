package com.bochocredit.service;

import com.bochocredit.dto.auth.AuthDtos.LoginRequest;
import com.bochocredit.dto.auth.AuthDtos.LoginResponse;
import com.bochocredit.entity.Usuario;
import com.bochocredit.repository.UsuarioRepository;
import com.bochocredit.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        // Lanza BadCredentialsException si usuario/clave no coinciden
        // (capturada centralmente por GlobalExceptionHandler -> 401).
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado pero no encontrado en BD"));

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities("ROLE_" + (usuario.getRol() != null ? usuario.getRol().getNombreRol() : "USER"))
                .build();

        String token = jwtService.generateToken(userDetails);

        String rol = usuario.getRol() != null ? usuario.getRol().getNombreRol() : "USER";

        return LoginResponse.of(token, usuario.getId(), usuario.getUsername(), usuario.getNombreCompleto(), rol);
    }
}
