package com.helpdeskpro.helpdeskpro_backend.controller;

import com.helpdeskpro.helpdeskpro_backend.model.DeudorSimulado;
import com.helpdeskpro.helpdeskpro_backend.service.DeudorSimuladoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deudores")
@CrossOrigin(origins = "http://localhost:5173")
public class DeudorSimuladoController {

    private final DeudorSimuladoService service;

    public DeudorSimuladoController(DeudorSimuladoService service) {
        this.service = service;
    }

    @GetMapping
    public List<DeudorSimulado> listar() {
        return service.listar();
    }

    @PostMapping
    public DeudorSimulado guardar(@RequestBody DeudorSimulado deudor) {
        return service.guardar(deudor);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}