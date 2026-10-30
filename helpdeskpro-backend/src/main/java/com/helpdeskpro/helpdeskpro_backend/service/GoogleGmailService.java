package com.helpdeskpro.helpdeskpro_backend.service;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.History;
import com.google.api.services.gmail.model.HistoryMessageAdded;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.Profile;
import com.google.api.services.gmail.model.WatchRequest;
import com.google.api.services.gmail.model.WatchResponse;

@Service
public class GoogleGmailService {
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final String clientId;
    private final String clientSecret;
    private final String refreshToken;
    private final String applicationName;
    private final String pubsubTopic;
    private final String expectedEmail;
    private final Gmail configuredGmailClient;
    private final AtomicReference<BigInteger> historyId = new AtomicReference<>();
    private final GoogleGmailMessageParser messageParser = new GoogleGmailMessageParser();

    @Autowired
    public GoogleGmailService(
            @Value("${google.gmail.client-id}") String clientId,
            @Value("${google.gmail.client-secret}") String clientSecret,
            @Value("${google.gmail.refresh-token}") String refreshToken,
            @Value("${google.gmail.application-name}") String applicationName,
            @Value("${google.gmail.pubsub.topic:}") String pubsubTopic,
            @Value("${google.gmail.expected-email:}") String expectedEmail) {
        this(clientId, clientSecret, refreshToken, applicationName, pubsubTopic, expectedEmail, null);
    }

    GoogleGmailService(Gmail gmailClient) {
        this("test-client-id", "test-client-secret", "test-refresh-token", "test-application", "", "", gmailClient);
    }

    private GoogleGmailService(
            String clientId,
            String clientSecret,
            String refreshToken,
            String applicationName,
            String pubsubTopic,
            String expectedEmail,
            Gmail configuredGmailClient) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
        this.applicationName = applicationName;
        this.pubsubTopic = pubsubTopic;
        this.expectedEmail = expectedEmail;
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

    public synchronized void startWatch() throws IOException {
        if (isBlank(pubsubTopic)) {
            throw new GoogleGmailConfigurationException(
                    "Falta GOOGLE_GMAIL_PUBSUB_TOPIC con el nombre completo del tópico de Pub/Sub.");
        }

        Gmail gmail = configuredGmailClient != null ? configuredGmailClient : createGmailClient();
        WatchRequest request = new WatchRequest()
            .setTopicName(pubsubTopic)
                .setLabelIds(List.of("INBOX"))
                .setLabelFilterBehavior("INCLUDE");
        WatchResponse response = gmail.users().watch("me", request).execute();
        historyId.compareAndSet(null, response.getHistoryId());
    }

    public synchronized List<GoogleGmailMessageParser.ParsedGmailMessage> readMessagesFromNotification(
            String emailAddress,
            BigInteger notificationHistoryId) throws IOException {
        if (!isBlank(expectedEmail) && !expectedEmail.equalsIgnoreCase(emailAddress)) {
            return List.of();
        }

        BigInteger currentHistoryId = historyId.get();
        if (currentHistoryId == null || notificationHistoryId.compareTo(currentHistoryId) <= 0) {
            return List.of();
        }

        Gmail gmail = configuredGmailClient != null ? configuredGmailClient : createGmailClient();
        List<GoogleGmailMessageParser.ParsedGmailMessage> messages = new ArrayList<>();
        String pageToken = null;

        do {
            Gmail.Users.History.List request = gmail.users().history()
                    .list("me")
                    .setStartHistoryId(currentHistoryId)
                    .setHistoryTypes(List.of("messageAdded"))
                    .setPageToken(pageToken);
            com.google.api.services.gmail.model.ListHistoryResponse response = request.execute();

            if (response.getHistory() != null) {
                for (History history : response.getHistory()) {
                    if (history.getMessagesAdded() == null) {
                        continue;
                    }
                    for (HistoryMessageAdded messageAdded : history.getMessagesAdded()) {
                        Message message = messageAdded.getMessage();
                        if (message != null && message.getId() != null) {
                            Message fullMessage = gmail.users().messages()
                                    .get("me", message.getId())
                                    .setFormat("full")
                                    .execute();
                            messages.add(messageParser.parse(fullMessage));
                        }
                    }
                }
            }
            pageToken = response.getNextPageToken();
        } while (pageToken != null);

        historyId.set(notificationHistoryId);
        return messages;
    }

    public boolean acceptsEmail(String emailAddress) {
        return isBlank(expectedEmail) || expectedEmail.equalsIgnoreCase(emailAddress);
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
