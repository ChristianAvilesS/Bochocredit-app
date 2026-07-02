package com.bochocredit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "vehiculos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "marca", nullable = false, length = 80)
    private String marca;

    @Column(name = "modelo", nullable = false, length = 80)
    private String modelo;

    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Column(name = "precio", nullable = false, precision = 14, scale = 2)
    private BigDecimal precio;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "disponibilidad", nullable = false, length = 20)
    @Builder.Default
    private Disponibilidad disponibilidad = Disponibilidad.DISPONIBLE;

    public enum Disponibilidad {
        DISPONIBLE, RESERVADO, VENDIDO
    }

    @Transient
    public String getNombreCompleto() {
        return marca + " " + modelo;
    }
}
