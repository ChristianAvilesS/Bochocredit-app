package com.bochocredit.util.classes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParametrosCalculo {
    private double valorVenta;
    private int cantPagos;
    private double valorPrestamo;
    private double cuotaInicial;
    private double cuotaFinal;
    private double saldoFinanciado;
    private double tem;
    private double seguroDesgravamenPeriodo;
    private double seguroVehicular;
    private double portes;
    private double gastosAdmin;
    private double gps;
    private String tipoGracia;
    private int mesesGracia;
}
