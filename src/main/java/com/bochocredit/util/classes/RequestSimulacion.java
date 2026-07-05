package com.bochocredit.util.classes;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestSimulacion {

    @NotBlank(message = "La moneda es obligatoria")
    @Pattern(regexp = "soles|dolares", message = "Moneda inválida")
    private String moneda;

    @NotNull(message = "El precio del vehículo es obligatorio")
    @DecimalMin(value = "0.01")
    private double valorVenta;

    @NotNull(message = "El porcentaje de cuota inicial es obligatorio")
    @DecimalMin(value = "0.0") @DecimalMax(value = "90.0")
    private double cuotaInicialPct;

    @NotNull(message = "El porcentaje de cuota final es obligatorio")
    @DecimalMin(value = "0.0") @DecimalMax(value = "90.0")
    private double cuotaFinalPct;

    @NotBlank(message = "El tipo de tasa es obligatorio")
    @Pattern(regexp = "efectiva_mensual|efectiva_anual|nominal_anual")
    private String tipoTasa;

    @NotNull(message = "La capitalización es obligatoria")
    @Min(1) @Max(360)
    private int capitalizacion;

    @NotNull(message = "El valor de la tasa es obligatorio")
    @DecimalMin(value = "0.0")
    private double tasaInteres;

    @NotNull(message = "El plazo en meses es obligatorio")
    @Min(1) @Max(120)
    private int plazoMeses;

    @NotBlank(message = "El tipo de gracia es obligatorio")
    @Pattern(regexp = "ninguno|parcial|total|plan36|plan48")
    private String tipoGracia;

    @NotNull @Min(0) @Max(24)
    private int periodoGraciaMeses;

    @NotNull @DecimalMin(value = "0.0")
    private double seguroDesgPct;

    @NotNull @DecimalMin(value = "0.0")
    private double seguroVehicularPct;

    @NotNull @NotEmpty
    private Map<String, Double> gastosIniciales;

    @NotNull @NotEmpty
    private Map<String, Double> gastosPeriodicos;

    @NotNull @DecimalMin(value = "0.0")
    private double cok;

    private Long clienteId;

    private Long vehiculoId;

    private Long usuarioId;

    private boolean esEspecial;
}
