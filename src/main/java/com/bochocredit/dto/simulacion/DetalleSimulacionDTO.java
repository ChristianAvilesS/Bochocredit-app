package com.bochocredit.dto.simulacion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetalleSimulacionDTO {
    private Long id;
    private BigDecimal tcea;
    private BigDecimal van;
    private BigDecimal tir;

    private BigDecimal valorVenta;
    private BigDecimal saldoFinanciado;
    private BigDecimal prestamo;
    private Integer plazoMeses;
    private BigDecimal porcCuotaInicial;
    private BigDecimal pctCuotaFinal;
    private String graciaTipo;
    private Integer graciaMeses;
    private String moneda;
    private Boolean esElegido;
    private LocalDateTime creadoEn;

    private String tipoTasa;
    private BigDecimal tasaValor;
    private int capitalizacion;
    private BigDecimal tem;
    private BigDecimal tsd;
    private BigDecimal tsv;
    private BigDecimal portes;
    private BigDecimal gastosAdmin;
    private BigDecimal gps;
    private BigDecimal cok;

    private Long clienteId;
    private String clienteNombre;
    private String clienteDni;
    private String clienteEmail;
    private String clienteTelefono;

    private Long vehiculoId;
    private String vehiculoMarca;
    private String vehiculoModelo;
    private Integer vehiculoAnio;
    private BigDecimal precioVehiculo;


    private BigDecimal notariales;
    private BigDecimal registrales;
}
