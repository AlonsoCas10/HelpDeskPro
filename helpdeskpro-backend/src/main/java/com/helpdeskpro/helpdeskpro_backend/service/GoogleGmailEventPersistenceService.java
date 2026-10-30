package com.helpdeskpro.helpdeskpro_backend.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.helpdeskpro.helpdeskpro_backend.model.Archivo;
import com.helpdeskpro.helpdeskpro_backend.model.Enlace;
import com.helpdeskpro.helpdeskpro_backend.model.EstadoConexion;
import com.helpdeskpro.helpdeskpro_backend.model.Evento;
import com.helpdeskpro.helpdeskpro_backend.repository.EventoRepository;

@Service
public class GoogleGmailEventPersistenceService {
    private final EventoRepository eventoRepository;

    public GoogleGmailEventPersistenceService(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    @Transactional
    public Evento persist(GoogleGmailMessageParser.ParsedGmailMessage message) {
        if (message == null || isBlank(message.messageId())) {
            throw new IllegalArgumentException("El mensaje Gmail no tiene messageId.");
        }

        return eventoRepository.findByIdGmailMessage(message.messageId())
                .orElseGet(() -> eventoRepository.save(toEvento(message)));
    }

    private Evento toEvento(GoogleGmailMessageParser.ParsedGmailMessage message) {
        Evento evento = new Evento();
        evento.setIdGmailMessage(message.messageId());
        evento.setIdGmailThread(blankToNull(message.threadId()));
        evento.setNombreRemitente(blankToNull(message.senderName()));
        evento.setCorreoRemitente(message.senderAddress());
        evento.setDominioRemitente(message.senderDomain());
        evento.setAsunto(blankToNull(message.subject()));
        evento.setFechaHora(parseDate(message.date()));
        evento.setContenidoTexto(blankToNull(message.textBody()));
        evento.setContenidoHTML(blankToNull(message.htmlBody()));
        evento.setEstadoConexion(EstadoConexion.CONECTADO);

        message.links().forEach(url -> evento.getEnlaces().add(Enlace.builder()
                .evento(evento)
                .dominio(extractDomain(url))
                .url(url)
                .build()));

        message.attachments().forEach(attachment -> evento.getArchivos().add(Archivo.builder()
                .evento(evento)
                .nombre(attachment.name())
                .tipo(attachment.mimeType())
                .extension(attachment.extension())
                .build()));
        return evento;
    }

    private LocalDateTime parseDate(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            try {
                return OffsetDateTime.parse(value).toLocalDateTime();
            } catch (DateTimeParseException exception) {
                return null;
            }
        }
    }

    private String extractDomain(String url) {
        try {
            return java.net.URI.create(url).getHost();
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String blankToNull(String value) {
        return isBlank(value) ? null : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
