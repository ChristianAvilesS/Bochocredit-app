package com.bochocredit.util;

import com.bochocredit.dto.simulacion.*;
import com.bochocredit.entity.*;
import com.bochocredit.util.classes.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Component
public class ClassMapper {
    public Pago toPago(EntradaCronograma entrada, LocalDateTime fechaPago, Simulacion sim) {
        var pago = new Pago();
        pago.setNumCuota(entrada.getPeriodo());
        pago.setFechaPago(fechaPago.plusMonths(entrada.getPeriodo()));
        pago.setTipoGracia(entrada.getPg());
        pago.setEstaPagado(false);
        
        pago.setSaldoInicialCf(bd4(entrada.getSaldoInicialCf()));
        pago.setInteresCf(bd4(entrada.getInteresCf()));
        pago.setAmortizacionCf(bd4(entrada.getAmortCf()));
        pago.setSeguroDesgravamenCf(bd4(entrada.getSegDesgravCf()));
        pago.setSaldoFinalCf(bd4(entrada.getSaldoFinalCf()));

        pago.setSaldoInicial(bd4(entrada.getSaldoInicial()));
        pago.setInteres(bd4(entrada.getInteres()));
        pago.setAmortizacion(bd4(entrada.getAmort()));
        pago.setCuota(bd4(entrada.getCuota()));
        pago.setSeguroDesgravamen(bd4(entrada.getSegDesgravCuota()));

        pago.setSeguroRiesgo(bd4(entrada.getSegRiesgo()));
        pago.setGastosAdmin(bd4(entrada.getGastosAdmin()));
        pago.setPortes(bd4(entrada.getPortes()));
        pago.setGps(bd4(entrada.getGps()));

        pago.setSaldoFinal(bd4(entrada.getSaldoFinal()));
        pago.setFlujo(bd4(entrada.getFlujo()));
        
        pago.setSimulacion(sim);
        
        return pago;
    }

    public PagoDTO toPagoDto(Pago pago) {
        var dto = new PagoDTO();
        dto.setNumCuota(pago.getNumCuota());
        dto.setFechaPago(pago.getFechaPago());
        dto.setTipoGracia(pago.getTipoGracia());
        dto.setEstaPagado(pago.getEstaPagado());

        dto.setSaldoInicial(pago.getSaldoInicial().add(pago.getSaldoInicialCf()));
        dto.setInteres(pago.getInteres().add(pago.getInteresCf()));
        dto.setAmortizacion(pago.getAmortizacion().add(pago.getAmortizacionCf()));
        dto.setSeguroDesgravamen(pago.getSeguroDesgravamenCf().add(pago.getSeguroDesgravamen()));
        dto.setCuota(pago.getCuota());

        dto.setSeguroRiesgo(pago.getSeguroRiesgo());
        dto.setOtrosGastos(pago.getGastosAdmin().add(pago.getPortes().add(pago.getGps())));

        dto.setSaldoFinal(pago.getSaldoFinal().add(pago.getSaldoFinalCf()));
        dto.setFlujo(pago.getFlujo());

        return dto;
    }

    public DetalleSimulacionDTO toDetalle(Simulacion s) {
        if (s == null) return null;

        Cliente c = s.getCliente();
        Vehiculo v = s.getVehiculo();

        DetalleSimulacionDTO detalle = new DetalleSimulacionDTO();

        detalle.setId(s.getId());
        detalle.setTcea(s.getTcea());
        detalle.setVan(s.getVan());
        detalle.setTir(s.getTir());

        detalle.setValorVenta(s.getPrecioVehiculo());
        detalle.setPrecioVehiculo(s.getPrecioVehiculo());
        detalle.setSaldoFinanciado(s.getSaldoFinanciado());
        detalle.setPrestamo(s.getPrestamo());
        detalle.setPlazoMeses(s.getPlazoMeses());
        detalle.setPorcCuotaInicial(s.getPorcCuotaInicial());
        detalle.setPctCuotaFinal(s.getPorcCuotaFinal());
        detalle.setGraciaTipo(s.getTipoPeriodoGracia());
        detalle.setGraciaMeses(
                s.getPeriodoGraciaMeses() != null ? s.getPeriodoGraciaMeses() : null
        );
        detalle.setMoneda(s.getTipoMoneda());
        detalle.setEsElegido(s.getEsElegido());
        detalle.setCreadoEn(s.getCreadoEn());

        detalle.setTipoTasa(s.getTipoTasa());
        detalle.setTasaValor(s.getTasaValor());
        detalle.setCapitalizacion(s.getCapitalizacion());
        detalle.setTem(s.getTem());
        detalle.setTsd(s.getTsd());
        detalle.setTsv(s.getTsv());
        detalle.setPortes(s.getPortes());
        detalle.setGastosAdmin(s.getGastosAdmin());
        detalle.setGps(s.getGps());
        detalle.setCok(s.getCok());
        detalle.setNotariales(s.getGastosNotar());
        detalle.setRegistrales(s.getGastosRegist());

        if (c != null) {
            detalle.setClienteId(c.getId());
            detalle.setClienteNombre(c.getNombres() + " " + c.getApellidos());
            detalle.setClienteDni(c.getDni());
            detalle.setClienteEmail(c.getEmail());
            detalle.setClienteTelefono(c.getTelefono());
        }

        if (v != null) {
            detalle.setVehiculoId(v.getId());
            detalle.setVehiculoMarca(v.getMarca());
            detalle.setVehiculoModelo(v.getModelo());
            detalle.setVehiculoAnio(v.getAnio());
            detalle.setPrecioVehiculo(s.getPrecioVehiculo());
        }

        return detalle;

    }

    public SimulacionListItemDTO toListItem(Simulacion s) {
        return new SimulacionListItemDTO(
                s.getId(),
                s.getCliente().getNombreCompleto(),
                s.getVehiculo().getNombreCompleto(),
                s.getTipoMoneda(),
                s.getPrestamo(),
                s.getPlazoMeses(),
                s.getTcea(),
                s.getVan(),
                s.getTir(),
                s.getEsElegido(),
                s.getCreadoEn()
        );
    }

    public SimulacionCronogramaDTO toSimCrono(RequestSimulacion r,
                                              ParametrosCalculo p,
                                              ResultadoCronograma cronograma) {
        SimulacionCronogramaDTO dto = new SimulacionCronogramaDTO();

        // Indicadores financieros
        dto.setTcea(BigDecimal.valueOf(cronograma.getTceaPct() * 100));
        dto.setVan(BigDecimal.valueOf(cronograma.getVan()));
        dto.setTir(BigDecimal.valueOf(cronograma.getTirMensualPct() * 100));

        // Condiciones del crédito
        dto.setSaldoFinanciado(BigDecimal.valueOf(p.getSaldoFinanciado()));
        dto.setPrestamo(BigDecimal.valueOf(p.getValorPrestamo()));
        dto.setPlazoMeses(r.getPlazoMeses());
        dto.setPorcCuotaInicial(BigDecimal.valueOf(r.getCuotaInicialPct()));
        dto.setPctCuotaFinal(BigDecimal.valueOf(r.getCuotaFinalPct()));
        dto.setGraciaTipo(r.getTipoGracia());
        dto.setGraciaMeses(r.getPeriodoGraciaMeses());
        dto.setMoneda(r.getMoneda());

        // Parámetros de tasa
        dto.setTem(BigDecimal.valueOf(p.getTem() * 100));
        dto.setCok(BigDecimal.valueOf(r.getCok()));

        // Cronograma de cuotas
        dto.setCuotas(cronograma.getFilas().stream().map(this::entradaCronoToPagoDTO)
                .collect(Collectors.toList()));

        return dto;
    }

    public PagoDTO entradaCronoToPagoDTO(EntradaCronograma entrada) {
        var dto = new PagoDTO();
        dto.setNumCuota(entrada.getPeriodo());
        dto.setFechaPago(null);
        dto.setTipoGracia(entrada.getPg());
        dto.setEstaPagado(false);

        dto.setSaldoInicial(bd4(entrada.getSaldoInicial() + entrada.getSaldoInicialCf()));
        dto.setInteres(bd4(entrada.getInteres() + entrada.getInteresCf()));
        dto.setAmortizacion(bd4(entrada.getAmort()+ entrada.getAmortCf()));
        dto.setSeguroDesgravamen(bd4(entrada.getSegDesgravCuota() + entrada.getSegDesgravCf()));

        dto.setSeguroRiesgo(bd4(entrada.getSegRiesgo()));
        dto.setOtrosGastos(bd4(entrada.getGastosAdmin() + entrada.getPortes() + entrada.getGps()));

        dto.setSaldoFinal(bd4(entrada.getSaldoFinal() + entrada.getSaldoFinalCf()));
        dto.setFlujo(bd4(entrada.getFlujo()));

        return dto;
    }



    private BigDecimal bd4(double v) {
        return BigDecimal.valueOf(v).setScale(4, RoundingMode.HALF_UP);
    }
}
