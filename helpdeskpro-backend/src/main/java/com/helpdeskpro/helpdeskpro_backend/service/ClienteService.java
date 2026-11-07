package com.helpdeskpro.helpdeskpro_backend.service;

import com.helpdeskpro.helpdeskpro_backend.model.Cliente;
import com.helpdeskpro.helpdeskpro_backend.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    public List<Cliente> listar() {
        return repository.findAll();
    }

    public Cliente guardar(Cliente cliente) {
        return repository.save(cliente);
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }

    public Cliente marcarComoVip(String ruc) {

        Cliente cliente = repository.findByRuc(ruc)
                .orElseThrow(() ->
                        new RuntimeException("Cliente no encontrado con RUC: " + ruc)
                );

        cliente.setVip(true);

        return repository.save(cliente);
    }
}