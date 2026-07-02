package com.bochocredit.dto.usuario;

import lombok.Data;

@Data
public class UsuarioRequestDTO {
    private String nombreCompleto;
    private String username;
    private String email;
    private Long rol;
}
