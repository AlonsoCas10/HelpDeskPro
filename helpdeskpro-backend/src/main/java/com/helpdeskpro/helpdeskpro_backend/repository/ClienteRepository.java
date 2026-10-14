package com.helpdeskpro.helpdeskpro_backend.repository;


import com.helpdeskpro.helpdeskpro_backend.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClienteRepository
        extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByRuc(String ruc);

}