package com.helpdeskpro.helpdeskpro_backend.repository;

import com.helpdeskpro.helpdeskpro_backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);
}