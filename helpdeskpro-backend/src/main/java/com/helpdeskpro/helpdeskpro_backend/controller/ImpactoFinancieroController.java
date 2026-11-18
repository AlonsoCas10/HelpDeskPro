package com.helpdeskpro.helpdeskpro_backend.controller;

import com.helpdeskpro.helpdeskpro_backend.dto.ImpactoFinancieroResponse;
import com.helpdeskpro.helpdeskpro_backend.service.ImpactoFinancieroConsultaService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/impacto-financiero")
public class ImpactoFinancieroController {

    private final ImpactoFinancieroConsultaService consultaService;

    public ImpactoFinancieroController(
            ImpactoFinancieroConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @GetMapping("/eventos/{eventoId}")
    public ResponseEntity<?> obtenerPorEvento(
            @PathVariable Long eventoId) {

        if (eventoId == null || eventoId <= 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "El identificador del evento no es válido.",
                    "error", "Bad Request",
                    "status", HttpStatus.BAD_REQUEST.value(),
                    "path", "/impacto-financiero/eventos/" + eventoId
            ));
        }

        try {
            ImpactoFinancieroResponse respuesta = consultaService
                    .obtenerPorEvento(eventoId);

            return ResponseEntity.ok(respuesta);
        } catch (
                ImpactoFinancieroConsultaService.IncidenciaNoEncontradaException error
        ) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", error.getMessage(),
                    "error", "Not Found",
                    "status", HttpStatus.NOT_FOUND.value(),
                    "path", "/impacto-financiero/eventos/" + eventoId
            ));
        }
    }
}
