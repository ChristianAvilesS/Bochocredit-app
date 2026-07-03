package com.bochocredit.repository;

import com.bochocredit.entity.TasaInteres;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TasaInteresRepository extends JpaRepository<TasaInteres, Long> {
}
