package com.helpdeskpro.helpdeskpro_backend.service;

import com.helpdeskpro.helpdeskpro_backend.model.FacturaSimulada;
import com.helpdeskpro.helpdeskpro_backend.repository.FacturaSimuladaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FacturaSimuladaService {

    private final FacturaSimuladaRepository repository;

    public FacturaSimuladaService(FacturaSimuladaRepository repository) {
        this.repository = repository;
    }

    public List<FacturaSimulada> listar() {
        return repository.findAll();
    }

    public FacturaSimulada guardar(FacturaSimulada factura) {
        return repository.save(factura);
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
