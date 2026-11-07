package com.helpdeskpro.helpdeskpro_backend.service;

import com.helpdeskpro.helpdeskpro_backend.model.Deudor;
import com.helpdeskpro.helpdeskpro_backend.repository.DeudorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeudorService {

    private final DeudorRepository repository;

    public DeudorService(DeudorRepository repository) {
        this.repository = repository;
    }

    public List<Deudor> listar() {
        return repository.findAll();
    }

    public Deudor guardar(Deudor deudor) {
        return repository.save(deudor);
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}