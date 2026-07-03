package com.bochocredit.dto.usuario;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UsuarioDTO {
    private Long id;
    private String nombreCompleto;
    private String username;
    private String email;
    private String rol;
}
