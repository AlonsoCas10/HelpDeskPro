package com.helpdeskpro.helpdeskpro_backend.repository;

import com.helpdeskpro.helpdeskpro_backend.model.DeudorSimulado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeudorSimuladoRepository
        extends JpaRepository<DeudorSimulado, Long> {
}