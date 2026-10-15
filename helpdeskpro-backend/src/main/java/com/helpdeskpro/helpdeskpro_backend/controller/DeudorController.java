package com.helpdeskpro.helpdeskpro_backend.controller;

import com.helpdeskpro.helpdeskpro_backend.model.Deudor;
import com.helpdeskpro.helpdeskpro_backend.service.DeudorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deudores")
@CrossOrigin(origins = "http://localhost:5173")
public class DeudorController {

    private final DeudorService service;

    public DeudorController(DeudorService service) {
        this.service = service;
    }

    @GetMapping
    public List<Deudor> listar() {
        return service.listar();
    }

    @PostMapping
    public Deudor guardar(@RequestBody Deudor deudor) {
        return service.guardar(deudor);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}