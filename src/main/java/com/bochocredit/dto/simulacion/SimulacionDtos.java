package com.bochocredit.dto.simulacion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SimulacionDtos {

    /** Vista resumida — para listados (equivalente a la tabla de /creditos). */
    public record SimulacionListItem(
            Long id,
            String cliente,
            String vehiculo,
            String moneda,
            BigDecimal saldoFinanciado,
            Integer plazoMeses,
            BigDecimal tcea,
            BigDecimal van,
            BigDecimal tir,
            Boolean esElegido,
            LocalDateTime creadoEn
    ) {}

    /** Vista detallada — para /creditos/{id} (incluye cronograma completo). */
    public record SimulacionDetalle(
            Long id,
            BigDecimal tcea,
            BigDecimal van,
            BigDecimal tir,
            BigDecimal saldoFinanciado,
            Integer plazoMeses,
            BigDecimal porcCuotaInicial,
            String graciaTipo,
            Integer graciaMeses,
            String moneda,
            Boolean esElegido,
            LocalDateTime creadoEn,

            String tipoTasa,
            BigDecimal tasaValor,
            String capitalizacion,
            BigDecimal tem,
            BigDecimal tsd,
            BigDecimal tsv,
            BigDecimal portes,
            BigDecimal gastosAdmin,
            BigDecimal gps,
            String metodoPago,
            BigDecimal pctCuotaFinal,
            BigDecimal cok,

            Long clienteId,
            String clienteNombre,
            String clienteDni,
            String clienteEmail,
            String clienteTelefono,

            Long vehiculoId,
            String vehiculoMarca,
            String vehiculoModelo,
            Integer vehiculoAnio,
            BigDecimal precioVehiculo,

            List<Map<String, Object>> cronograma
    ) {}

    /** Respuesta de la vista previa en tiempo real (equivalente a /api/calcular). */
    public record CalculoPreview(
            BigDecimal sf,
            BigDecimal ciMonto,
            BigDecimal tem,
            BigDecimal tea,
            BigDecimal van,
            BigDecimal tir,
            BigDecimal tcea,
            BigDecimal cuotaRegular,
            List<Map<String, Object>> cronograma
    ) {}
}
