package com.bochocredit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "simulaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Simulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Indicadores financieros obligatorios ──
    @Column(name = "tcea", nullable = false, precision = 10, scale = 4)
    private BigDecimal tcea;

    @Column(name = "van", nullable = false, precision = 16, scale = 4)
    private BigDecimal van;

    @Column(name = "tir", nullable = false, precision = 10, scale = 6)
    private BigDecimal tir;

    @Column(name = "saldo_financiado", nullable = false, precision = 14, scale = 2)
    private BigDecimal saldoFinanciado;

    @Column(name = "prestamo", nullable = false, precision = 14, scale = 2)
    private BigDecimal prestamo;

    // ── Condiciones del crédito ──
    @Column(name = "plazo_meses", nullable = false)
    private Integer plazoMeses;

    @Column(name = "porc_cuota_inicial", nullable = false, precision = 6, scale = 2)
    private BigDecimal porcCuotaInicial;

    @Column(name = "porc_cuota_final", nullable = false, precision = 6, scale = 2)
    private BigDecimal porcCuotaFinal;

    @Column(name = "tipo_periodo_gracia", nullable = false, length = 20)
    private String tipoPeriodoGracia;

    @Column(name = "periodo_gracia_meses", nullable = false)
    private Integer periodoGraciaMeses;

    @Column(name = "tipo_moneda", nullable = false, length = 10)
    private String tipoMoneda;

    @Column(name = "es_elegido", nullable = false)
    private Boolean esElegido;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    // ── Relaciones ──
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_vehiculo", nullable = false)
    private Vehiculo vehiculo;



    // ── Parámetros denormalizados (snapshot al momento del cálculo) ──

    @Column(name = "precio_vehiculo", precision = 14, scale = 2)
    private BigDecimal precioVehiculo;

    @Column(name = "tipo_tasa", length = 30)
    private String tipoTasa;

    @Column(name = "tasa_valor", precision = 10, scale = 4)
    private BigDecimal tasaValor;

    @Column(name = "capitalizacion")
    private int capitalizacion;

    @Column(name = "tem", precision = 14, scale = 10)
    private BigDecimal tem;

    @Column(name = "tsd", precision = 10, scale = 4)
    private BigDecimal tsd;

    @Column(name = "tsv", precision = 10, scale = 4)
    private BigDecimal tsv;

    @Column(name = "portes", precision = 10, scale = 2)
    private BigDecimal portes;

    @Column(name = "gastos_admin", precision = 10, scale = 2)
    private BigDecimal gastosAdmin;

    @Column(name = "gps", precision = 10, scale = 2)
    private BigDecimal gps;

    @Column(name = "gastos_notariales", precision = 10, scale = 2)
    private BigDecimal gastosNotar;

    @Column(name = "gastos_registrales", length = 30)
    private BigDecimal gastosRegist;

    @Column(name = "cok", precision = 6, scale = 4)
    private BigDecimal cok;

    @OneToMany(mappedBy = "simulacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pago> pagos = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (creadoEn == null) {
            creadoEn = LocalDateTime.now();
        }
        if (esElegido == null) {
            esElegido = false;
        }
    }

}
