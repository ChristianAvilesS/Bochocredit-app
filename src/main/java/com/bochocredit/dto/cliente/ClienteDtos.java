package com.bochocredit.dto.cliente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class ClienteDtos {

    public record ClienteRequest(
            @NotBlank(message = "Los nombres son obligatorios") String nombres,
            @NotBlank(message = "Los apellidos son obligatorios") String apellidos,
            @NotBlank(message = "El DNI es obligatorio")
            @Size(min = 8, max = 15, message = "El DNI debe tener entre 8 y 15 caracteres") String dni,
            @NotBlank(message = "El teléfono es obligatorio") String telefono,
            @NotBlank(message = "El email es obligatorio") @Email(message = "Email inválido") String email,
            @NotBlank(message = "La dirección es obligatoria") String direccion
    ) {}

    public record ClienteResponse(
            Long id,
            String nombres,
            String apellidos,
            String nombreCompleto,
            String dni,
            String telefono,
            String email,
            String direccion,
            LocalDateTime createdAt
    ) {}
}
