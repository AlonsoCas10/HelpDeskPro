package com.helpdeskpro.helpdeskpro_backend.repository;

import com.helpdeskpro.helpdeskpro_backend.model.UsuarioCritico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioCriticoRepository
        extends JpaRepository<UsuarioCritico, Long> {

}