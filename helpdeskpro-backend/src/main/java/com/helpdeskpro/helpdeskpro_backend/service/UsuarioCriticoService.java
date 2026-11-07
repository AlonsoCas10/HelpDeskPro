package com.helpdeskpro.helpdeskpro_backend.service;

import com.helpdeskpro.helpdeskpro_backend.model.UsuarioCritico;
import com.helpdeskpro.helpdeskpro_backend.repository.UsuarioCriticoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioCriticoService {

    private final UsuarioCriticoRepository usuarioCriticoRepository;

    public UsuarioCriticoService(
            UsuarioCriticoRepository usuarioCriticoRepository) {

        this.usuarioCriticoRepository =
                usuarioCriticoRepository;
    }

    // ==========================================
    // OBTENER TODOS LOS USUARIOS CRÍTICOS
    // ==========================================

    public List<UsuarioCritico> obtenerTodos() {

        return usuarioCriticoRepository.findAll();
    }

    // ==========================================
    // GUARDAR UN USUARIO CRÍTICO
    // ==========================================

    public UsuarioCritico guardar(
            UsuarioCritico usuarioCritico) {

        return usuarioCriticoRepository.save(
                usuarioCritico
        );
    }

    // ==========================================
    // ELIMINAR UN USUARIO CRÍTICO
    // ==========================================

    public void eliminar(Long id) {

        usuarioCriticoRepository.deleteById(id);
    }
}