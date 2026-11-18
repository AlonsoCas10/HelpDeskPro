package com.helpdeskpro.helpdeskpro_backend.repository;

import com.helpdeskpro.helpdeskpro_backend.model.Moneda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MonedaRepository extends JpaRepository<Moneda, Long> {
    Optional<Moneda> findByNombre(String nombre);
}
