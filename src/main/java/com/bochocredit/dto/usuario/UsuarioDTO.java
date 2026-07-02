package com.bochocredit.dto.usuario;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UsuarioDTO {
    private String nombreCompleto;
    private String username;
    private String email;
}
