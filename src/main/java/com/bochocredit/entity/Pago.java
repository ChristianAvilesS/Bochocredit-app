package com.bochocredit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "num_cuota", nullable = false)
    private Integer numCuota;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @Column(name = "tipo_gracia", nullable = false, length = 20)
    private String tipoGracia;

    @Column(name = "esta_pagado", nullable = false)
    private Boolean estaPagado = false;

    // ── Cronograma "cuota final" (Compra Inteligente) ──
    @Column(name = "saldo_inicial_cf", nullable = false, precision = 14, scale = 4)
    private BigDecimal saldoInicialCf;

    @Column(name = "interes_cf", nullable = false, precision = 14, scale = 4)
    private BigDecimal interesCf;

    @Column(name = "amortizacion_cf", nullable = false, precision = 14, scale = 4)
    private BigDecimal amortizacionCf;

    @Column(name = "seguro_desgravamen_cf", nullable = false, precision = 14, scale = 4)
    private BigDecimal seguroDesgravamenCf;

    @Column(name = "saldo_final_cf", nullable = false, precision = 14, scale = 4)
    private BigDecimal saldoFinalCf;

    // ── Cronograma regular ──
    @Column(name = "saldo_inicial", nullable = false, precision = 14, scale = 4)
    private BigDecimal saldoInicial;

    @Column(name = "interes", nullable = false, precision = 14, scale = 4)
    private BigDecimal interes;

    @Column(name = "amortizacion", nullable = false, precision = 14, scale = 4)
    private BigDecimal amortizacion;

    @Column(name = "cuota", nullable = false, precision = 14, scale = 4)
    private BigDecimal cuota;

    @Column(name = "seguro_desgravamen", nullable = false, precision = 14, scale = 4)
    private BigDecimal seguroDesgravamen;

    @Column(name = "seguro_riesgo", nullable = false, precision = 14, scale = 4)
    private BigDecimal seguroRiesgo;

    @Column(name = "portes", nullable = false, precision = 14, scale = 4)
    private BigDecimal portes;

    @Column(name = "gastos_admin", nullable = false, precision = 14, scale = 4)
    private BigDecimal gastosAdmin = BigDecimal.ZERO;

    @Column(name = "gps", nullable = false, precision = 14, scale = 4)
    private BigDecimal gps = BigDecimal.ZERO;

    @Column(name = "saldo_final", nullable = false, precision = 14, scale = 4)
    private BigDecimal saldoFinal;

    @Column(name = "flujo", nullable = false, precision = 14, scale = 4)
    private BigDecimal flujo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_simulacion", nullable = false)
    private Simulacion simulacion;

    @PrePersist
    void prePersist() {
        if (fechaPago == null) {
            fechaPago = LocalDateTime.now();
        }
    }
}
