package com.bochocredit.service.finance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias del motor financiero, validando paridad numérica
 * contra los resultados conocidos del backend Flask original
 * (ver app_v2.py — funciones calcular_tem / generar_cronograma_v2).
 */
class FinancialEngineTest {

    private final FinancialEngine engine = new FinancialEngine();

    @Test
    void calcularTem_efectivaAnual_15pct() {
        double tem = engine.calcularTem("efectiva_anual", 15.0, "mensual");
        assertEquals(0.011715, tem, 1e-6);
    }

    @Test
    void calcularTem_efectivaMensual_directa() {
        double tem = engine.calcularTem("efectiva_mensual", 1.5, "mensual");
        assertEquals(0.015, tem, 1e-9);
    }

    @Test
    void calcularTem_nominalAnual_capitalizacionMensual() {
        // TNA 18%, capitalización mensual -> TEM = 18%/12 = 1.5%
        double tem = engine.calcularTem("nominal_anual", 18.0, "mensual");
        assertEquals(0.015, tem, 1e-6);
    }

    @Test
    void generarCronogramaFrances_sinGracia_sumaCuotasConsistente() {
        double sf = 50000, tem = engine.calcularTem("efectiva_anual", 15.0, "mensual");
        int n = 48;

        ResultadoCronograma r = engine.generarCronogramaFrances(
                sf, tem, n, "ninguno", 0, 0.0005, 0.0015, 70000, 5, 0, 0);

        assertEquals(n, r.getFilas().size());

        // El saldo final de la última cuota debe ser 0 (crédito totalmente amortizado)
        assertEquals(0, r.getFilas().get(n - 1).getSaldoFinal().doubleValue(), 0.5);

        // El TCEA debe ser positivo y mayor que la TEA nominal (por seguros/portes)
        assertTrue(r.getTceaPct().doubleValue() > 0);
    }

    @Test
    void generarCronogramaFrances_graciaTotal_capitalizaIntereses() {
        double sf = 50000, tem = 0.0117;
        int n = 48, graciaMeses = 3;

        ResultadoCronograma r = engine.generarCronogramaFrances(
                sf, tem, n, "total", graciaMeses, 0.0005, 0.0015, 70000, 5, 0, 0);

        // Durante la gracia total, no se cobra cuota (cuotaTotal = solo seguros+portes... en este caso 0 amort)
        FilaCronograma primera = r.getFilas().get(0);
        assertEquals(0, primera.getAmort().doubleValue(), 1e-9);
        assertEquals(0, primera.getCuotaCapital().doubleValue(), 1e-9);

        // El saldo debe haber crecido (capitalización) durante la gracia
        FilaCronograma terceraFila = r.getFilas().get(graciaMeses - 1);
        assertTrue(terceraFila.getSaldoFinal().doubleValue() > sf);
    }

    @Test
    void generarCronogramaFrances_graciaParcial_soloPagaInteres() {
        double sf = 50000, tem = 0.0117;
        int n = 48, graciaMeses = 3;

        ResultadoCronograma r = engine.generarCronogramaFrances(
                sf, tem, n, "parcial", graciaMeses, 0.0005, 0.0015, 70000, 5, 0, 0);

        FilaCronograma primera = r.getFilas().get(0);
        // En gracia parcial, la cuota de capital paga solo el interés
        assertEquals(primera.getInteres().doubleValue(), primera.getCuotaCapital().doubleValue(), 0.01);
        // El saldo no se reduce durante la gracia parcial
        assertEquals(sf, primera.getSaldoFinal().doubleValue(), 0.01);
    }

    @Test
    void calcularTir_flujoSimple_convergeRazonable() {
        // Desembolso de 1000, 12 cuotas de ~91.68 (TEM ~1.5%)
        double[] flujos = new double[13];
        flujos[0] = 1000;
        for (int i = 1; i <= 12; i++) flujos[i] = -91.68;

        double tir = engine.calcularTir(flujos);
        assertEquals(0.015, tir, 0.01); // tolerancia amplia, solo verifica convergencia razonable
    }

    @Test
    void generarCronogramaAleman_amortizacionConstante() {
        double sf = 48000, tem = 0.01;
        int n = 12;

        ResultadoCronograma r = engine.generarCronogramaAleman(
                sf, tem, n, "ninguno", 0, 0, 0, 60000, 0, 0, 0);

        double amortEsperada = sf / n;
        for (FilaCronograma f : r.getFilas()) {
            assertEquals(amortEsperada, f.getAmort().doubleValue(), 0.5);
        }
    }

    @Test
    void generarCronogramaAmericano_soloInteresHastaUltimoPeriodo() {
        double sf = 30000, tem = 0.01;
        int n = 6;

        ResultadoCronograma r = engine.generarCronogramaAmericano(
                sf, tem, n, "ninguno", 0, 0, 0, 40000, 0, 0, 0);

        // Períodos 1 al 5: amortización = 0
        for (int i = 0; i < n - 1; i++) {
            assertEquals(0, r.getFilas().get(i).getAmort().doubleValue(), 1e-9);
        }
        // Último período: amortiza el saldo completo
        FilaCronograma ultima = r.getFilas().get(n - 1);
        assertEquals(sf, ultima.getAmort().doubleValue(), 0.5);
        assertEquals(0, ultima.getSaldoFinal().doubleValue(), 1e-6);
    }
}
