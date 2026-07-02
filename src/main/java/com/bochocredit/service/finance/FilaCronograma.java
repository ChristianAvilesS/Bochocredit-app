package com.bochocredit.service.finance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Representa una fila del cronograma de pagos.
 * Equivalente al diccionario `filas.append({...})` del código Python original.
 * Se serializa tal cual a JSON para persistirse en la columna `cronograma` (JSONB).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilaCronograma {

    private int periodo;
    private String pg; // "S" (sin gracia/normal), "P" (parcial), "T" (total)

    // Cronograma "cuota final" (Compra Inteligente)
    @Builder.Default
    private BigDecimal saldoInicialCf = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal interesCf = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal amortCf = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal segDesgravCf = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal saldoFinalCf = BigDecimal.ZERO;

    // Cronograma regular
    private BigDecimal saldoInicial;
    private BigDecimal interes;
    private BigDecimal amort;
    private BigDecimal cuotaCapital;
    private BigDecimal segDesgrav;
    private BigDecimal segVeh;
    private BigDecimal portes;
    @Builder.Default
    private BigDecimal gastosAdmin = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal gps = BigDecimal.ZERO;
    private BigDecimal cuotaTotal;
    private BigDecimal saldoFinal;
}
