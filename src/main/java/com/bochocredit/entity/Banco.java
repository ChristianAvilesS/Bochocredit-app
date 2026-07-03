package com.bochocredit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bancos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Banco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(name = "ruc", nullable = false, length = 15)
    private String ruc;

    @Column(name = "direccion", nullable = false, length = 255)
    private String direccion;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
