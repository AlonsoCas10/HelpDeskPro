
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

    // =====================================================
    // GUARDAR EVENTO
    // =====================================================

    public Evento guardarEvento(Evento evento) {

        if (evento.getEnlaces() != null) {
            evento.getEnlaces().forEach(enlace -> enlace.setEvento(evento));
        }

        if (evento.getArchivos() != null) {
            evento.getArchivos().forEach(archivo -> archivo.setEvento(evento));
        }

        // =================================================
        // EVALUAR LOS CINCO CRITERIOS DE RIESGO
        // =================================================

        int riesgoTotal =
                evaluadorRiesgoService.evaluarRiesgo(evento);

        evento.setRiskScore(riesgoTotal);

        // =================================================
        // PRIMER GUARDADO
        // =================================================

        Evento eventoGuardado = eventoRepository.save(evento);

        // =================================================
        // GENERAR CÓDIGO DEL INCIDENTE
        // =================================================

        if (eventoGuardado.getCodigo() == null
                || eventoGuardado.getCodigo().isBlank()) {

            String codigo = String.format(
                    "INC-%03d",
                    eventoGuardado.getId()
            );

            eventoGuardado.setCodigo(codigo);

            eventoGuardado = eventoRepository.save(eventoGuardado);
        }

        return eventoGuardado;
    }

    // =====================================================
    // LISTAR EVENTOS
    // =====================================================

    public List<Evento> listarEventos() {
        return eventoRepository.findAll();
    }
}
