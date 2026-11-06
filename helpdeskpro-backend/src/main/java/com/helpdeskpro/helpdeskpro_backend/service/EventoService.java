
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

        // Si el remitente está vacío, utilizamos el correo
        // obtenido mediante la integración con Gmail.
        if ((evento.getRemitente() == null
                || evento.getRemitente().isBlank())
                && evento.getCorreoRemitente() != null) {

            evento.setRemitente(evento.getCorreoRemitente());
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
