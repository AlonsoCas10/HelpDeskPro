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

    @Autowired
    private EvaluadorRiesgoService evaluadorRiesgoService;
    
public Evento guardarEvento(Evento evento) {

    // Calculamos automáticamente el Risk Score
    int puntosRemitente = evaluadorRiesgoService.evaluarRemitente(
            evento.getRemitente(),
            evento.getDominioRegistrado(),
            evento.getRemitenteConocido()

        );

    // Guardamos el resultado en el evento
    evento.setRiskScore(puntosRemitente);

    // Guardamos el evento
    Evento eventoGuardado = eventoRepository.save(evento);

    // Obtenemos el ID generado por SQL Server
    Long id = eventoGuardado.getId();

    // Creamos la identificación
    String codigo = String.format("INC-%03d", id);

    // Asignamos el código
    eventoGuardado.setCodigo(codigo);

    // Guardamos nuevamente el evento
    return eventoRepository.save(eventoGuardado);
}


    public List<Evento> listarEventos() {

        return eventoRepository.findAll();

    }
}