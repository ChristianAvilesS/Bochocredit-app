package com.bochocredit.util.classes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntradaCronograma {
    private int periodo;
    private String pg; // "S" (sin gracia/normal), "P" (parcial), "T" (total)

    // Cronograma "cuota final" (Compra Inteligente)
    private double saldoInicialCf;
    private double interesCf;
    private double amortCf;
    private double segDesgravCf;
    private double saldoFinalCf;

    // Cronograma regular
    private double saldoInicial;
    private double interes;
    private double cuota;
    private double amort;
    private double segDesgravCuota;
    private double segRiesgo;
    private double portes;
    private double gastosAdmin;
    private double gps;
    private double saldoFinal;
    private double flujo;
}
