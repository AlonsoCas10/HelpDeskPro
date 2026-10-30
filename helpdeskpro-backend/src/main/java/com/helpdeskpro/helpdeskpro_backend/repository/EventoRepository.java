package com.helpdeskpro.helpdeskpro_backend.repository;


import com.helpdeskpro.helpdeskpro_backend.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventoRepository extends JpaRepository<Evento, String> {
    Optional<Evento> findByIdGmailMessage(String idGmailMessage);
}