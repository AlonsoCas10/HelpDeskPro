package com.helpdeskpro.helpdeskpro_backend.service;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Profile;

@Service
public class GoogleGmailService {
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final String clientId;
    private final String clientSecret;
    private final String refreshToken;
    private final String applicationName;
    private final Gmail configuredGmailClient;

    @Autowired
    public GoogleGmailService(
            @Value("${google.gmail.client-id}") String clientId,
            @Value("${google.gmail.client-secret}") String clientSecret,
            @Value("${google.gmail.refresh-token}") String refreshToken,
            @Value("${google.gmail.application-name}") String applicationName) {
        this(clientId, clientSecret, refreshToken, applicationName, null);
    }

    GoogleGmailService(Gmail gmailClient) {
        this("test-client-id", "test-client-secret", "test-refresh-token", "test-application", gmailClient);
    }

    private GoogleGmailService(
            String clientId,
            String clientSecret,
            String refreshToken,
            String applicationName,
            Gmail configuredGmailClient) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
        this.applicationName = applicationName;
        this.configuredGmailClient = configuredGmailClient;
    }

    public GmailConnectionResult testConnection() throws IOException {
        Gmail gmail = configuredGmailClient != null ? configuredGmailClient : createGmailClient();
        Profile profile = gmail.users().getProfile("me").execute();

        return new GmailConnectionResult(
                "connected",
                profile.getEmailAddress(),
                profile.getMessagesTotal(),
                profile.getThreadsTotal());
    }

    private Gmail createGmailClient() throws IOException {
        validateConfiguration();

        try {
            HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
            GoogleCredential credential = new GoogleCredential.Builder()
                    .setTransport(transport)
                    .setJsonFactory(JSON_FACTORY)
                    .setClientSecrets(clientId, clientSecret)
                    .build()
                    .setRefreshToken(refreshToken);

            return new Gmail.Builder(transport, JSON_FACTORY, credential)
                    .setApplicationName(applicationName)
                    .build();
        } catch (GeneralSecurityException exception) {
            throw new IOException("No se pudo preparar la conexión segura con Google.", exception);
        }
    }

    private void validateConfiguration() {
        if (isBlank(clientId) || isBlank(clientSecret) || isBlank(refreshToken)) {
            throw new GoogleGmailConfigurationException(
                    "Faltan GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET o GOOGLE_REFRESH_TOKEN en el archivo .env.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record GmailConnectionResult(
            String status,
            String emailAddress,
            Integer messagesTotal,
            Integer threadsTotal) {
    }

    public static class GoogleGmailConfigurationException extends RuntimeException {
        public GoogleGmailConfigurationException(String message) {
            super(message);
        }
    }
}
