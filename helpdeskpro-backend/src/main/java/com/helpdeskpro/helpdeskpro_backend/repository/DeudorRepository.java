package com.helpdeskpro.helpdeskpro_backend.repository;

import com.helpdeskpro.helpdeskpro_backend.model.Deudor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DeudorRepository
        extends JpaRepository<Deudor, Long> {
        Optional<Deudor> findByRuc(String ruc);

}