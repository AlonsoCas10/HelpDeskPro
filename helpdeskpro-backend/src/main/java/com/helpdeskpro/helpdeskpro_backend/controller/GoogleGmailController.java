package com.helpdeskpro.helpdeskpro_backend.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.helpdeskpro.helpdeskpro_backend.service.GoogleGmailService;
import com.helpdeskpro.helpdeskpro_backend.service.GoogleGmailService.GmailConnectionResult;
import com.helpdeskpro.helpdeskpro_backend.service.GoogleGmailService.GoogleGmailConfigurationException;

@RestController
@RequestMapping("/integraciones/gmail")
public class GoogleGmailController {
    private final GoogleGmailService service;

    public GoogleGmailController(GoogleGmailService service) {
        this.service = service;
    }

    @GetMapping("/connection-test")
    public ResponseEntity<?> testConnection() {
        try {
            GmailConnectionResult result = service.testConnection();
            return ResponseEntity.ok(result);
        } catch (GoogleGmailConfigurationException exception) {
            return error(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
        } catch (GoogleJsonResponseException exception) {
            return error(HttpStatus.BAD_GATEWAY, "Google rechazó las credenciales o el acceso a Gmail API.");
        } catch (IOException exception) {
            return error(HttpStatus.BAD_GATEWAY, "No se pudo conectar con Gmail API.");
        }
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("status", "error", "message", message));
    }
}
