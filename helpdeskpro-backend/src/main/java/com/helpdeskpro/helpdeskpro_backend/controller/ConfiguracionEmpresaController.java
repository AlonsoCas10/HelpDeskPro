package com.helpdeskpro.helpdeskpro_backend.controller;

import com.helpdeskpro.helpdeskpro_backend.model.ConfiguracionEmpresa;
import com.helpdeskpro.helpdeskpro_backend.service.ConfiguracionEmpresaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/configuracion")
public class ConfiguracionEmpresaController {

    private final ConfiguracionEmpresaService service;

    public ConfiguracionEmpresaController(ConfiguracionEmpresaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> guardar(@RequestBody ConfiguracionEmpresa configuracion) {
        try {
            return ResponseEntity.ok(service.guardar(configuracion));
        } catch (IllegalArgumentException error) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", error.getMessage(),
                    "error", "Bad Request",
                    "status", HttpStatus.BAD_REQUEST.value(),
                    "path", "/configuracion"
            ));
        }
    }
}