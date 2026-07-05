package com.bochocredit.controller;

import com.bochocredit.dto.simulacion.DetalleSimulacionDTO;
import com.bochocredit.dto.simulacion.PagoDTO;
import com.bochocredit.dto.simulacion.SimulacionCronogramaDTO;
import com.bochocredit.dto.simulacion.SimulacionListItemDTO;
import com.bochocredit.entity.Pago;
import com.bochocredit.util.ClassMapper;
import com.bochocredit.service.SimulacionService;
import com.bochocredit.util.classes.RequestSimulacion;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/creditos")
@RequiredArgsConstructor
public class SimulacionController {

    private final SimulacionService simulacionService;
    private final ClassMapper mapper;

    @GetMapping
    public ResponseEntity<List<SimulacionListItemDTO>> listar() {
        return ResponseEntity.ok(simulacionService.listar());
    }


    @GetMapping("/clientes/{clienteId}")
    public ResponseEntity<List<SimulacionListItemDTO>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(simulacionService.listarPorCliente(clienteId));
    }


    @GetMapping("/{id}")
    public ResponseEntity<DetalleSimulacionDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(simulacionService.obtener(id));
    }


    @PostMapping
    public ResponseEntity<DetalleSimulacionDTO> crear(@Valid @RequestBody RequestSimulacion request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(simulacionService.crear(request));
    }


    @PutMapping("/{id}")
    public ResponseEntity<DetalleSimulacionDTO> actualizar(@PathVariable Long id,
                                                           @Valid @RequestBody RequestSimulacion request) {
        return ResponseEntity.ok(simulacionService.actualizar(id, request));
    }

    /** Marca una simulación como el plan elegido por el cliente (bloquea el vehículo). */
    @PatchMapping("/{id}/elegir")
    public ResponseEntity<Void> elegir(@PathVariable Long id) {
        simulacionService.elegir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pagos/{id}")
    public List<PagoDTO> listarPagosPorSimulacion(@PathVariable Long id) {
        return toDTOList(simulacionService.listarPagosPorSimulacion(id));
    }

    @PostMapping("/simulacion")
    public ResponseEntity<SimulacionCronogramaDTO> simularCronograma(@Valid @RequestBody
                                                                         RequestSimulacion request) {
        return ResponseEntity.ok(simulacionService.simularCronograma(request));
    }


    private List<PagoDTO> toDTOList(List<Pago> pagos) {
        return pagos.stream()
                .map(mapper::toPagoDto)
                .toList();
    }
}
