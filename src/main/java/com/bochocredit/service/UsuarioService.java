package com.bochocredit.service;

import com.bochocredit.entity.Rol;
import com.bochocredit.entity.Usuario;
import com.bochocredit.repository.RolRepository;
import com.bochocredit.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repos;
    private final RolRepository rolRepos;

    public List<Usuario> listarUsuarios() {
        return repos.buscarTodosActivos();
    }

    public Usuario guardarUsuario(Usuario usuario) {
        return repos.save(usuario);
    }

    private Usuario obtenerPorId(Long id) {
        return repos.findById(id).orElse(null);
    }

    public boolean cambiarPassword(Long id, String password) {
        var u = obtenerPorId(id);
        if (u != null) {
            u.setPassword(password);
            return true;
        }
        return false;
    }

    public Rol obtenerRol(Long id) {
        return rolRepos.findById(id).orElse(null);
    }

    public void eliminarUsuario(Long id) {
        var u = obtenerPorId(id);
        if (u != null) {
            if (u.getActivo()){
                u.setActivo(false);
            }
        }
    }

    public boolean necesitaRenovarPassword(Long id) {
        return repos.renovarPassword(id) != null;
    }
}
