package com.helpdeskpro.helpdeskpro_backend.service;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.ProjectSubscriptionName;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class GoogleGmailPubSubConsumer {
    private static final Logger logger = LoggerFactory.getLogger(GoogleGmailPubSubConsumer.class);

    private final GoogleGmailService gmailService;
    private final GoogleGmailEventPersistenceService eventPersistenceService;
    private volatile boolean enabled;
    private final String subscriptionName;
    private Subscriber subscriber;

    public GoogleGmailPubSubConsumer(
            GoogleGmailService gmailService,
            GoogleGmailEventPersistenceService eventPersistenceService,
            @Value("${google.gmail.pubsub.enabled:false}") boolean enabled,
            @Value("${google.gmail.pubsub.subscription:}") String subscriptionName) {
        this.gmailService = gmailService;
        this.eventPersistenceService = eventPersistenceService;
        this.enabled = enabled;
        this.subscriptionName = subscriptionName;
    }

    @PostConstruct
    public void start() {
        if (!enabled) {
            logger.info("Consumidor pull de Gmail deshabilitado.");
            return;
        }

        try {
            startSubscriber();
        } catch (Exception exception) {
            enabled = false;
            logger.error("No se pudo iniciar el consumidor pull de Gmail.", exception);
            stopSubscriber();
        }
    }

    public synchronized void setEnabled(boolean enabled) throws IOException {
        if (this.enabled == enabled && (!enabled || isRunning())) {
            return;
        }

        this.enabled = enabled;
        if (!enabled) {
            stopSubscriber();
            logger.info("Consumidor pull de Gmail deshabilitado por configuración.");
            return;
        }

        try {
            startSubscriber();
        } catch (Exception exception) {
            this.enabled = false;
            stopSubscriber();
            throw new IOException("No se pudo activar la recepción de Gmail.", exception);
        }
    }

    public boolean isEnabled() {
        return enabled && isRunning();
    }

    private synchronized void startSubscriber() throws IOException {
        if (subscriptionName == null || subscriptionName.isBlank()) {
            throw new IOException("Falta GOOGLE_GMAIL_PUBSUB_SUBSCRIPTION.");
        }

        if (isRunning()) {
            return;
        }

        gmailService.startWatch();
        MessageReceiver receiver = this::receive;
        subscriber = Subscriber.newBuilder(ProjectSubscriptionName.parse(subscriptionName), receiver).build();
        subscriber.startAsync().awaitRunning();
        logger.info("Consumidor pull de Gmail iniciado para {}.", subscriptionName);
    }

    @Scheduled(fixedDelayString = "${google.gmail.watch.renewal-ms:432000000}")
    public void renewWatch() {
        if (!isEnabled()) {
            return;
        }
        try {
            gmailService.startWatch();
            logger.info("Watch de Gmail renovado.");
        } catch (IOException exception) {
            logger.error("No se pudo renovar el watch de Gmail.", exception);
        }
    }

    @PreDestroy
    public void stop() {
        enabled = false;
        stopSubscriber();
    }

    private boolean isRunning() {
        return subscriber != null && subscriber.isRunning();
    }

    private void receive(PubsubMessage message, AckReplyConsumer consumer) {
        try {
            String payload = message.getData().toStringUtf8();
            GmailNotification notification = parseNotification(payload);
            if (notification.getEmailAddress() == null
                    || notification.getEmailAddress().isBlank()
                    || notification.getHistoryId() == null
                    || notification.getHistoryId().isBlank()) {
                logger.warn("Notificación de Gmail incompleta. Payload recibido: {}", payload);
                consumer.ack();
                return;
            }
            if (!gmailService.acceptsEmail(notification.getEmailAddress())) {
                logger.warn("Notificación de Gmail ignorada para una cuenta no configurada: {}.", notification.getEmailAddress());
                consumer.ack();
                return;
            }

            BigInteger historyId;
            try {
                historyId = new BigInteger(notification.getHistoryId());
            } catch (NumberFormatException exception) {
                logger.warn("Notificación de Gmail con historyId inválido. Payload recibido: {}", payload);
                consumer.ack();
                return;
            }

            List<GoogleGmailMessageParser.ParsedGmailMessage> messages = gmailService
                    .readMessagesFromNotification(notification.getEmailAddress(), historyId);
            messages.forEach(parsedMessage -> {
                eventPersistenceService.persist(parsedMessage);
                logMessage(parsedMessage);
            });
            consumer.ack();
        } catch (IOException exception) {
            logger.error("No se pudo leer el correo notificado por Gmail; se solicitará reintento.", exception);
            consumer.nack();
        } catch (Exception exception) {
            logger.error("Error inesperado procesando una notificación de Gmail; se solicitará reintento.", exception);
            consumer.nack();
        }
    }

    private GmailNotification parseNotification(String payload) throws IOException {
        try {
            JsonObject json = JsonParser.parseString(payload).getAsJsonObject();
            GmailNotification notification = new GmailNotification();
            notification.emailAddress = valueAsString(json, "emailAddress");
            notification.historyId = valueAsString(json, "historyId");
            return notification;
        } catch (RuntimeException exception) {
            throw new IOException("El payload de Gmail no tiene un formato JSON válido.", exception);
        }
    }

    private String valueAsString(JsonObject json, String propertyName) {
        JsonElement value = json.get(propertyName);
        return value == null || value.isJsonNull() ? null : value.getAsString();
    }

    private void logMessage(GoogleGmailMessageParser.ParsedGmailMessage message) {
        logger.info(
                "Correo Gmail recibido: messageId={}, threadId={}, from={}, senderDomain={}, to={}, subject={}, date={}, labels={}, links={}, attachments={}, textBody={}, htmlBody={}",
                message.messageId(),
                message.threadId(),
                message.from(),
                message.senderDomain(),
                message.to(),
                message.subject(),
                message.date(),
                message.labelIds(),
                message.links(),
                message.attachments(),
                message.textBody(),
                message.htmlBody());
    }

    private void stopSubscriber() {
        if (subscriber == null) {
            return;
        }
        try {
            subscriber.stopAsync().awaitTerminated(10, TimeUnit.SECONDS);
        } catch (Exception exception) {
            logger.warn("El consumidor pull de Gmail no se detuvo limpiamente.", exception);
        } finally {
            subscriber = null;
        }
    }

    public static class GmailNotification {
        public String emailAddress;
        public String historyId;

        public GmailNotification() {
        }

        public String getEmailAddress() {
            return emailAddress;
        }

        public String getHistoryId() {
            return historyId;
        }
    }
}