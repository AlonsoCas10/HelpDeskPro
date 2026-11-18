package com.helpdeskpro.helpdeskpro_backend.service;

import com.helpdeskpro.helpdeskpro_backend.dto.ImpactoFinancieroResponse;
import com.helpdeskpro.helpdeskpro_backend.model.Incidencia;
import com.helpdeskpro.helpdeskpro_backend.repository.IncidenciaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ImpactoFinancieroConsultaService {

    private final IncidenciaRepository incidenciaRepository;

    public ImpactoFinancieroConsultaService(
            IncidenciaRepository incidenciaRepository) {
        this.incidenciaRepository = incidenciaRepository;
    }

    @Transactional(readOnly = true)
    public ImpactoFinancieroResponse obtenerPorEvento(Long eventoId) {
        Incidencia incidencia = incidenciaRepository
                .findByEventoId(eventoId)
                .orElseThrow(() -> new IncidenciaNoEncontradaException(
                        "No existe una incidencia de impacto financiero para el evento "
                                + eventoId + "."
                ));

        String mensajePotencial = incidencia.getExplicacionCalculo() == null
                || incidencia.getExplicacionCalculo().isBlank()
                ? "Impacto financiero no estimable por falta de información suficiente."
                : "Impacto financiero estimado; no representa una pérdida confirmada.";

        return new ImpactoFinancieroResponse(
                incidencia.getId(),
                incidencia.getEvento().getId(),
                incidencia.getEvento().getCodigo(),
                incidencia.getCliente().getId(),
                incidencia.getCliente().getRuc(),
                incidencia.getCliente().getRazonSocial(),
                incidencia.getMoneda().getNombre(),
                incidencia.getCategoria() == null
                        ? null
                        : normalizarCategoria(incidencia.getCategoria().name()),
                incidencia.getConfiabilidadExposicion() == null
                        ? null
                        : incidencia.getConfiabilidadExposicion().name(),
                incidencia.getPerdidaPotencialDirecta(),
                incidencia.getVolumenPotencialPorInterrupcion(),
                incidencia.getPerdidaPotencialPorInterrupcion(),
                incidencia.getMontoSemanal(),
                incidencia.getMontoDiario(),
                incidencia.getDiasOperativos(),
                incidencia.getTiempoInterrupcion(),
                incidencia.getComision(),
                incidencia.getFuentesCalculo(),
                incidencia.getExplicacionCalculo(),
                incidencia.getFechaCalculo(),
                mensajePotencial
        );
    }

    private String normalizarCategoria(String categoria) {
        return switch (categoria) {
            case "MEDIO" -> "Medio";
            case "ALTO" -> "Alto";
            case "CRITICO" -> "Crítico";
            default -> "Bajo";
        };
    }

    public static class IncidenciaNoEncontradaException
            extends RuntimeException {

        public IncidenciaNoEncontradaException(String mensaje) {
            super(mensaje);
        }
    }
}
