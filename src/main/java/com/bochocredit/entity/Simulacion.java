package com.bochocredit.entity;

import com.bochocredit.entity.converter.CronogramaJsonConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    // ── Condiciones del crédito ──
    @Column(name = "plazo_meses", nullable = false)
    private Integer plazoMeses;

    @Column(name = "cantidad_cuotas", nullable = false)
    private Integer cantidadCuotas;

    @Column(name = "porc_cuota_inicial", nullable = false, precision = 6, scale = 2)
    private BigDecimal porcCuotaInicial;

    @Column(name = "tipo_periodo_gracia", nullable = false, length = 20)
    private String tipoPeriodoGracia;

    @Column(name = "periodo_gracia_meses", nullable = false, precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal periodoGraciaMeses = BigDecimal.ZERO;

    @Column(name = "tipo_moneda", nullable = false, length = 10)
    private String tipoMoneda;

    @Column(name = "es_elegido", nullable = false)
    @Builder.Default
    private Boolean esElegido = false;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_banco", nullable = false)
    private Banco banco;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tasa", nullable = false)
    private TasaInteres tasaInteres;

    // ── Parámetros denormalizados (snapshot al momento del cálculo) ──
    @Column(name = "moneda", length = 10)
    private String moneda;

    @Column(name = "precio_vehiculo", precision = 14, scale = 2)
    private BigDecimal precioVehiculo;

    @Column(name = "cuota_inicial_pct", precision = 6, scale = 2)
    private BigDecimal cuotaInicialPct;

    @Column(name = "cuota_inicial_monto", precision = 14, scale = 2)
    private BigDecimal cuotaInicialMonto;

    @Column(name = "tipo_tasa", length = 30)
    private String tipoTasa;

    @Column(name = "tasa_valor", precision = 10, scale = 4)
    private BigDecimal tasaValor;

    @Column(name = "capitalizacion", length = 20)
    private String capitalizacion;

    @Column(name = "tem", precision = 14, scale = 10)
    private BigDecimal tem;

    @Column(name = "gracia_tipo", length = 20)
    private String graciaTipo;

    @Column(name = "gracia_meses")
    private Integer graciaMeses;

    @Column(name = "tsd", precision = 10, scale = 4)
    private BigDecimal tsd;

    @Column(name = "tsv", precision = 10, scale = 4)
    private BigDecimal tsv;

    @Column(name = "portes", precision = 10, scale = 2)
    private BigDecimal portes;

    @Column(name = "gastos_admin", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal gastosAdmin = BigDecimal.ZERO;

    @Column(name = "gps", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal gps = BigDecimal.ZERO;

    @Column(name = "metodo_pago", length = 30)
    @Builder.Default
    private String metodoPago = "regular";

    @Column(name = "pct_cuota_final", precision = 6, scale = 4)
    @Builder.Default
    private BigDecimal pctCuotaFinal = BigDecimal.ZERO;

    @Column(name = "cok", precision = 6, scale = 4)
    @Builder.Default
    private BigDecimal cok = BigDecimal.ZERO;

    /**
     * Cronograma completo, serializado a JSON vía {@link CronogramaJsonConverter}.
     * La columna física en PostgreSQL es {@code jsonb} (ver Flyway V1).
     * En pruebas (perfil "test", H2) el esquema se genera con
     * {@code ddl-auto: create} en vez de {@code validate}, por lo que H2
     * crea la columna a partir de este {@code columnDefinition} sin
     * problema (H2 simplemente la trata como texto largo).
     */
    @Convert(converter = CronogramaJsonConverter.class)
    @Column(name = "cronograma", columnDefinition = "jsonb")
    private List<Map<String, Object>> cronograma;

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
