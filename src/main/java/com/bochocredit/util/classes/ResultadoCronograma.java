package com.bochocredit.util.classes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoCronograma {
    private List<EntradaCronograma> filas;
    private double van;
    private double tirMensualPct;
    private double tceaPct;
}
