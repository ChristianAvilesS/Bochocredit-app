package com.bochocredit.repository;

import com.bochocredit.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findBySimulacionIdOrderByNumCuota(Long simulacionId);
    void deleteBySimulacionId(Long simulacionId);
}
