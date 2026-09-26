package com.helpdeskpro.helpdeskpro_backend.service;


import com.helpdeskpro.helpdeskpro_backend.model.DeudorSimulado;
import com.helpdeskpro.helpdeskpro_backend.repository.DeudorSimuladoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeudorSimuladoService {

    private final DeudorSimuladoRepository repository;

    public DeudorSimuladoService(DeudorSimuladoRepository repository) {
        this.repository = repository;
    }

    public List<DeudorSimulado> listar() {
        return repository.findAll();
    }

    public DeudorSimulado guardar(DeudorSimulado deudor) {
        return repository.save(deudor);
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}