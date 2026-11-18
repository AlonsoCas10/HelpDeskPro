package com.helpdeskpro.helpdeskpro_backend.repository;

import com.helpdeskpro.helpdeskpro_backend.model.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IncidenciaRepository extends JpaRepository<Incidencia, String> {
    Optional<Incidencia> findByEventoId(Long eventoId);
}
