package com.bochocredit.dto.vehiculo;

import com.bochocredit.entity.Vehiculo;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class VehiculoDtos {

    public record VehiculoRequest(
            @NotBlank(message = "La marca es obligatoria") String marca,
            @NotBlank(message = "El modelo es obligatorio") String modelo,
            @NotNull(message = "El año es obligatorio")
            @Min(value = 1990, message = "Año inválido") Integer anio,
            @NotNull(message = "El precio es obligatorio")
            @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0") BigDecimal precio,
            String descripcion,
            Vehiculo.Disponibilidad disponibilidad
    ) {}

    public record VehiculoResponse(
            Long id,
            String marca,
            String modelo,
            Integer anio,
            BigDecimal precio,
            String descripcion,
            Vehiculo.Disponibilidad disponibilidad,
            boolean bloqueado,
            String bloqueadoHasta
    ) {}
}
