package com.helpdeskpro.helpdeskpro_backend.controller;


import com.helpdeskpro.helpdeskpro_backend.model.ClienteSimulado;
import com.helpdeskpro.helpdeskpro_backend.service.ClienteSimuladoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
@CrossOrigin(origins = "http://localhost:5173")
public class ClienteSimuladoController {

    private final ClienteSimuladoService service;

    public ClienteSimuladoController(ClienteSimuladoService service) {
        this.service = service;
    }

    @GetMapping
    public List<ClienteSimulado> listar() {
        return service.listar();
    }

    @PostMapping
    public ClienteSimulado guardar(@RequestBody ClienteSimulado cliente) {
        return service.guardar(cliente);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }

    @PostMapping("/marcar-vip/{ruc}")
    public ClienteSimulado marcarComoVip(@PathVariable String ruc) {
        return service.marcarComoVip(ruc);
    }
}