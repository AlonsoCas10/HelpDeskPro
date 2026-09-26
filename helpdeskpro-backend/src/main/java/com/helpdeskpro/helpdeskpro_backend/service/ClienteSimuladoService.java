package com.helpdeskpro.helpdeskpro_backend.service;



import com.helpdeskpro.helpdeskpro_backend.model.ClienteSimulado;
import com.helpdeskpro.helpdeskpro_backend.repository.ClienteSimuladoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteSimuladoService {

    private final ClienteSimuladoRepository repository;

    public ClienteSimuladoService(ClienteSimuladoRepository repository) {
        this.repository = repository;
    }

    public List<ClienteSimulado> listar() {
        return repository.findAll();
    }

    public ClienteSimulado guardar(ClienteSimulado cliente) {
        return repository.save(cliente);
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }

    public ClienteSimulado marcarComoVip(String ruc) {

        ClienteSimulado cliente = repository.findByRuc(ruc)
                .orElseThrow(() ->
                        new RuntimeException("Cliente no encontrado con RUC: " + ruc)
                );

        cliente.setVip(true);

        return repository.save(cliente);
    }
}