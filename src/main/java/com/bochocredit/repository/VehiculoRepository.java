package com.bochocredit.repository;

import com.bochocredit.entity.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    @Query("SELECT COUNT(v) FROM Vehiculo v")
    long countTotal();

    @Query("SELECT s.vehiculo FROM Simulacion s " +
            "WHERE s.cliente.id = :id")
    List<Vehiculo> buscarPorCliente(@Param("id") Long id);
}
