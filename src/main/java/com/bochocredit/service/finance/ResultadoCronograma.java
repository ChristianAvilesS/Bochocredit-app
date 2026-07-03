package com.bochocredit.service.finance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resultado de generar un cronograma: filas + indicadores VAN / TIR / TCEA.
 * Equivalente a la tupla `(filas, van, tir, tcea)` retornada por las funciones
 * `generar_cronograma_*` en el código Python original.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoCronograma {
    private List<FilaCronograma> filas;
    private BigDecimal van;
    private BigDecimal tirMensualPct;
    private BigDecimal tceaPct;
}
