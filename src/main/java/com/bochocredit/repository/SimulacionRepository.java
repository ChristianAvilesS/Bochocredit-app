package com.bochocredit.repository;

import com.bochocredit.entity.Simulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SimulacionRepository extends JpaRepository<Simulacion, Long> {

    @Query("""
        SELECT COUNT(s) FROM Simulacion s
        JOIN s.cliente c
        """)
    Long countByEsElegidoTrue();

    @Query("""
        SELECT s FROM Simulacion s
        JOIN s.cliente c
        ORDER BY s.creadoEn DESC
        LIMIT 10
        """)
    List<Simulacion> findRecientesElegidas();

    List<Simulacion> findByClienteIdOrderByCreadoEnDesc(Long clienteId);

    List<Simulacion> findAllByOrderByCreadoEnDesc();

    @Query("""
        SELECT s FROM Simulacion s
        WHERE s.vehiculo.id = :vehiculoId AND s.esElegido = true
        """)
    List<Simulacion> findElegidasByVehiculoId(@Param("vehiculoId") Long vehiculoId);

    /** Marca como NO elegidas todas las simulaciones de un vehículo (antes de elegir una nueva). */
    @Query("""
        SELECT s FROM Simulacion s
        WHERE s.vehiculo.id = :vehiculoId
        """)
    List<Simulacion> findAllByVehiculoId(@Param("vehiculoId") Long vehiculoId);
}
