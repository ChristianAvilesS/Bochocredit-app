package com.bochocredit.dto.auth;

import jakarta.validation.constraints.NotBlank;

public class AuthDtos {

    public record LoginRequest(
            @NotBlank(message = "El usuario es obligatorio") String username,
            @NotBlank(message = "La contraseña es obligatoria") String password
    ) {}

    public record ChangePasswordRequestDTO(
            @NotBlank(message = "El usuario es obligatorio") String username,
            @NotBlank(message = "La contraseña es obligatoria") String passwordAntigua,
            @NotBlank(message = "La contraseña es obligatoria") String passwordNueva
    ) {}

    public record LoginResponse(
            String token,
            String tokenType,
            Long userId,
            String username,
            String nombreCompleto,
            String rol
    ) {
        public static LoginResponse of(String token, Long userId, String username, String nombreCompleto, String rol) {
            return new LoginResponse(token, "Bearer", userId, username, nombreCompleto, rol);
        }
    }
}
