package com.bochocredit.controller;

import com.bochocredit.dto.simulacion.SimulacionDtos;
import com.bochocredit.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService servicio;

    @GetMapping("/cantidad-clientes")
    public ResponseEntity<Long> obtenerCantidadClientes() {
        return ResponseEntity.ok(servicio.cantidadClientes());
    }

    @GetMapping("/cantidad-vehiculos")
    public ResponseEntity<Long> obtenerCantidadVehiculos() {
        return ResponseEntity.ok(servicio.cantidadVehiculos());
    }

    @GetMapping("/cantidad-creditos")
    public ResponseEntity<Long> obtenerCantidadCreditos() {
        return ResponseEntity.ok(servicio.cantidadCreditos());
    }

    @GetMapping("/creditos-recientes")
    public ResponseEntity<List<SimulacionDtos.SimulacionListItem>> top5CreditosMasRecientes() {
        return ResponseEntity.ok(servicio.top5CreditosMasRecientes());
    }

}
