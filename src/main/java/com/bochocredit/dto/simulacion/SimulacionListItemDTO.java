package com.bochocredit.dto.simulacion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulacionListItemDTO {
    Long id;
    String cliente;
    String vehiculo;
    String moneda;
    BigDecimal prestamo;
    Integer plazoMeses;
    BigDecimal tcea;
    BigDecimal van;
    BigDecimal tir;
    Boolean esElegido;
    LocalDateTime creadoEn;
}
