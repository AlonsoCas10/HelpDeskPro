package com.helpdeskpro.helpdeskpro_backend.controller;

import com.helpdeskpro.helpdeskpro_backend.model.Moneda;
import com.helpdeskpro.helpdeskpro_backend.service.MonedaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/monedas")
public class MonedaController {

    private final MonedaService service;

    public MonedaController(MonedaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Moneda> listar() {
        return service.listar();
    }
}
