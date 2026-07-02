package com.bochocredit.repository;

import com.bochocredit.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    @Query("SELECT COUNT(c) FROM Cliente c")
    long countTotal();

    boolean existsByDni(String dni);
    boolean existsByDniAndIdNot(String dni, Long id);
}
