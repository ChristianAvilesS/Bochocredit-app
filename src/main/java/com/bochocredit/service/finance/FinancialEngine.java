package com.bochocredit.service.finance;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Motor de cálculo financiero de BochoCredit.
 *
 * Port directo y fiel de las funciones Python del backend Flask original:
 *   - calcular_tem
 *   - calcular_tir (Newton-Raphson)
 *   - generar_cronograma_v2        (Francés vencido ordinario)
 *   - generar_cronograma_aleman    (Amortización constante)
 *   - generar_cronograma_americano (Solo interés + bullet final)
 *   - generar_cronograma_peruano   (Francés con doble cuota en julio/diciembre)
 *   - generar_cronograma_compra_inteligente (Francés + cuotón final + COK)
 *
 * Se usa {@code double} internamente (igual que el Python original, que usa
 * float nativo) para mantener exactamente el mismo comportamiento numérico;
 * la conversión a BigDecimal ocurre solo en el límite del motor, antes de
 * persistir/devolver resultados.
 */
@Component
public class FinancialEngine {

    // ─────────────────────────────────────────────
    // TEM — conversión de tasas
    // ─────────────────────────────────────────────

    /**
     * Calcula la Tasa Efectiva Mensual (TEM) a partir del tipo de tasa configurado.
     *
     * @param tipoTasa      "efectiva_mensual" | "efectiva_anual" | "nominal_anual"
     * @param tasaValorPct  valor de la tasa en porcentaje (ej. 15.0 para 15%)
     * @param capitalizacion nombre de la capitalización (diaria, mensual, etc.) — solo
     *                      relevante para tasa nominal anual
     * @param diasTasa      base de días anual de la tasa (360 por defecto, Perú)
     */
    public double calcularTem(String tipoTasa, double tasaValorPct, String capitalizacion, int diasTasa) {
        double tasa = tasaValorPct / 100.0;
        int capDays = capDiasDe(capitalizacion);
        double n = 30.0 / capDays;

        if (tipoTasa.contains("efectiva_mensual")) {
            return tasa;
        } else if (tipoTasa.contains("efectiva_anual")) {
            return Math.pow(1 + tasa, 1.0 / 12.0) - 1;
        } else {
            // nominal_anual
            double m = diasTasa / (double) capDays;
            return Math.pow(1 + tasa / m, n) - 1;
        }
    }

    public double calcularTem(String tipoTasa, double tasaValorPct, String capitalizacion) {
        return calcularTem(tipoTasa, tasaValorPct, capitalizacion, 360);
    }

    private int capDiasDe(String capitalizacion) {
        if (capitalizacion == null) return 30;
        return switch (capitalizacion) {
            case "diaria" -> 1;
            case "quincenal" -> 15;
            case "mensual" -> 30;
            case "bimestral" -> 60;
            case "trimestral" -> 90;
            case "cuatrimestral" -> 120;
            case "semestral" -> 180;
            case "anual" -> 360;
            default -> 30;
        };
    }

    // ─────────────────────────────────────────────
    // TIR — Newton-Raphson
    // ─────────────────────────────────────────────

    /**
     * Calcula la TIR mensual de un flujo de caja mediante Newton-Raphson.
     * flujos[0] es el desembolso inicial (positivo); flujos[1..n] son las
     * cuotas pagadas (negativas).
     */
    public double calcularTir(double[] flujos) {
        return calcularTir(flujos, 1e-7, 1000);
    }

    public double calcularTir(double[] flujos, double tol, int maxIter) {
        double r = 0.01;
        for (int iter = 0; iter < maxIter; iter++) {
            double f = 0.0;
            for (int t = 0; t < flujos.length; t++) {
                f += flujos[t] / Math.pow(1 + r, t);
            }
            double df = 0.0;
            for (int t = 1; t < flujos.length; t++) {
                df += -t * flujos[t] / Math.pow(1 + r, t + 1);
            }
            if (Math.abs(df) < 1e-15) {
                break;
            }
            double rNew = r - f / df;
            if (Math.abs(rNew - r) < tol) {
                return rNew;
            }
            r = rNew;
        }
        return r;
    }

    // ─────────────────────────────────────────────
    // MÉTODO FRANCÉS VENCIDO ORDINARIO (estándar BochoCredit)
    // ─────────────────────────────────────────────

    public ResultadoCronograma generarCronogramaFrances(
            double sf, double tem, int n, String graciaTipo, int graciaMeses,
            double tsd, double tsv, double vv, double portes,
            double gastosAdmin, double gps) {

        List<FilaCronograma> filas = new ArrayList<>();
        List<Double> flujos = new ArrayList<>();
        flujos.add(sf);

        int nActivo = n - graciaMeses;

        double sc = "total".equals(graciaTipo) ? sf * Math.pow(1 + tem, graciaMeses) : sf;
        double cuotaBase = nActivo > 0 ? (sc * tem) / (1 - Math.pow(1 + tem, -nActivo)) : 0.0;

        double saldo = sf;

        for (int k = 1; k <= n; k++) {
            double sIni = saldo;
            double interes = sIni * tem;
            double segDesgrav = sIni * tsd;
            double segVeh = vv * tsv;

            double amort, cuotaCapital, cuotaTotal;

            if ("total".equals(graciaTipo) && k <= graciaMeses) {
                amort = 0;
                cuotaCapital = 0;
                cuotaTotal = segDesgrav + segVeh + portes + gastosAdmin + gps;
                saldo = sIni * (1 + tem);
            } else if ("parcial".equals(graciaTipo) && k <= graciaMeses) {
                amort = 0;
                cuotaCapital = interes;
                cuotaTotal = interes + segDesgrav + segVeh + portes + gastosAdmin + gps;
                saldo = sIni;
            } else {
                amort = cuotaBase - interes;
                cuotaCapital = cuotaBase;
                cuotaTotal = cuotaBase + segDesgrav + segVeh + portes + gastosAdmin + gps;
                saldo = sIni - amort;
            }

            if (saldo < 0.01) saldo = 0;

            filas.add(FilaCronograma.builder()
                    .periodo(k)
                    .saldoInicial(r2(sIni))
                    .interes(r2(interes))
                    .amort(r2(amort))
                    .cuotaCapital(r2(cuotaCapital))
                    .segDesgrav(r2(segDesgrav))
                    .segVeh(r2(segVeh))
                    .portes(r2(portes))
                    .gastosAdmin(r2(gastosAdmin))
                    .gps(r2(gps))
                    .cuotaTotal(r2(cuotaTotal))
                    .saldoFinal(r2(saldo))
                    .build());

            flujos.add(-cuotaTotal);
        }

        return indicadores(flujos, tem, filas);
    }

    // ─────────────────────────────────────────────
    // MÉTODO ALEMÁN (amortización constante)
    // ─────────────────────────────────────────────

    public ResultadoCronograma generarCronogramaAleman(
            double sf, double tem, int n, String graciaTipo, int graciaMeses,
            double tsd, double tsv, double vv, double portes,
            double gastosAdmin, double gps) {

        List<FilaCronograma> filas = new ArrayList<>();
        List<Double> flujos = new ArrayList<>();
        flujos.add(sf);

        int nActivo = n - graciaMeses;
        double sc = "total".equals(graciaTipo) ? sf * Math.pow(1 + tem, graciaMeses) : sf;
        double amortConst = nActivo > 0 ? sc / nActivo : 0.0;

        double saldo = sf;

        for (int k = 1; k <= n; k++) {
            double sIni = saldo;
            double interes = sIni * tem;
            double segDesgrav = sIni * tsd;
            double segVeh = vv * tsv;

            double amort, cuotaCapital, cuotaTotal;

            if ("total".equals(graciaTipo) && k <= graciaMeses) {
                amort = 0;
                cuotaCapital = 0;
                cuotaTotal = segDesgrav + segVeh + portes + gastosAdmin + gps;
                saldo = sIni * (1 + tem);
            } else if ("parcial".equals(graciaTipo) && k <= graciaMeses) {
                amort = 0;
                cuotaCapital = interes;
                cuotaTotal = interes + segDesgrav + segVeh + portes + gastosAdmin + gps;
                saldo = sIni;
            } else {
                amort = amortConst;
                cuotaCapital = amort + interes;
                cuotaTotal = cuotaCapital + segDesgrav + segVeh + portes + gastosAdmin + gps;
                saldo = sIni - amort;
            }

            if (saldo < 0.01) saldo = 0;

            filas.add(FilaCronograma.builder()
                    .periodo(k)
                    .saldoInicial(r2(sIni))
                    .interes(r2(interes))
                    .amort(r2(amort))
                    .cuotaCapital(r2(cuotaCapital))
                    .segDesgrav(r2(segDesgrav))
                    .segVeh(r2(segVeh))
                    .portes(r2(portes))
                    .gastosAdmin(r2(gastosAdmin))
                    .gps(r2(gps))
                    .cuotaTotal(r2(cuotaTotal))
                    .saldoFinal(r2(saldo))
                    .build());

            flujos.add(-cuotaTotal);
        }

        return indicadores(flujos, tem, filas);
    }

    // ─────────────────────────────────────────────
    // MÉTODO AMERICANO (solo interés + bullet final)
    // ─────────────────────────────────────────────

    public ResultadoCronograma generarCronogramaAmericano(
            double sf, double tem, int n, String graciaTipo, int graciaMeses,
            double tsd, double tsv, double vv, double portes,
            double gastosAdmin, double gps) {

        List<FilaCronograma> filas = new ArrayList<>();
        List<Double> flujos = new ArrayList<>();
        flujos.add(sf);

        double saldo = sf;

        for (int k = 1; k <= n; k++) {
            double sIni = saldo;
            double interes = sIni * tem;
            double segDesgrav = sIni * tsd;
            double segVeh = vv * tsv;

            double amort, cuotaCapital, cuotaTotal;

            if (k < n) {
                if ("total".equals(graciaTipo) && k <= graciaMeses) {
                    amort = 0;
                    cuotaCapital = 0;
                    cuotaTotal = segDesgrav + segVeh + portes + gastosAdmin + gps;
                    saldo = sIni * (1 + tem);
                } else {
                    // parcial o regular: solo interés en todos los períodos intermedios
                    amort = 0;
                    cuotaCapital = interes;
                    cuotaTotal = interes + segDesgrav + segVeh + portes + gastosAdmin + gps;
                    saldo = sIni;
                }
            } else {
                // Último período: se paga el íntegro del capital
                amort = sIni;
                cuotaCapital = amort + interes;
                cuotaTotal = cuotaCapital + segDesgrav + segVeh + portes + gastosAdmin + gps;
                saldo = 0;
            }

            if (saldo < 0.01) saldo = 0;

            filas.add(FilaCronograma.builder()
                    .periodo(k)
                    .saldoInicial(r2(sIni))
                    .interes(r2(interes))
                    .amort(r2(amort))
                    .cuotaCapital(r2(cuotaCapital))
                    .segDesgrav(r2(segDesgrav))
                    .segVeh(r2(segVeh))
                    .portes(r2(portes))
                    .gastosAdmin(r2(gastosAdmin))
                    .gps(r2(gps))
                    .cuotaTotal(r2(cuotaTotal))
                    .saldoFinal(r2(saldo))
                    .build());

            flujos.add(-cuotaTotal);
        }

        return indicadores(flujos, tem, filas);
    }

    // ─────────────────────────────────────────────
    // MÉTODO PERUANO (francés con doble cuota julio/diciembre)
    // ─────────────────────────────────────────────

    public ResultadoCronograma generarCronogramaPeruano(
            double sf, double tem, int n, String graciaTipo, int graciaMeses,
            double tsd, double tsv, double vv, double portes,
            double gastosAdmin, double gps) {

        List<FilaCronograma> filas = new ArrayList<>();
        List<Double> flujos = new ArrayList<>();
        flujos.add(sf);

        LocalDateTime startDate = LocalDateTime.now();
        int[] nCuotasList = new int[n];
        for (int k = 1; k <= n; k++) {
            int m = (startDate.getMonthValue() - 1 + k) % 12 + 1;
            nCuotasList[k - 1] = (m == 7 || m == 12) ? 2 : 1;
        }

        double factorSum = 0.0;
        for (int k = graciaMeses + 1; k <= n; k++) {
            int ncK = nCuotasList[k - 1];
            factorSum += ncK / Math.pow(1 + tem, k - graciaMeses);
        }

        double sc = "total".equals(graciaTipo) ? sf * Math.pow(1 + tem, graciaMeses) : sf;
        double anualidad = factorSum > 0 ? sc / factorSum : 0.0;

        double saldo = sf;

        for (int k = 1; k <= n; k++) {
            double sIni = saldo;
            double interes = sIni * tem;
            double segDesgrav = sIni * tsd;
            double segVeh = vv * tsv;
            int ncK = nCuotasList[k - 1];

            double amort, cuotaCapital, cuotaTotal;

            if ("total".equals(graciaTipo) && k <= graciaMeses) {
                amort = 0;
                cuotaCapital = 0;
                cuotaTotal = segDesgrav + segVeh + portes + gastosAdmin + gps;
                saldo = sIni * (1 + tem);
            } else if ("parcial".equals(graciaTipo) && k <= graciaMeses) {
                amort = 0;
                cuotaCapital = interes;
                cuotaTotal = interes + segDesgrav + segVeh + portes + gastosAdmin + gps;
                saldo = sIni;
            } else {
                double cuotaBase = anualidad * ncK;
                amort = cuotaBase - interes;
                cuotaCapital = cuotaBase;
                cuotaTotal = cuotaBase + segDesgrav + segVeh + portes + gastosAdmin + gps;
                saldo = sIni - amort;
            }

            if (saldo < 0.01) saldo = 0;

            filas.add(FilaCronograma.builder()
                    .periodo(k)
                    .saldoInicial(r2(sIni))
                    .interes(r2(interes))
                    .amort(r2(amort))
                    .cuotaCapital(r2(cuotaCapital))
                    .segDesgrav(r2(segDesgrav))
                    .segVeh(r2(segVeh))
                    .portes(r2(portes))
                    .gastosAdmin(r2(gastosAdmin))
                    .gps(r2(gps))
                    .cuotaTotal(r2(cuotaTotal))
                    .saldoFinal(r2(saldo))
                    .build());

            flujos.add(-cuotaTotal);
        }

        return indicadores(flujos, tem, filas);
    }

    // ─────────────────────────────────────────────
    // COMPRA INTELIGENTE (francés + cuotón final + COK)
    // ─────────────────────────────────────────────

    /**
     * @param gastos     mapa con claves "portes", "gastos_admin", "gps"
     * @param pctCuotaFinalFraccion fracción (0–1) del precio del vehículo pactada como cuotón final
     * @param cokFraccion costo de oportunidad anual del cliente, en fracción (0–1)
     */
    public ResultadoCronograma generarCronogramaCompraInteligente(
            double sf, double tem, int n, String graciaTipo, int graciaMeses,
            double tsd, double tsv, double vv, Map<String, Double> gastos,
            double pctCuotaFinalFraccion, double cokFraccion) {

        List<FilaCronograma> filas = new ArrayList<>();
        List<Double> flujos = new ArrayList<>();
        flujos.add(sf);

        double portesG = gastos.getOrDefault("portes", 0.0);
        double gAdminG = gastos.getOrDefault("gastos_admin", 0.0);
        double gPeriodG = gastos.getOrDefault("gps", 0.0);

        // Cuotón nominal al final (porcentaje del precio del vehículo)
        double cuotonFinal = vv * pctCuotaFinalFraccion;

        // Valor presente del cuotón, capitalizando interés + desgravamen durante n+1 meses
        double factor = Math.pow(1 + tem + tsd, n + 1);
        double cuotonPv = cuotonFinal / factor;

        // Saldo regular (para cuotas mensuales)
        double saldoRegular = sf - cuotonPv;

        int nActivo = n - graciaMeses;
        double sc = "total".equals(graciaTipo) ? saldoRegular * Math.pow(1 + tem, graciaMeses) : saldoRegular;

        double cuotaBase = nActivo > 0 ? (sc * tem) / (1 - Math.pow(1 + tem, -nActivo)) : 0.0;

        double saldo = saldoRegular;
        double saldoCf = cuotonPv;

        for (int k = 1; k <= n; k++) {
            double sIni = saldo;
            double interes = sIni * tem;
            double segDesgrav = sIni * tsd;
            double segVeh = vv * tsv;

            double sIniCf = saldoCf;
            double interesCf = sIniCf * tem;
            double segDesgravCf = sIniCf * tsd;
            // El saldo final del cuotón acumula tanto interés como seguro de desgravamen
            saldoCf = sIniCf + interesCf + segDesgravCf;

            double amort, cuotaCapital, cuotaTotal;
            String pg;

            if ("total".equals(graciaTipo) && k <= graciaMeses) {
                amort = 0;
                cuotaCapital = 0;
                cuotaTotal = segDesgrav + segVeh + portesG + gAdminG + gPeriodG;
                saldo = sIni * (1 + tem);
                pg = "T";
            } else if ("parcial".equals(graciaTipo) && k <= graciaMeses) {
                amort = 0;
                cuotaCapital = interes;
                cuotaTotal = interes + segDesgrav + segVeh + portesG + gAdminG + gPeriodG;
                saldo = sIni;
                pg = "P";
            } else {
                amort = cuotaBase - interes;
                cuotaCapital = cuotaBase + segDesgrav;
                cuotaTotal = cuotaCapital + segVeh + portesG + gAdminG + gPeriodG;
                saldo = sIni - amort;
                pg = "S";
            }

            if (saldo < 0.01) saldo = 0;

            filas.add(FilaCronograma.builder()
                    .periodo(k)
                    .pg(pg)
                    .saldoInicialCf(r4(sIniCf))
                    .interesCf(r4(interesCf))
                    .amortCf(BigDecimal.ZERO)
                    .segDesgravCf(r4(segDesgravCf))
                    .saldoFinalCf(r4(saldoCf))
                    .saldoInicial(r4(sIni))
                    .interes(r4(interes))
                    .amort(r4(amort))
                    .cuotaCapital(r4(cuotaCapital))
                    .segDesgrav(r4(segDesgrav))
                    .segVeh(r4(segVeh))
                    .portes(r4(portesG))
                    .gastosAdmin(r4(gAdminG))
                    .gps(r4(gPeriodG))
                    .cuotaTotal(r4(cuotaTotal))
                    .saldoFinal(r4(saldo))
                    .build());

            flujos.add(-cuotaTotal);
        }

        // Cuotón final (período n+1)
        double segVehN1 = vv * tsv;
        double cuotaTotalN1 = saldoCf + segVehN1 + portesG + gAdminG + gPeriodG;

        filas.add(FilaCronograma.builder()
                .periodo(n + 1)
                .pg("S")
                .saldoInicialCf(r4(saldoCf))
                .interesCf(BigDecimal.ZERO)
                .amortCf(r4(saldoCf))
                .segDesgravCf(BigDecimal.ZERO)
                .saldoFinalCf(BigDecimal.ZERO)
                .saldoInicial(BigDecimal.ZERO)
                .interes(BigDecimal.ZERO)
                .amort(BigDecimal.ZERO)
                .cuotaCapital(BigDecimal.ZERO)
                .segDesgrav(BigDecimal.ZERO)
                .segVeh(r4(segVehN1))
                .portes(r4(portesG))
                .gastosAdmin(r4(gAdminG))
                .gps(r4(gPeriodG))
                .cuotaTotal(r4(cuotaTotalN1))
                .saldoFinal(BigDecimal.ZERO)
                .build());

        flujos.add(-cuotaTotalN1);

        // Conversión del COK anual a TEM
        double temCok = Math.pow(1 + cokFraccion, 1.0 / 12.0) - 1;

        // VAN con costo de oportunidad (descuenta con TEM del COK, no con la TEM del crédito)
        double van = 0.0;
        for (int t = 0; t < flujos.size(); t++) {
            van += flujos.get(t) / Math.pow(1 + temCok, t);
        }

        double[] flujosArr = flujos.stream().mapToDouble(Double::doubleValue).toArray();
        double tirMensual = calcularTir(flujosArr);
        double tcea = Math.pow(1 + tirMensual, 12) - 1;

        return new ResultadoCronograma(
                filas,
                BigDecimal.valueOf(van).setScale(4, RoundingMode.HALF_UP),
                BigDecimal.valueOf(tirMensual * 100).setScale(6, RoundingMode.HALF_UP),
                BigDecimal.valueOf(tcea * 100).setScale(4, RoundingMode.HALF_UP)
        );
    }

    // ─────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────

    private ResultadoCronograma indicadores(List<Double> flujos, double tem, List<FilaCronograma> filas) {
        double van = 0.0;
        for (int t = 0; t < flujos.size(); t++) {
            van += flujos.get(t) / Math.pow(1 + tem, t);
        }
        double[] flujosArr = flujos.stream().mapToDouble(Double::doubleValue).toArray();
        double tirMensual = calcularTir(flujosArr);
        double tcea = Math.pow(1 + tirMensual, 12) - 1;

        return new ResultadoCronograma(
                filas,
                BigDecimal.valueOf(van).setScale(2, RoundingMode.HALF_UP),
                BigDecimal.valueOf(tirMensual * 100).setScale(6, RoundingMode.HALF_UP),
                BigDecimal.valueOf(tcea * 100).setScale(4, RoundingMode.HALF_UP)
        );
    }

    private static BigDecimal r2(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal r4(double v) {
        return BigDecimal.valueOf(v).setScale(4, RoundingMode.HALF_UP);
    }
}
