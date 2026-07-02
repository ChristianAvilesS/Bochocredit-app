package com.bochocredit.service;

import com.bochocredit.dto.simulacion.SimulacionDtos;
import com.bochocredit.entity.Simulacion;
import com.bochocredit.repository.ClienteRepository;
import com.bochocredit.repository.SimulacionRepository;
import com.bochocredit.repository.VehiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final SimulacionRepository simulacionRepository;
    private final ClienteRepository clienteRepository;
    private final VehiculoRepository vehiculoRepository;

    public Long cantidadCreditos() {
        return simulacionRepository.countByEsElegidoTrue();
    }

    public Long cantidadClientes() {
        return clienteRepository.countTotal();
    }

    public Long cantidadVehiculos() {
        return vehiculoRepository.countTotal();
    }

    public List<SimulacionDtos.SimulacionListItem> top5CreditosMasRecientes() {
        return simulacionRepository.findRecientesElegidas().stream()
                .map(this::toListItem)
                .toList();
    }

    private SimulacionDtos.SimulacionListItem toListItem(Simulacion s) {
        return new SimulacionDtos.SimulacionListItem(
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


}
