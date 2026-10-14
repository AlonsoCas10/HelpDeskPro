package com.helpdeskpro.helpdeskpro_backend.controller;

import com.helpdeskpro.helpdeskpro_backend.model.UsuarioCritico;
import com.helpdeskpro.helpdeskpro_backend.service.UsuarioCriticoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios-criticos")
@CrossOrigin(origins = "http://localhost:5173")
public class UsuarioCriticoController {

    private final UsuarioCriticoService usuarioCriticoService;

    public UsuarioCriticoController(
            UsuarioCriticoService usuarioCriticoService) {

        this.usuarioCriticoService =
                usuarioCriticoService;
    }

    // ==========================================
    // OBTENER TODOS LOS USUARIOS CRÍTICOS
    // ==========================================

    @GetMapping
    public ResponseEntity<List<UsuarioCritico>> obtenerTodos() {

        return ResponseEntity.ok(
                usuarioCriticoService.obtenerTodos()
        );
    }

    // ==========================================
    // GUARDAR UN USUARIO CRÍTICO
    // ==========================================

    @PostMapping
    public ResponseEntity<UsuarioCritico> guardar(
            @RequestBody UsuarioCritico usuarioCritico) {

        UsuarioCritico usuarioGuardado =
                usuarioCriticoService.guardar(
                        usuarioCritico
                );

        return ResponseEntity.ok(
                usuarioGuardado
        );
    }

    // ==========================================
    // ELIMINAR UN USUARIO CRÍTICO
    // ==========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        usuarioCriticoService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}