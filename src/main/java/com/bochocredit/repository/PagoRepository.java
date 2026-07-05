package com.bochocredit.repository;

import com.bochocredit.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    @Query("SELECT p FROM Pago p " +
            "WHERE p.simulacion.id = :id ")
    List<Pago> buscarPorSimulacion(@Param("id") Long simulacionId);
    void deleteBySimulacionId(Long simulacionId);
}
