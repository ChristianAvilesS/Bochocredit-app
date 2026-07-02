package com.bochocredit.service;

import com.bochocredit.dto.vehiculo.VehiculoDtos.VehiculoRequest;
import com.bochocredit.dto.vehiculo.VehiculoDtos.VehiculoResponse;
import com.bochocredit.entity.Vehiculo;
import com.bochocredit.exception.ResourceNotFoundException;
import com.bochocredit.repository.VehiculoRepository;
import com.bochocredit.service.VehiculoBloqueoService.EstadoBloqueo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final VehiculoBloqueoService bloqueoService;

    public List<VehiculoResponse> listar() {
        return vehiculoRepository.findAll().stream()
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .map(this::toResponse)
                .toList();
    }

    public VehiculoResponse obtener(Long id) {
        return toResponse(buscarOFallar(id));
    }

    public VehiculoResponse crear(VehiculoRequest req) {
        Vehiculo vehiculo = Vehiculo.builder()
                .marca(req.marca())
                .modelo(req.modelo())
                .anio(req.anio())
                .precio(req.precio())
                .descripcion(req.descripcion())
                .disponibilidad(req.disponibilidad() != null ? req.disponibilidad() : Vehiculo.Disponibilidad.DISPONIBLE)
                .build();
        return toResponse(vehiculoRepository.save(vehiculo));
    }

    public VehiculoResponse actualizar(Long id, VehiculoRequest req) {
        Vehiculo vehiculo = buscarOFallar(id);
        vehiculo.setMarca(req.marca());
        vehiculo.setModelo(req.modelo());
        vehiculo.setAnio(req.anio());
        vehiculo.setPrecio(req.precio());
        vehiculo.setDescripcion(req.descripcion());
        if (req.disponibilidad() != null) {
            vehiculo.setDisponibilidad(req.disponibilidad());
        }
        return toResponse(vehiculo);
    }

    private Vehiculo buscarOFallar(Long id) {
        return vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado: " + id));
    }

    private VehiculoResponse toResponse(Vehiculo v) {
        EstadoBloqueo estado = bloqueoService.esVehiculoBloqueado(v.getId());
        return new VehiculoResponse(
                v.getId(), v.getMarca(), v.getModelo(), v.getAnio(), v.getPrecio(),
                v.getDescripcion(), v.getDisponibilidad(),
                estado.bloqueado(), estado.bloqueadoHasta()
        );
    }
}
