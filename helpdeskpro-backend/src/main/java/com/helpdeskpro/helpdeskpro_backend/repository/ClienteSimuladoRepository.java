package com.helpdeskpro.helpdeskpro_backend.repository;


import com.helpdeskpro.helpdeskpro_backend.model.ClienteSimulado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClienteSimuladoRepository
        extends JpaRepository<ClienteSimulado, Long> {
    Optional<ClienteSimulado> findByRuc(String ruc);

}