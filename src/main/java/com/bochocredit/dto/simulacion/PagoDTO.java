package com.bochocredit.dto.simulacion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoDTO {
    private Integer numCuota;
    private LocalDateTime fechaPago;
    private String tipoGracia;
    private Boolean estaPagado = false;


    private BigDecimal saldoInicial;
    private BigDecimal interes;
    private BigDecimal amortizacion;
    private BigDecimal seguroDesgravamen;
    private BigDecimal cuota;

    private BigDecimal seguroRiesgo;
    private BigDecimal otrosGastos;

    private BigDecimal saldoFinal;
    private BigDecimal flujo;
}
