package com.bochocredit.service;

import com.bochocredit.dto.simulacion.SimulacionListItemDTO;
import com.bochocredit.repository.ClienteRepository;
import com.bochocredit.repository.SimulacionRepository;
import com.bochocredit.repository.VehiculoRepository;
import com.bochocredit.util.ClassMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final SimulacionRepository simulacionRepository;
    private final ClienteRepository clienteRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ClassMapper mapper;

    public Long cantidadCreditos() {
        return simulacionRepository.countByEsElegidoTrue();
    }

    public Long cantidadClientes() {
        return clienteRepository.countTotal();
    }

    public Long cantidadVehiculos() {
        return vehiculoRepository.countTotal();
    }

    public List<SimulacionListItemDTO> top5CreditosMasRecientes() {
        return simulacionRepository.findRecientesElegidas().stream()
                .map(mapper::toListItem)
                .toList();
    }

}
