package com.bochocredit.controller;

import com.bochocredit.dto.simulacion.SimulacionDtos.CalculoPreview;
import com.bochocredit.dto.simulacion.SimulacionDtos.SimulacionDetalle;
import com.bochocredit.dto.simulacion.SimulacionDtos.SimulacionListItem;
import com.bochocredit.dto.simulacion.SimulacionRequest;
import com.bochocredit.service.SimulacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SimulacionController {

    private final SimulacionService simulacionService;

    /** Equivalente a /api/calcular (POST) — vista previa en tiempo real, sin persistir. */
    @PostMapping("/calcular")
    public ResponseEntity<CalculoPreview> calcular(@Valid @RequestBody SimulacionRequest request) {
        return ResponseEntity.ok(simulacionService.calcular(request));
    }

    /** Equivalente a /creditos (GET) — listado completo. */
    @GetMapping("/creditos")
    public ResponseEntity<List<SimulacionListItem>> listar() {
        return ResponseEntity.ok(simulacionService.listar());
    }

    /** Listado de créditos filtrado por cliente — usado en el perfil de cliente. */
    @GetMapping("/clientes/{clienteId}/creditos")
    public ResponseEntity<List<SimulacionListItem>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(simulacionService.listarPorCliente(clienteId));
    }

    /** Equivalente a /creditos/{id} (GET) — detalle con cronograma completo. */
    @GetMapping("/creditos/{id}")
    public ResponseEntity<SimulacionDetalle> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(simulacionService.obtener(id));
    }

    /** Equivalente a /creditos/nuevo (POST) — calcula y persiste una nueva oferta. */
    @PostMapping("/creditos")
    public ResponseEntity<SimulacionDetalle> crear(@Valid @RequestBody SimulacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(simulacionService.crear(request));
    }

    /** Equivalente a /creditos/{id}/editar (POST) — recalcula y actualiza. */
    @PutMapping("/creditos/{id}")
    public ResponseEntity<SimulacionDetalle> actualizar(@PathVariable Long id, @Valid @RequestBody SimulacionRequest request) {
        return ResponseEntity.ok(simulacionService.actualizar(id, request));
    }

    /** Marca una simulación como el plan elegido por el cliente (bloquea el vehículo). */
    @PatchMapping("/creditos/{id}/elegir")
    public ResponseEntity<Void> elegir(@PathVariable Long id) {
        simulacionService.elegir(id);
        return ResponseEntity.noContent().build();
    }
}
