package com.helpdeskpro.helpdeskpro_backend.repository;


import com.helpdeskpro.helpdeskpro_backend.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoRepository extends JpaRepository<Evento, Long> {
}