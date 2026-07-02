package com.bochocredit.security;

import com.bochocredit.entity.Usuario;
import com.bochocredit.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        String rol = usuario.getRol() != null ? usuario.getRol().getNombreRol() : "USER";

        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                usuario.getActivo(),
                true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + rol))
        );
    }
}
