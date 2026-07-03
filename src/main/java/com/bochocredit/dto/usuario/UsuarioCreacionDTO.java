package com.bochocredit.dto.usuario;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UsuarioCreacionDTO {
    private String username;
    private String password;
}
