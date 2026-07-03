package com.bochocredit.dto.simulacion;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Cuerpo de la solicitud para crear o recalcular una simulación de crédito.
 * Equivalente a los campos leídos de `request.form` / `request.get_json()`
 * en las rutas Flask `/creditos/nuevo`, `/creditos/<id>/editar` y `/api/calcular`.
 */
public record SimulacionRequest(

        @NotNull(message = "El cliente es obligatorio") Long clienteId,
        @NotNull(message = "El vehículo es obligatorio") Long vehiculoId,

        @NotBlank(message = "La moneda es obligatoria")
        @Pattern(regexp = "soles|dolares", message = "Moneda inválida") String moneda,

        @NotNull(message = "El precio del vehículo es obligatorio")
        @DecimalMin(value = "0.01") BigDecimal precioVehiculo,

        @NotNull(message = "El porcentaje de cuota inicial es obligatorio")
        @DecimalMin(value = "0.0") @DecimalMax(value = "90.0") BigDecimal cuotaInicialPct,

        @NotNull(message = "El plazo en meses es obligatorio")
        @Min(1) @Max(120) Integer plazoMeses,

        @NotBlank(message = "El tipo de tasa es obligatorio")
        @Pattern(regexp = "efectiva_mensual|efectiva_anual|nominal_anual") String tipoTasa,

        @NotNull(message = "El valor de la tasa es obligatorio")
        @DecimalMin(value = "0.0") BigDecimal tasaValor,

        String capitalizacion, // requerido solo si tipoTasa = nominal_anual

        @NotBlank(message = "El tipo de gracia es obligatorio")
        @Pattern(regexp = "ninguno|parcial|total") String graciaTipo,

        @NotNull @Min(0) @Max(24) Integer graciaMeses,

        @NotNull @DecimalMin(value = "0.0") BigDecimal tsd,
        @NotNull @DecimalMin(value = "0.0") BigDecimal tsv,
        @NotNull @DecimalMin(value = "0.0") BigDecimal portes,

        BigDecimal gastosAdmin,
        BigDecimal gps,

        @Pattern(regexp = "regular|aleman|americano|peruano|compra_inteligente")
        String metodoPago,

        // Solo para metodoPago = compra_inteligente
        BigDecimal pctCuotaFinal, // en porcentaje (ej. 40 = 40%)
        BigDecimal cok            // en porcentaje (ej. 50 = 50%)
) {
    public String metodoPagoOrDefault() {
        return (metodoPago == null || metodoPago.isBlank()) ? "regular" : metodoPago;
    }

    public BigDecimal gastosAdminOrZero() {
        return gastosAdmin == null ? BigDecimal.ZERO : gastosAdmin;
    }

    public BigDecimal gpsOrZero() {
        return gps == null ? BigDecimal.ZERO : gps;
    }

    public BigDecimal pctCuotaFinalOrDefault() {
        return pctCuotaFinal == null ? BigDecimal.valueOf(40) : pctCuotaFinal;
    }

    public BigDecimal cokOrDefault() {
        return cok == null ? BigDecimal.valueOf(50) : cok;
    }

    public String capitalizacionOrDefault() {
        return (capitalizacion == null || capitalizacion.isBlank()) ? "mensual" : capitalizacion;
    }
}
