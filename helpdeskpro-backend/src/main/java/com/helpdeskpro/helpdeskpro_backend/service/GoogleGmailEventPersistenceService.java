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
    private final EventoService eventoService;


    public GoogleGmailEventPersistenceService(
            EventoRepository eventoRepository,
            EventoService eventoService) {

        this.eventoRepository = eventoRepository;
        this.eventoService = eventoService;
    }


    // =====================================================
    // PERSISTIR MENSAJE DE GMAIL
    // =====================================================

    @Transactional
    public Evento persist(
            GoogleGmailMessageParser.ParsedGmailMessage message) {

        if (
            message == null ||
            isBlank(message.messageId())
        ) {

            throw new IllegalArgumentException(
                "El mensaje Gmail no tiene messageId."
            );
        }


        // =================================================
        // EVITAR DUPLICADOS
        // =================================================

        var eventoExistente =
            eventoRepository.findByIdGmailMessage(
                message.messageId()
            );

        if (eventoExistente.isPresent()) {

            return eventoExistente.get();

        }


        // =================================================
        // CONVERTIR GMAIL → EVENTO
        // =================================================

        Evento evento =
            toEvento(message);


        // =================================================
        // GUARDAR MEDIANTE EVENTOSERVICE
        // =================================================

        /*
         * No utilizamos directamente:
         *
         * eventoRepository.save(evento)
         *
         * porque queremos que el evento pase por:
         *
         * EventoService
         *       ↓
         * EvaluadorRiesgoService
         *       ↓
         * riskScore
         *       ↓
         * código INC-XXX
         */

        return eventoService.guardarEvento(
            evento
        );
    }


    // =====================================================
    // CONVERTIR MENSAJE GMAIL A EVENTO
    // =====================================================

    private Evento toEvento(
            GoogleGmailMessageParser.ParsedGmailMessage message) {

        Evento evento =
            new Evento();


        // =================================================
        // IDENTIFICADORES GMAIL
        // =================================================

        evento.setIdGmailMessage(
            message.messageId()
        );

        evento.setIdGmailThread(
            blankToNull(
                message.threadId()
            )
        );


        // =================================================
        // INFORMACIÓN DEL REMITENTE
        // =================================================

        evento.setNombreRemitente(
            blankToNull(
                message.senderName()
            )
        );

        evento.setCorreoRemitente(
            blankToNull(
                message.senderAddress()
            )
        );

        evento.setDominioRemitente(
            blankToNull(
                message.senderDomain()
            )
        );


        // =================================================
        // INFORMACIÓN DEL CORREO
        // =================================================

        evento.setAsunto(
            blankToNull(
                message.subject()
            )
        );

        evento.setDestinatario(
            blankToNull(
                message.to()
            )
        );

        evento.setFechaHora(
            parseDate(
                message.date()
            )
        );

        evento.setContenidoTexto(
            blankToNull(
                message.textBody()
            )
        );

        evento.setContenidoHTML(
            blankToNull(
                message.htmlBody()
            )
        );


        // =================================================
        // ESTADO DE CONEXIÓN
        // =================================================

        evento.setEstadoConexion(
            EstadoConexion.CONECTADO
        );


        // =================================================
        // ENLACES
        // =================================================

        if (message.links() != null) {

            message.links().forEach(
                url -> evento.getEnlaces().add(

                    Enlace.builder()
                        .evento(evento)
                        .dominio(
                            extractDomain(url)
                        )
                        .url(url)
                        .build()

                )
            );

        }


        // =================================================
        // ARCHIVOS ADJUNTOS
        // =================================================

        if (message.attachments() != null) {

            message.attachments().forEach(
                attachment ->
                    evento.getArchivos().add(

                        Archivo.builder()
                            .evento(evento)
                            .nombre(
                                attachment.name()
                            )
                            .tipo(
                                attachment.mimeType()
                            )
                            .extension(
                                attachment.extension()
                            )
                            .build()

                    )
            );

        }


        return evento;
    }


    // =====================================================
    // CONVERTIR FECHA DE GMAIL
    // =====================================================

    private LocalDateTime parseDate(
            String value) {

        if (isBlank(value)) {
            return null;
        }


        try {

            return ZonedDateTime
                .parse(
                    value,
                    DateTimeFormatter.RFC_1123_DATE_TIME
                )
                .toLocalDateTime();

        } catch (
            DateTimeParseException ignored
        ) {

            try {

                return OffsetDateTime
                    .parse(value)
                    .toLocalDateTime();

            } catch (
                DateTimeParseException exception
            ) {

                return null;

            }
        }
    }


    // =====================================================
    // EXTRAER DOMINIO DEL ENLACE
    // =====================================================

    private String extractDomain(
            String url) {

        try {

            return java.net.URI
                .create(url)
                .getHost();

        } catch (
            IllegalArgumentException exception
        ) {

            return null;

        }
    }


    // =====================================================
    // UTILIDADES
    // =====================================================

    private String blankToNull(
            String value) {

        return isBlank(value)
            ? null
            : value;
    }


    private boolean isBlank(
            String value) {

        return value == null ||
               value.isBlank();
    }
}