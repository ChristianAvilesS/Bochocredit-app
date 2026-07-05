package com.bochocredit.service;

import com.bochocredit.entity.Simulacion;
import com.bochocredit.repository.SimulacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Port de `es_vehiculo_bloqueado(db, id_vehiculo)` del Flask original.
 * Un vehículo queda bloqueado mientras exista una simulación "elegida" (es_elegido=true)
 * cuyo plazo (creadoEn + plazoMeses) todavía no haya vencido.
 */
@Service
@RequiredArgsConstructor
public class VehiculoBloqueoService {

    private static final DateTimeFormatter SALIDA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final SimulacionRepository simulacionRepository;

    public record EstadoBloqueo(boolean bloqueado, String bloqueadoHasta) {
        static final EstadoBloqueo LIBRE = new EstadoBloqueo(false, null);
    }

    public EstadoBloqueo esVehiculoBloqueado(Long idVehiculo) {
        List<Simulacion> elegidas = simulacionRepository.findElegidasByVehiculoId(idVehiculo);
        LocalDateTime ahora = LocalDateTime.now();

        for (Simulacion sim : elegidas) {
            LocalDateTime fin = sim.getCreadoEn().plusMonths(sim.getPlazoMeses());
            if (ahora.isBefore(fin)) {
                return new EstadoBloqueo(true, fin.format(SALIDA));
            }
        }
        return EstadoBloqueo.LIBRE;
    }
}
