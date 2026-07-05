package com.bochocredit.dto.simulacion;

import com.bochocredit.util.classes.EntradaCronograma;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimulacionCronogramaDTO {
    private BigDecimal tcea;
    private BigDecimal van;
    private BigDecimal tir;

    private BigDecimal saldoFinanciado;
    private BigDecimal prestamo;
    private Integer plazoMeses;
    private BigDecimal porcCuotaInicial;
    private BigDecimal pctCuotaFinal;
    private String graciaTipo;
    private Integer graciaMeses;
    private String moneda;

    private BigDecimal tem;
    private BigDecimal cok;

    private List<PagoDTO> cuotas = new ArrayList<>();
}
