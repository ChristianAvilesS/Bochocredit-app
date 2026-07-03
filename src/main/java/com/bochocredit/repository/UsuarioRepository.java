package com.bochocredit.repository;

import com.bochocredit.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    boolean existsByUsername(String username);

    @Query("SELECT u FROM Usuario u " +
            "WHERE u.activo = true")
    List<Usuario> buscarTodosActivos();


    @Query("SELECT u FROM Usuario u " +
            "WHERE u.id = :id AND " +
            "u.activo = true AND " +
            "u.fechaModificacion IS NULL")
    Usuario renovarPassword(@Param("id") Long id);
}
