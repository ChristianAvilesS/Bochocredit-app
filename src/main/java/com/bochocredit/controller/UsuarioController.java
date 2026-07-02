package com.bochocredit.controller;

import com.bochocredit.dto.auth.AuthDtos;
import com.bochocredit.dto.usuario.UsuarioCreacionDTO;
import com.bochocredit.dto.usuario.UsuarioDTO;
import com.bochocredit.dto.usuario.UsuarioRequestDTO;
import com.bochocredit.entity.Usuario;
import com.bochocredit.service.AuthService;
import com.bochocredit.service.UsuarioService;
import com.bochocredit.util.PasswordGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService servicio;
    private final PasswordGenerator generator;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listar() {
        return ResponseEntity.ok(servicio.listarUsuarios().stream().map(usuario ->
            new UsuarioDTO(usuario.getNombreCompleto(), usuario.getUsername(), usuario.getEmail())
        ).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<UsuarioCreacionDTO> crear(@Valid @RequestBody UsuarioRequestDTO request) {
        Usuario u = new Usuario();
        u.setActivo(true);
        u.setRol(servicio.obtenerRol(request.getRol()));
        u.setNombreCompleto(request.getNombreCompleto());
        u.setUsername(request.getUsername());
        u.setEmail(request.getEmail());
        u.setFechaModificacion(null);

        u = servicio.guardarUsuario(u);

        var dto = new UsuarioCreacionDTO(u.getUsername(), generator.generarPassword(u.getId()));

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/cambiar-password/{id}")
    public ResponseEntity<Boolean> cambiarPassword(@PathVariable Long id,
                                                      @Valid @RequestBody AuthDtos.ChangePasswordRequestDTO request) {
        var login = new AuthDtos.LoginRequest(request.username(), request.passwordAntigua());
        if (authService.login(login).userId() != null)
            return ResponseEntity.ok(servicio.cambiarPassword(id, request.passwordNueva()));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/renovar-password/{id}")
    public ResponseEntity<Boolean> necesitaRenovarPassword(@PathVariable Long id) {
        return ResponseEntity.ok(servicio.necesitaRenovarPassword(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminarUsuario(id);
        return ResponseEntity.ok(null);
    }
}
