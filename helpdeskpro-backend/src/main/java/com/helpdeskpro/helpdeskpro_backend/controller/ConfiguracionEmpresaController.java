package com.helpdeskpro.helpdeskpro_backend.controller;


import com.helpdeskpro.helpdeskpro_backend.model.ConfiguracionEmpresa;
import com.helpdeskpro.helpdeskpro_backend.service.ConfiguracionEmpresaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/configuracion")
@CrossOrigin(origins = "http://localhost:5173")
public class ConfiguracionEmpresaController {

    private final ConfiguracionEmpresaService service;

    public ConfiguracionEmpresaController(ConfiguracionEmpresaService service) {
        this.service = service;
    }

    @PostMapping
    public ConfiguracionEmpresa guardar(@RequestBody ConfiguracionEmpresa configuracion) {
        return service.guardar(configuracion);
    }
}