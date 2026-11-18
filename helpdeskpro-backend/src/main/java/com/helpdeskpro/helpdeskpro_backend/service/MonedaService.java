package com.helpdeskpro.helpdeskpro_backend.service;

import com.helpdeskpro.helpdeskpro_backend.model.Moneda;
import com.helpdeskpro.helpdeskpro_backend.repository.MonedaRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MonedaService {

    private final MonedaRepository repository;

    public MonedaService(MonedaRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void inicializarMonedas() {
        crearSiNoExiste("PEN");
        crearSiNoExiste("USD");
    }

    private void crearSiNoExiste(String nombre) {
        if (repository.findByNombre(nombre).isEmpty()) {
            repository.save(Moneda.builder().nombre(nombre).build());
        }
    }

    public List<Moneda> listar() {
        return repository.findAll();
    }

    public Moneda buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La moneda seleccionada no existe."));
    }
}
