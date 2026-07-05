package com.bochocredit.service;

import com.bochocredit.dto.simulacion.*;
import com.bochocredit.entity.*;
import com.bochocredit.exception.*;
import com.bochocredit.repository.*;
import com.bochocredit.service.VehiculoBloqueoService.EstadoBloqueo;
import com.bochocredit.util.*;
import com.bochocredit.util.classes.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SimulacionService {

    // Repositorios
    private final SimulacionRepository simRepos;
    private final PagoRepository pagoRepos;
    private final ClienteRepository clientRepos;
    private final VehiculoRepository vehiculoRepos;
    private final UsuarioRepository userRepos;

    // Servicios
    private final VehiculoBloqueoService bloqueoService;

    // Componentes de Utilidad
    private final CalculadoraPagos calc;
    private final ClassMapper mapper;

    // ─────────────────────────────────────────────
    // CREAR — equivalente a /creditos/nuevo (POST)
    // ─────────────────────────────────────────────

    public DetalleSimulacionDTO crear(RequestSimulacion req) {
        EstadoBloqueo estado = bloqueoService.esVehiculoBloqueado(req.getVehiculoId());
        if (estado.bloqueado()) {
            throw new VehiculoBloqueadoException(
                    "El vehículo seleccionado está bloqueado por un plan de pagos activo hasta el "
                            + estado.bloqueadoHasta() + ".");
        }

        Cliente cliente = clientRepos.findById(req.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + req.getClienteId()));
        Vehiculo vehiculo = vehiculoRepos.findById(req.getVehiculoId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado: " + req.getVehiculoId()));
        Usuario usuario = userRepos.findById(req.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado: " + req.getUsuarioId()));

        ParametrosCalculo params = calc.calcular(req);

        var cronograma = calc.crearCronograma(params, req.getCok() / 100.0);
        
        var temp = generarSimulacion(req, params, cronograma, cliente, vehiculo, usuario);
        
        Simulacion sim = simRepos.save(temp);
        guardarPagos(sim, cronograma.getFilas());

        return mapper.toDetalle(sim);
    }

    // ─────────────────────────────────────────────
    // EDITAR / RECALCULAR — equivalente a /creditos/<id>/editar (POST)
    // ─────────────────────────────────────────────

    public DetalleSimulacionDTO actualizar(Long id, RequestSimulacion req) {
        Simulacion sim = simRepos.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Simulación no encontrada: " + id));

        // Solo valida bloqueo si el vehículo cambió
        if (!sim.getVehiculo().getId().equals(req.getVehiculoId())) {
            EstadoBloqueo estado = bloqueoService.esVehiculoBloqueado(req.getVehiculoId());
            if (estado.bloqueado()) {
                throw new VehiculoBloqueadoException(
                        "El vehículo seleccionado está bloqueado por un plan de pagos activo hasta el "
                                + estado.bloqueadoHasta() + ".");
            }
        }

        Cliente cliente = clientRepos.findById(req.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + req.getClienteId()));
        Vehiculo vehiculo = vehiculoRepos.findById(req.getVehiculoId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado: " + req.getVehiculoId()));

        ParametrosCalculo params = calc.calcular(req);

        var cronograma = calc.crearCronograma(params, req.getCok() / 100.0);

        var temp = generarSimulacion(req, params, cronograma, cliente, vehiculo, sim.getUsuario());
        temp.setId(sim.getId());

        sim = simRepos.save(temp);
        pagoRepos.deleteBySimulacionId(id);
        guardarPagos(sim, cronograma.getFilas());

        return mapper.toDetalle(sim);
    }

    // ─────────────────────────────────────────────
    // ELEGIR PLAN — equivalente a /creditos/<id>/elegir
    // ─────────────────────────────────────────────

    public void elegir(Long id) {
        Simulacion sim = simRepos.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Simulación no encontrada: " + id));

        Long vehiculoId = sim.getVehiculo().getId();

        // Desmarcar cualquier otra simulación elegida para este mismo vehículo
        List<Simulacion> todasDelVehiculo = simRepos.findAllByVehiculoId(vehiculoId);
        todasDelVehiculo.forEach(s -> {
            s.setEsElegido(false);
            simRepos.save(s);
        });

        sim.setEsElegido(true);
        simRepos.save(sim);
    }


    // ─────────────────────────────────────────────
    // CREAR — equivalente a /creditos/nuevo (POST)
    // ─────────────────────────────────────────────

    public SimulacionCronogramaDTO simularCronograma(RequestSimulacion req) {
        ParametrosCalculo params = calc.calcular(req);
        var cronograma = calc.crearCronograma(params, req.getCok() / 100.0);
        return mapper.toSimCrono(req, params, cronograma);
    }




    // ─────────────────────────────────────────────
    // LECTURA
    // ─────────────────────────────────────────────

    public List<SimulacionListItemDTO> listar() {
        return simRepos.findAllByOrderByCreadoEnDesc().stream()
                .map(mapper::toListItem)
                .toList();
    }

    public List<SimulacionListItemDTO> listarPorCliente(Long clienteId) {
        return simRepos.findByClienteIdOrderByCreadoEnDesc(clienteId).stream()
                .map(mapper::toListItem)
                .toList();
    }

    public DetalleSimulacionDTO obtener(Long id) {
        Simulacion sim = simRepos.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Simulación no encontrada: " + id));
        return mapper.toDetalle(sim);
    }

    public List<Pago> listarPagosPorSimulacion(Long id) {
        return pagoRepos.buscarPorSimulacion(id);
    }

    // ─────────────────────────────────────────────
    // Helpers internos
    // ─────────────────────────────────────────────

    private void guardarPagos(Simulacion sim, List<EntradaCronograma> filas) {
        var fecha = LocalDateTime.now();
        for (var f : filas) {
            var pago = mapper.toPago(f, fecha, sim);
            pagoRepos.save(pago);
        }
    }


    private BigDecimal bd2(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal bd4(double v) {
        return BigDecimal.valueOf(v).setScale(4, RoundingMode.HALF_UP);
    }


    private Simulacion generarSimulacion(RequestSimulacion r, ParametrosCalculo p, ResultadoCronograma cronograma,
                                         Cliente cliente, Vehiculo vehiculo, Usuario usuario) {
        var tempSim = new Simulacion();
        var temp = r.getGastosIniciales();
        BigDecimal notarial = bd2(temp.getOrDefault("NOTARIAL", 0.0));
        BigDecimal registra = bd2(temp.getOrDefault("REGISTRAL", 0.0));

        tempSim.setPrestamo(bd2(p.getValorPrestamo()));
        tempSim.setCok(bd4(r.getCok()));
        tempSim.setTcea(bd4(cronograma.getTceaPct()));
        tempSim.setVan(bd2(cronograma.getVan()));
        tempSim.setTir(bd4(cronograma.getTirMensualPct()));
        tempSim.setSaldoFinanciado(bd2(p.getSaldoFinanciado()));
        tempSim.setPlazoMeses(r.getPlazoMeses());
        tempSim.setPorcCuotaInicial(bd4(r.getCuotaInicialPct()));
        tempSim.setPorcCuotaFinal(bd4(r.getCuotaFinalPct()));
        tempSim.setTipoPeriodoGracia(p.getTipoGracia());
        tempSim.setPeriodoGraciaMeses(p.getMesesGracia());
        tempSim.setTipoMoneda(r.getMoneda());
        tempSim.setEsElegido(false);
        tempSim.setCreadoEn(LocalDateTime.now());
        tempSim.setUsuario(usuario);
        tempSim.setCliente(cliente);
        tempSim.setVehiculo(vehiculo);
        tempSim.setPrecioVehiculo(bd2(r.getValorVenta()));
        tempSim.setTipoTasa(r.getTipoTasa());
        tempSim.setTasaValor(bd4(r.getTasaInteres()));
        tempSim.setCapitalizacion(r.getCapitalizacion());
        tempSim.setTem(bd4(p.getTem()));
        tempSim.setTsd(bd2(r.getSeguroDesgPct()));
        tempSim.setTsv(bd2(r.getSeguroVehicularPct()));
        tempSim.setPortes(bd2(p.getPortes()));
        tempSim.setGastosAdmin(bd2(p.getGastosAdmin()));
        tempSim.setGps(bd2(p.getGps()));
        tempSim.setGastosNotar(notarial);
        tempSim.setGastosRegist(registra);

        return tempSim;
    }

}
