package com.bochocredit.util;

import com.bochocredit.util.classes.*;
import com.bochocredit.util.classes.ResultadoCronograma;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CalculadoraPagos {

    public ParametrosCalculo calcular(RequestSimulacion req) {
        double cuotaInicial = req.getValorVenta() * req.getCuotaInicialPct() / 100.0;
        double cuotonFinal = req.getValorVenta() * req.getCuotaFinalPct() / 100.0;

        String[] tipoTasa = req.getTipoTasa().split("_");

        var gastosI = req.getGastosIniciales();
        double gastosIniciales = sumarGastos(gastosI);

        var gastosP = req.getGastosPeriodicos();

        double prestamo = req.getValorVenta() - cuotaInicial + gastosIniciales;

        double frecPago = 30.0;
        double diasAnio = 360.0;
        double nCuotas = req.getPlazoMeses();
        double cuotasAlAnio = diasAnio / frecPago;

        double m = parsearDiasTipoTasa(tipoTasa[1]) / req.getCapitalizacion();
        double n = tipoTasa[0].equals("nominal") ?
                frecPago / req.getCapitalizacion() :
                frecPago / parsearDiasTipoTasa(tipoTasa[1]);

        double tem = calcularTEM(tipoTasa[0], req.getTasaInteres() / 100.0, m, n);
        if (tem < 0 || tem > 1) {
            throw new IllegalArgumentException("TEM out of range: " + tem);
        }


        double segDesg = req.getSeguroDesgPct() / 100.0;
        double seguroDesgravamenPeriodo = segDesg * frecPago / 30;

        double seguroVehicular = (req.getSeguroVehicularPct() / 100.0 ) * req.getValorVenta() / cuotasAlAnio;

        double saldoFinanciado = prestamo - (cuotonFinal / Math.pow(1 + tem + segDesg, nCuotas + 1));
        if (saldoFinanciado < 0) {
            throw new IllegalArgumentException("Saldo financiado negative: " + saldoFinanciado);
        }



        return new ParametrosCalculo(
                req.getValorVenta(), (int)nCuotas, prestamo, cuotaInicial, cuotonFinal, saldoFinanciado, tem,
                seguroDesgravamenPeriodo, seguroVehicular, gastosP.getOrDefault("Portes", 0.0),
                gastosP.getOrDefault("Admin", 0.0), gastosP.getOrDefault("Gps", 0.0),
                String.valueOf(req.getTipoGracia().charAt(0)).toUpperCase(), req.getPeriodoGraciaMeses()
        );
    }

    public double sumarGastos(Map<String, Double> gastosIniciales) {
        double sum = 0;
        for (var gasto : gastosIniciales.values()) {
            sum += gasto;
        }

        return sum;
    }

    private double parsearDiasTipoTasa(String tipoTasa) {
        return switch (tipoTasa) {
            case "mensual" -> 30;
            case "anual" -> 360;
            default -> 1;
        };
    }

    private double calcularTEM(String tipoTasa, double valorTasa, double m, double n) {
        return switch (tipoTasa) {
            case "efectiva" -> Math.pow(1 + valorTasa, n) - 1;
            case "nominal" -> Math.pow(1 + valorTasa / m, n) - 1;
            default -> 0.1;
        };
    }

    public ResultadoCronograma crearCronograma(ParametrosCalculo p, double cok) {
        ResultadoCronograma crono = new ResultadoCronograma();

        var cuotas = crearCuotas(p);

        crono.setFilas(cuotas);

        crono.setVan(calcularVan(p, cuotas, cok));

        var tir = calcularTir(cuotas);


        crono.setTirMensualPct(tir);

        var tcea = calcularTcea(tir);


        crono.setTceaPct(tcea);

        //exportData(crono, p, cok);

        if (!Double.isFinite(tir) || tir < 0 || tir > 1) {
            throw new IllegalArgumentException("TIR unrealistic: " + tir);
        }

        if (!Double.isFinite(tcea) || tcea < 0 || tcea > 5) { // cap at 500%
            throw new IllegalArgumentException("TCEA unrealistic: " + tcea);
        }

        return crono;
    }

    private List<EntradaCronograma> crearCuotas(ParametrosCalculo p) {
        List<EntradaCronograma> pagos = new ArrayList<>();
        EntradaCronograma entradaAnterior = crearEntrada(p, null, 0, "");
        pagos.add(entradaAnterior);
        int nCuotas = p.getCantPagos();

        for (int i = 0; i < nCuotas + 1; i++) {
            int numCuota = i + 1;
            var pg = numCuota <= p.getMesesGracia() ? p.getTipoGracia() : "S";
            
             var actual = crearEntrada(p, entradaAnterior, numCuota, pg);

            pagos.add(actual);
            entradaAnterior = actual;
        }

        return pagos;
    }
    
    private EntradaCronograma crearEntrada(ParametrosCalculo p, EntradaCronograma anterior, int n, String pg) {
        int cantCuotas = p.getCantPagos();

        if (n == 0) {
            return new EntradaCronograma(
                    n, "", 0, 0,0,0,0,0,
                    0,0,0,0,0,0,0,0,
                    0, p.getValorPrestamo()
            );
        }

        //--------------------------------------------------//
        //              Cuota Final (Cuotón)                //
        //--------------------------------------------------//


        double saldoInicialCf = n == 1 ?
                p.getCuotaFinal() / Math.pow(1 + p.getTem() + p.getSeguroDesgravamenPeriodo(), cantCuotas + 1) :
                anterior.getSaldoFinalCf();

        double interesCf = - saldoInicialCf * p.getTem();

        double segDesgravCf = - saldoInicialCf * p.getSeguroDesgravamenPeriodo();

        double amortCf = n == cantCuotas + 1 ? - saldoInicialCf + interesCf + segDesgravCf : 0;

        double saldoFinalCf = saldoInicialCf - interesCf - segDesgravCf + amortCf;

        //--------------------------------------------------//
        //              Cuota Regular                       //
        //--------------------------------------------------//

        double saldoInicial = n == 1 ?
                p.getSaldoFinanciado() : n <= cantCuotas ?
                anterior.getSaldoFinal() : 0;

        double interes = - saldoInicial * p.getTem();

        double segDesgravCuota = - saldoInicial * p.getSeguroDesgravamenPeriodo();

        double cuota, amort;

        if (n <= cantCuotas) {
            switch (pg) {
                case "T": {
                    cuota = 0;
                    amort = 0;
                    break;
                }
                case "P": {
                    cuota = interes;
                    amort = 0;
                    break;
                }
                default: { // valor S
                    cuota = - pago(
                            p.getTem() + p.getSeguroDesgravamenPeriodo(),
                            cantCuotas + 1 - n, saldoInicial
                            );
                    amort = cuota - interes - segDesgravCuota;
                }
            }
        }
        else {
            cuota = 0;
            amort = 0;
        }

        //--------------------------------------------------//
        //              Costes Operación                    //
        //--------------------------------------------------//

        double segRiesgo = - p.getSeguroVehicular();
        double portes = - p.getPortes();
        double gastosAdmin = - p.getGastosAdmin();
        double gps = - p.getGps();

        //--------------------------------------------------//
        //                      Final                       //
        //--------------------------------------------------//

        double saldoFinal = pg.equals("T") ? saldoInicial - interes : saldoInicial + amort;
        double flujo = cuota + segRiesgo + gps + portes + gastosAdmin;

        if (pg.equals("P") || pg.equals("T")) flujo += segDesgravCuota;
        if (n == cantCuotas + 1) flujo += amortCf;

        return new EntradaCronograma(
                n, pg, saldoInicialCf, interesCf, amortCf, segDesgravCf, saldoFinalCf, saldoInicial, interes,
                cuota, amort, segDesgravCuota, segRiesgo, portes, gastosAdmin, gps, saldoFinal, flujo
        );
    }

    private double pago(double interes, double n, double va) {
        return va * interes / (1 - Math.pow(1 + interes, -n) );
    }


    private double calcularVan(ParametrosCalculo p, List<EntradaCronograma> cuotas, double cok) {
        double COKi = Math.pow(1 + cok, 1.0 / 12.0) - 1;

        double vna = 0;

        for (EntradaCronograma entrada : cuotas) {
            vna += entrada.getFlujo() / Math.pow(1 + COKi, entrada.getPeriodo());
        }

        return vna;
    }

    private double calcularTir(List<EntradaCronograma> cuotas) {
        double[] flujos = new double[cuotas.size()];
        for (var c : cuotas) {
            flujos[c.getPeriodo()] = c.getFlujo();
        }
        return calcularTir(flujos);
    }

    private double calcularTir(double[] flujos) {
        double r = 0.01;
        for (int iter = 0; iter < 1000; iter++) {
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
            if (Math.abs(rNew - r) < 1.0E-7) {
                return rNew;
            }
            r = rNew;
        }
        return r;
    }

    private double calcularTcea(double tir) {
        return Math.pow(1.0 + tir, 12.0) - 1.0;
    }

    private double round(double i) {
        return BigDecimal.valueOf(i)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private void exportData(List<EntradaCronograma> l) {
        try {
            CsvExporter.exportListToCsv(l, "C:/exports/cronograma.csv");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void exportData(ResultadoCronograma c, ParametrosCalculo p, double cok) {
        try {
            Map<String,String> resumen = new HashMap<>(Map.of(
                    "Valor Venta", String.valueOf(p.getValorVenta()),
                    "Valor Prestamo", String.valueOf(p.getValorPrestamo()),
                    "Cuota Inicial", String.valueOf(p.getCuotaInicial()),
                    "Cuota Final", String.valueOf(p.getCuotaFinal()),
                    "Saldo Financiado", String.valueOf(p.getSaldoFinanciado()),

                    "TEM", String.valueOf(p.getTem()),
                    "Seguro Desgravamen", String.valueOf(p.getSeguroDesgravamenPeriodo()),
                    "Seguro Vehicular", String.valueOf(p.getSeguroVehicular())
            ));

            resumen.put("COK", String.valueOf(cok));
            resumen.put("VAN", String.valueOf(c.getVan()));

            resumen.put("TCEA", String.valueOf(c.getTceaPct()));
            resumen.put("TIR Mensual", String.valueOf(c.getTirMensualPct()));

            CsvExporter.exportMapToCsv(resumen, "C:/exports/resumen.csv");

            exportData(c.getFilas());
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
