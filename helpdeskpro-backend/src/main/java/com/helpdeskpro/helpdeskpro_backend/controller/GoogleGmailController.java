package com.helpdeskpro.helpdeskpro_backend.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.helpdeskpro.helpdeskpro_backend.service.GoogleGmailService;
import com.helpdeskpro.helpdeskpro_backend.service.GoogleGmailPubSubConsumer;
import com.helpdeskpro.helpdeskpro_backend.service.GoogleGmailService.GmailConnectionResult;
import com.helpdeskpro.helpdeskpro_backend.service.GoogleGmailService.GoogleGmailConfigurationException;

import com.helpdeskpro.helpdeskpro_backend.service.GoogleGmailEventPersistenceService;

@RestController
@RequestMapping("/integraciones/gmail")
public class GoogleGmailController {

    private final GoogleGmailService service;
    private final GoogleGmailPubSubConsumer consumer;
    private final GoogleGmailEventPersistenceService persistenceService;

    public GoogleGmailController(
        GoogleGmailService service,
        GoogleGmailPubSubConsumer consumer,
        GoogleGmailEventPersistenceService persistenceService) {

    this.service = service;
    this.consumer = consumer;
    this.persistenceService = persistenceService;
}

    @GetMapping("/connection-test")
    public ResponseEntity<?> testConnection() {
        try {
            GmailConnectionResult result = service.testConnection();
            return ResponseEntity.ok(result);
        } catch (GoogleGmailConfigurationException exception) {
            return error(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
        } catch (GoogleJsonResponseException exception) {
            return error(HttpStatus.BAD_GATEWAY,
                    "Google rechazó las credenciales o el acceso a Gmail API.");
        } catch (IOException exception) {
            return error(HttpStatus.BAD_GATEWAY,
                    "No se pudo conectar con Gmail API.");
        }
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
                "provider", "gmail",
                "enabled", consumer.isEnabled());
    }

    @PostMapping("/activation")
    public ResponseEntity<?> setActivation(
            @RequestParam boolean enabled) {
        try {
            consumer.setEnabled(enabled);
            return ResponseEntity.ok(status());
        } catch (IOException exception) {
            return error(HttpStatus.SERVICE_UNAVAILABLE,
                    exception.getMessage());
        }
    }

    // =====================================================
    // PRUEBA: CORREOS DE INBOX DE LOS ÚLTIMOS 30 DÍAS
    // NO GUARDA MENSAJES EN LA BASE DE DATOS
    // =====================================================

    @GetMapping("/inbox-preview")
    public ResponseEntity<?> previewRecentInbox() {
        try {
            var messages = service.readRecentInboxMessages(5);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "periodDays", 30,
                    "limit", 5,
                    "retrieved", messages.size(),
                    "savedToDatabase", false
            ));

        } catch (GoogleGmailConfigurationException exception) {
            return error(HttpStatus.SERVICE_UNAVAILABLE,
                    exception.getMessage());

        } catch (GoogleJsonResponseException exception) {
            return error(HttpStatus.BAD_GATEWAY,
                    "Google rechazó la consulta de correos.");

        } catch (IOException exception) {
            return error(HttpStatus.BAD_GATEWAY,
                    "No se pudieron consultar los correos de Gmail.");
        }
    }

// =====================================================
// IMPORTAR CORREOS RECIENTES DE GMAIL
// SOLO ADMINISTRADORES
// =====================================================

@PostMapping("/import-recent")
public ResponseEntity<?> importRecentInbox() {
    try {
        var messages = service.readRecentInboxMessages(100);

        int processed = 0;

        for (var message : messages) {
            persistenceService.persist(message);
            processed++;
        }

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "periodDays", 30,
                "retrieved", messages.size(),
                "processed", processed,
                "message", "Correos procesados correctamente."
        ));

    } catch (GoogleGmailConfigurationException exception) {
        return error(
                HttpStatus.SERVICE_UNAVAILABLE,
                exception.getMessage()
        );

    } catch (GoogleJsonResponseException exception) {
        return error(
                HttpStatus.BAD_GATEWAY,
                "Google rechazó la consulta de correos."
        );

    } catch (IOException exception) {
        return error(
                HttpStatus.BAD_GATEWAY,
                "No se pudieron consultar los correos de Gmail."
        );
    }
}

    private ResponseEntity<Map<String, String>> error(
            HttpStatus status,
            String message) {
        return ResponseEntity.status(status).body(
                Map.of(
                        "status", "error",
                        "message", message
                )
        );
    }
}
