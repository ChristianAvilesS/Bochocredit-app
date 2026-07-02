package com.bochocredit.service;

import com.bochocredit.dto.cliente.ClienteDtos.ClienteRequest;
import com.bochocredit.dto.cliente.ClienteDtos.ClienteResponse;
import com.bochocredit.entity.Cliente;
import com.bochocredit.exception.DuplicateResourceException;
import com.bochocredit.exception.ResourceNotFoundException;
import com.bochocredit.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public List<ClienteResponse> listar() {
        return clienteRepository.findAll().stream()
                .sorted((a, b) -> b.getId().compareTo(a.getId())) // más recientes primero
                .map(this::toResponse)
                .toList();
    }

    public ClienteResponse obtener(Long id) {
        return toResponse(buscarOFallar(id));
    }

    public ClienteResponse crear(ClienteRequest req) {
        if (clienteRepository.existsByDni(req.dni())) {
            throw new DuplicateResourceException("El DNI ya está registrado.");
        }
        Cliente cliente = Cliente.builder()
                .nombres(req.nombres())
                .apellidos(req.apellidos())
                .dni(req.dni())
                .telefono(req.telefono())
                .email(req.email())
                .direccion(req.direccion())
                .build();
        return toResponse(clienteRepository.save(cliente));
    }

    public ClienteResponse actualizar(Long id, ClienteRequest req) {
        Cliente cliente = buscarOFallar(id);
        if (clienteRepository.existsByDniAndIdNot(req.dni(), id)) {
            throw new DuplicateResourceException("El DNI ya está registrado por otro cliente.");
        }
        cliente.setNombres(req.nombres());
        cliente.setApellidos(req.apellidos());
        cliente.setDni(req.dni());
        cliente.setTelefono(req.telefono());
        cliente.setEmail(req.email());
        cliente.setDireccion(req.direccion());
        return toResponse(cliente);
    }

    public void eliminar(Long id) {
        Cliente cliente = buscarOFallar(id);
        clienteRepository.delete(cliente);
    }

    private Cliente buscarOFallar(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id));
    }

    private ClienteResponse toResponse(Cliente c) {
        return new ClienteResponse(
                c.getId(), c.getNombres(), c.getApellidos(), c.getNombreCompleto(),
                c.getDni(), c.getTelefono(), c.getEmail(), c.getDireccion(), c.getCreatedAt()
        );
    }
}
