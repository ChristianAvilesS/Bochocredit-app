package com.bochocredit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "tasas_interes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TasaInteres {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_tasa", nullable = false, length = 30)
    private String tipoTasa;

    @Column(name = "tasa_interes", nullable = false, precision = 8, scale = 4)
    @Builder.Default
    private BigDecimal tasaInteres = BigDecimal.valueOf(0.1);

    @Column(name = "dias_capitalizacion", nullable = false)
    @Builder.Default
    private Integer diasCapitalizacion = 1;

    @Column(name = "dias_tasa", nullable = false)
    @Builder.Default
    private Integer diasTasa = 360;
}
