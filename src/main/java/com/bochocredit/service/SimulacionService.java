package com.bochocredit.service;

import com.bochocredit.dto.simulacion.SimulacionDtos.CalculoPreview;
import com.bochocredit.dto.simulacion.SimulacionDtos.SimulacionDetalle;
import com.bochocredit.dto.simulacion.SimulacionDtos.SimulacionListItem;
import com.bochocredit.dto.simulacion.SimulacionRequest;
import com.bochocredit.entity.*;
import com.bochocredit.exception.ResourceNotFoundException;
import com.bochocredit.exception.VehiculoBloqueadoException;
import com.bochocredit.repository.*;
import com.bochocredit.service.VehiculoBloqueoService.EstadoBloqueo;
import com.bochocredit.service.finance.FilaCronograma;
import com.bochocredit.service.finance.FinancialEngine;
import com.bochocredit.service.finance.ResultadoCronograma;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class SimulacionService {

    private final SimulacionRepository simulacionRepository;
    private final PagoRepository pagoRepository;
    private final ClienteRepository clienteRepository;
    private final VehiculoRepository vehiculoRepository;
    private final BancoRepository bancoRepository;
    private final TasaInteresRepository tasaInteresRepository;
    private final UsuarioRepository usuarioRepository;

    private final FinancialEngine engine;
    private final VehiculoBloqueoService bloqueoService;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // ─────────────────────────────────────────────
    // CÁLCULO (sin persistir) — equivalente a /api/calcular
    // ─────────────────────────────────────────────

    public CalculoPreview calcular(SimulacionRequest req) {
        double vv = req.precioVehiculo().doubleValue();
        double ciPct = req.cuotaInicialPct().doubleValue();
        double ciMonto = vv * ciPct / 100.0;
        double sf = vv - ciMonto;

        double tem = engine.calcularTem(req.tipoTasa(), req.tasaValor().doubleValue(), req.capitalizacionOrDefault());
        int plazo = req.plazoMeses();
        String graciaTipo = req.graciaTipo();
        int graciaMeses = req.graciaMeses();
        double tsd = req.tsd().doubleValue() / 100.0;
        double tsv = req.tsv().doubleValue() / 100.0;
        double portes = req.portes().doubleValue();
        double gastosAdmin = req.gastosAdminOrZero().doubleValue();
        double gps = req.gpsOrZero().doubleValue();

        ResultadoCronograma resultado = ejecutarMotor(
                req.metodoPagoOrDefault(), sf, tem, plazo, graciaTipo, graciaMeses,
                tsd, tsv, vv, portes, gastosAdmin, gps,
                req.pctCuotaFinalOrDefault().doubleValue() / 100.0,
                req.cokOrDefault().doubleValue() / 100.0
        );

        double tea = Math.pow(1 + tem, 12) - 1;

        BigDecimal cuotaRegular = BigDecimal.ZERO;
        List<FilaCronograma> filas = resultado.getFilas();
        if (!filas.isEmpty()) {
            FilaCronograma primeraNormal = filas.stream()
                    .filter(f -> ("S".equals(f.getPg()) || f.getPg() == null) && f.getPeriodo() <= plazo)
                    .findFirst()
                    .orElse(filas.get(0));
            cuotaRegular = primeraNormal.getCuotaCapital() != null
                    ? primeraNormal.getCuotaCapital()
                    : primeraNormal.getCuotaTotal();
        }

        return new CalculoPreview(
                bd2(sf), bd2(ciMonto),
                BigDecimal.valueOf(tem * 100).setScale(6, RoundingMode.HALF_UP),
                BigDecimal.valueOf(tea * 100).setScale(4, RoundingMode.HALF_UP),
                resultado.getVan(), resultado.getTirMensualPct(), resultado.getTceaPct(),
                cuotaRegular.setScale(2, RoundingMode.HALF_UP),
                filasToMapList(filas)
        );
    }

    // ─────────────────────────────────────────────
    // CREAR — equivalente a /creditos/nuevo (POST)
    // ─────────────────────────────────────────────

    public SimulacionDetalle crear(SimulacionRequest req) {
        EstadoBloqueo estado = bloqueoService.esVehiculoBloqueado(req.vehiculoId());
        if (estado.bloqueado()) {
            throw new VehiculoBloqueadoException(
                    "El vehículo seleccionado está bloqueado por un plan de pagos activo hasta el "
                            + estado.bloqueadoHasta() + ".");
        }

        Cliente cliente = clienteRepository.findById(req.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + req.clienteId()));
        Vehiculo vehiculo = vehiculoRepository.findById(req.vehiculoId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado: " + req.vehiculoId()));
        Banco banco = bancoRepository.findById(1L)
                .orElseThrow(() -> new ResourceNotFoundException("Banco por defecto no configurado."));
        TasaInteres tasaDefault = tasaInteresRepository.findById(1L)
                .orElseThrow(() -> new ResourceNotFoundException("Tasa por defecto no configurada."));
        Usuario usuario = usuarioActual();

        ParametrosCalculo p = calcularParametros(req);

        ResultadoCronograma resultado = ejecutarMotor(
                req.metodoPagoOrDefault(), p.sf, p.tem, req.plazoMeses(), req.graciaTipo(), req.graciaMeses(),
                p.tsd, p.tsv, p.vv, p.portes, p.gastosAdmin, p.gps,
                req.pctCuotaFinalOrDefault().doubleValue() / 100.0,
                req.cokOrDefault().doubleValue() / 100.0
        );

        Simulacion sim = Simulacion.builder()
                .tcea(resultado.getTceaPct())
                .van(resultado.getVan())
                .tir(resultado.getTirMensualPct())
                .saldoFinanciado(bd2(p.sf))
                .plazoMeses(req.plazoMeses())
                .cantidadCuotas(req.plazoMeses())
                .porcCuotaInicial(req.cuotaInicialPct())
                .tipoPeriodoGracia(req.graciaTipo())
                .periodoGraciaMeses(BigDecimal.valueOf(req.graciaMeses()))
                .tipoMoneda(req.moneda())
                .esElegido(false)
                .usuario(usuario)
                .cliente(cliente)
                .vehiculo(vehiculo)
                .banco(banco)
                .tasaInteres(tasaDefault)
                .moneda(req.moneda())
                .precioVehiculo(req.precioVehiculo())
                .cuotaInicialPct(req.cuotaInicialPct())
                .cuotaInicialMonto(bd2(p.ciMonto))
                .tipoTasa(req.tipoTasa())
                .tasaValor(req.tasaValor())
                .capitalizacion(req.capitalizacionOrDefault())
                .tem(BigDecimal.valueOf(p.tem))
                .graciaTipo(req.graciaTipo())
                .graciaMeses(req.graciaMeses())
                .tsd(BigDecimal.valueOf(p.tsd * 100).setScale(4, RoundingMode.HALF_UP))
                .tsv(BigDecimal.valueOf(p.tsv * 100).setScale(4, RoundingMode.HALF_UP))
                .portes(req.portes())
                .gastosAdmin(req.gastosAdminOrZero())
                .gps(req.gpsOrZero())
                .metodoPago(req.metodoPagoOrDefault())
                .pctCuotaFinal(req.pctCuotaFinalOrDefault().divide(BigDecimal.valueOf(100)))
                .cok(req.cokOrDefault().divide(BigDecimal.valueOf(100)))
                .cronograma(filasToMapList(resultado.getFilas()))
                .build();

        simulacionRepository.save(sim);
        guardarPagos(sim, resultado.getFilas(), req.tipoTasa(), req.tasaValor(), req.graciaTipo());

        return toDetalle(sim);
    }

    // ─────────────────────────────────────────────
    // EDITAR / RECALCULAR — equivalente a /creditos/<id>/editar (POST)
    // ─────────────────────────────────────────────

    public SimulacionDetalle actualizar(Long id, SimulacionRequest req) {
        Simulacion sim = simulacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Simulación no encontrada: " + id));

        // Solo valida bloqueo si el vehículo cambió
        if (!sim.getVehiculo().getId().equals(req.vehiculoId())) {
            EstadoBloqueo estado = bloqueoService.esVehiculoBloqueado(req.vehiculoId());
            if (estado.bloqueado()) {
                throw new VehiculoBloqueadoException(
                        "El vehículo seleccionado está bloqueado por un plan de pagos activo hasta el "
                                + estado.bloqueadoHasta() + ".");
            }
        }

        Cliente cliente = clienteRepository.findById(req.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + req.clienteId()));
        Vehiculo vehiculo = vehiculoRepository.findById(req.vehiculoId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado: " + req.vehiculoId()));

        ParametrosCalculo p = calcularParametros(req);

        ResultadoCronograma resultado = ejecutarMotor(
                req.metodoPagoOrDefault(), p.sf, p.tem, req.plazoMeses(), req.graciaTipo(), req.graciaMeses(),
                p.tsd, p.tsv, p.vv, p.portes, p.gastosAdmin, p.gps,
                req.pctCuotaFinalOrDefault().doubleValue() / 100.0,
                req.cokOrDefault().doubleValue() / 100.0
        );

        sim.setCliente(cliente);
        sim.setVehiculo(vehiculo);
        sim.setTcea(resultado.getTceaPct());
        sim.setVan(resultado.getVan());
        sim.setTir(resultado.getTirMensualPct());
        sim.setSaldoFinanciado(bd2(p.sf));
        sim.setPlazoMeses(req.plazoMeses());
        sim.setCantidadCuotas(req.plazoMeses());
        sim.setPorcCuotaInicial(req.cuotaInicialPct());
        sim.setTipoPeriodoGracia(req.graciaTipo());
        sim.setPeriodoGraciaMeses(BigDecimal.valueOf(req.graciaMeses()));
        sim.setTipoMoneda(req.moneda());
        sim.setMoneda(req.moneda());
        sim.setPrecioVehiculo(req.precioVehiculo());
        sim.setCuotaInicialPct(req.cuotaInicialPct());
        sim.setCuotaInicialMonto(bd2(p.ciMonto));
        sim.setTipoTasa(req.tipoTasa());
        sim.setTasaValor(req.tasaValor());
        sim.setCapitalizacion(req.capitalizacionOrDefault());
        sim.setTem(BigDecimal.valueOf(p.tem));
        sim.setGraciaTipo(req.graciaTipo());
        sim.setGraciaMeses(req.graciaMeses());
        sim.setTsd(BigDecimal.valueOf(p.tsd * 100).setScale(4, RoundingMode.HALF_UP));
        sim.setTsv(BigDecimal.valueOf(p.tsv * 100).setScale(4, RoundingMode.HALF_UP));
        sim.setPortes(req.portes());
        sim.setGastosAdmin(req.gastosAdminOrZero());
        sim.setGps(req.gpsOrZero());
        sim.setMetodoPago(req.metodoPagoOrDefault());
        sim.setPctCuotaFinal(req.pctCuotaFinalOrDefault().divide(BigDecimal.valueOf(100)));
        sim.setCok(req.cokOrDefault().divide(BigDecimal.valueOf(100)));
        sim.setCronograma(filasToMapList(resultado.getFilas()));

        pagoRepository.deleteBySimulacionId(id);
        guardarPagos(sim, resultado.getFilas(), req.tipoTasa(), req.tasaValor(), req.graciaTipo());

        return toDetalle(sim);
    }

    // ─────────────────────────────────────────────
    // ELEGIR PLAN — equivalente a /creditos/<id>/elegir
    // ─────────────────────────────────────────────

    public void elegir(Long id) {
        Simulacion sim = simulacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Simulación no encontrada: " + id));

        Long vehiculoId = sim.getVehiculo().getId();

        // Desmarcar cualquier otra simulación elegida para este mismo vehículo
        List<Simulacion> todasDelVehiculo = simulacionRepository.findAllByVehiculoId(vehiculoId);
        todasDelVehiculo.forEach(s -> s.setEsElegido(false));

        sim.setEsElegido(true);
    }

    // ─────────────────────────────────────────────
    // LECTURA
    // ─────────────────────────────────────────────

    public List<SimulacionListItem> listar() {
        return simulacionRepository.findAllByOrderByCreadoEnDesc().stream()
                .map(this::toListItem)
                .toList();
    }

    public List<SimulacionListItem> listarPorCliente(Long clienteId) {
        return simulacionRepository.findByClienteIdOrderByCreadoEnDesc(clienteId).stream()
                .map(this::toListItem)
                .toList();
    }

    public SimulacionDetalle obtener(Long id) {
        Simulacion sim = simulacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Simulación no encontrada: " + id));
        return toDetalle(sim);
    }

    // ─────────────────────────────────────────────
    // Helpers internos
    // ─────────────────────────────────────────────

    private record ParametrosCalculo(double vv, double ciMonto, double sf, double tem,
                                      double tsd, double tsv, double portes,
                                      double gastosAdmin, double gps) {}

    private ParametrosCalculo calcularParametros(SimulacionRequest req) {
        double vv = req.precioVehiculo().doubleValue();
        double ciPct = req.cuotaInicialPct().doubleValue();
        double ciMonto = vv * ciPct / 100.0;
        double sf = vv - ciMonto;
        double tem = engine.calcularTem(req.tipoTasa(), req.tasaValor().doubleValue(), req.capitalizacionOrDefault());
        double tsd = req.tsd().doubleValue() / 100.0;
        double tsv = req.tsv().doubleValue() / 100.0;
        double portes = req.portes().doubleValue();
        double gastosAdmin = req.gastosAdminOrZero().doubleValue();
        double gps = req.gpsOrZero().doubleValue();
        return new ParametrosCalculo(vv, ciMonto, sf, tem, tsd, tsv, portes, gastosAdmin, gps);
    }

    private ResultadoCronograma ejecutarMotor(
            String metodoPago, double sf, double tem, int plazo, String graciaTipo, int graciaMeses,
            double tsd, double tsv, double vv, double portes, double gastosAdmin, double gps,
            double pctCuotaFinalFraccion, double cokFraccion) {

        return switch (metodoPago) {
            case "compra_inteligente" -> {
                Map<String, Double> gastos = new HashMap<>();
                gastos.put("portes", portes);
                gastos.put("gastos_admin", gastosAdmin);
                gastos.put("gps", gps);
                yield engine.generarCronogramaCompraInteligente(
                        sf, tem, plazo, graciaTipo, graciaMeses, tsd, tsv, vv, gastos,
                        pctCuotaFinalFraccion, cokFraccion);
            }
            case "aleman" -> engine.generarCronogramaAleman(
                    sf, tem, plazo, graciaTipo, graciaMeses, tsd, tsv, vv, portes, gastosAdmin, gps);
            case "americano" -> engine.generarCronogramaAmericano(
                    sf, tem, plazo, graciaTipo, graciaMeses, tsd, tsv, vv, portes, gastosAdmin, gps);
            case "peruano" -> engine.generarCronogramaPeruano(
                    sf, tem, plazo, graciaTipo, graciaMeses, tsd, tsv, vv, portes, gastosAdmin, gps);
            default -> engine.generarCronogramaFrances(
                    sf, tem, plazo, graciaTipo, graciaMeses, tsd, tsv, vv, portes, gastosAdmin, gps);
        };
    }

    private void guardarPagos(Simulacion sim, List<FilaCronograma> filas, String tipoTasa,
                               BigDecimal tasaValor, String graciaTipo) {
        for (FilaCronograma f : filas) {
            Pago pago = Pago.builder()
                    .numCuota(f.getPeriodo())
                    .tipoTasa(tipoTasa)
                    .tasaInteres(tasaValor)
                    .diasCapitalizacion(30)
                    .diasTasa(360)
                    .tipoGracia(graciaTipo)
                    .estaPagado(false)
                    .saldoInicialCf(orZero(f.getSaldoInicialCf()))
                    .interesCf(orZero(f.getInteresCf()))
                    .amortizacionCf(orZero(f.getAmortCf()))
                    .seguroDesgravamenCf(orZero(f.getSegDesgravCf()))
                    .saldoFinalCf(orZero(f.getSaldoFinalCf()))
                    .saldoInicial(f.getSaldoInicial())
                    .interes(f.getInteres())
                    .amortizacion(f.getAmort())
                    .seguroDesgravamen(f.getSegDesgrav())
                    .seguroRiesgo(f.getSegVeh())
                    .portes(f.getPortes())
                    .gastosAdmin(orZero(f.getGastosAdmin()))
                    .gps(orZero(f.getGps()))
                    .saldoFinal(f.getSaldoFinal())
                    .flujo(f.getCuotaTotal().negate())
                    .simulacion(sim)
                    .build();
            pagoRepository.save(pago);
        }
    }

    private BigDecimal orZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> filasToMapList(List<FilaCronograma> filas) {
        return filas.stream()
                .map(f -> (Map<String, Object>) MAPPER.convertValue(f, Map.class))
                .toList();
    }

    private static BigDecimal bd2(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP);
    }

    private Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado: " + username));
    }

    private SimulacionListItem toListItem(Simulacion s) {
        return new SimulacionListItem(
                s.getId(),
                s.getCliente().getNombreCompleto(),
                s.getVehiculo().getNombreCompleto(),
                s.getMoneda(),
                s.getSaldoFinanciado(),
                s.getPlazoMeses(),
                s.getTcea(),
                s.getVan(),
                s.getTir(),
                s.getEsElegido(),
                s.getCreadoEn()
        );
    }

    private SimulacionDetalle toDetalle(Simulacion s) {
        Cliente c = s.getCliente();
        Vehiculo v = s.getVehiculo();
        return new SimulacionDetalle(
                s.getId(), s.getTcea(), s.getVan(), s.getTir(), s.getSaldoFinanciado(),
                s.getPlazoMeses(), s.getPorcCuotaInicial(), s.getGraciaTipo(), s.getGraciaMeses(),
                s.getMoneda(), s.getEsElegido(), s.getCreadoEn(),
                s.getTipoTasa(), s.getTasaValor(), s.getCapitalizacion(), s.getTem(),
                s.getTsd(), s.getTsv(), s.getPortes(), s.getGastosAdmin(), s.getGps(),
                s.getMetodoPago(), s.getPctCuotaFinal(), s.getCok(),
                c.getId(), c.getNombreCompleto(), c.getDni(), c.getEmail(), c.getTelefono(),
                v.getId(), v.getMarca(), v.getModelo(), v.getAnio(), s.getPrecioVehiculo(),
                s.getCronograma()
        );
    }
}
