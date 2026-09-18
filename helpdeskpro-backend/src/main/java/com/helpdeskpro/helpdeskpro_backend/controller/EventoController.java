
package com.helpdeskpro.helpdeskpro_backend.controller;


import com.helpdeskpro.helpdeskpro_backend.model.Evento;
import com.helpdeskpro.helpdeskpro_backend.service.EventoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/eventos")
@CrossOrigin(origins = "http://localhost:5173")
public class EventoController {

    @Autowired
    private EventoService eventoService;


    @PostMapping
    public Evento crearEvento(@RequestBody Evento evento) {

        return eventoService.guardarEvento(evento);

    }


    @GetMapping
    public List<Evento> obtenerEventos() {

        return eventoService.listarEventos();

    }
}