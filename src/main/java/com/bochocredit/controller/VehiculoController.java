package com.bochocredit.controller;

import com.bochocredit.dto.vehiculo.VehiculoDtos.VehiculoRequest;
import com.bochocredit.dto.vehiculo.VehiculoDtos.VehiculoResponse;
import com.bochocredit.service.VehiculoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehiculos")
@RequiredArgsConstructor
public class VehiculoController {

    private final VehiculoService vehiculoService;

    @GetMapping
    public ResponseEntity<List<VehiculoResponse>> listar() {
        return ResponseEntity.ok(vehiculoService.listar());
    }

    @GetMapping("/clientes/{id}")
    public ResponseEntity<List<VehiculoResponse>> listarPorCliente(@PathVariable Long id) {
        return ResponseEntity.ok(vehiculoService.listarPorCliente(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehiculoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(vehiculoService.obtener(id));
    }

    @PostMapping
    public ResponseEntity<VehiculoResponse> crear(@Valid @RequestBody VehiculoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehiculoService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehiculoResponse> actualizar(@PathVariable Long id, @Valid @RequestBody VehiculoRequest request) {
        return ResponseEntity.ok(vehiculoService.actualizar(id, request));
    }
}
