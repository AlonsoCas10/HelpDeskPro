package com.helpdeskpro.helpdeskpro_backend.service;

import com.helpdeskpro.helpdeskpro_backend.model.Evento;
import com.helpdeskpro.helpdeskpro_backend.repository.EventoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventoService {

    @Autowired
    private EventoRepository eventoRepository;
    public Evento guardarEvento(Evento evento) {
        return eventoRepository.save(evento);
    }

    public List<Evento> listarEventos() {

        return eventoRepository.findAll();

    }
}