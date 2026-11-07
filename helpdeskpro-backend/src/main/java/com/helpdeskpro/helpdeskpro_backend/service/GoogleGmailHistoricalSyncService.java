
package com.helpdeskpro.helpdeskpro_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;


@Service
@ConditionalOnProperty(name = "google.gmail.historical-sync.enabled", havingValue = "true")
public class GoogleGmailHistoricalSyncService {


    private static final Logger logger =
            LoggerFactory.getLogger(GoogleGmailHistoricalSyncService.class);

    private final GoogleGmailService gmailService;
    private final GoogleGmailEventPersistenceService persistenceService;

    public GoogleGmailHistoricalSyncService(
            GoogleGmailService gmailService,
            GoogleGmailEventPersistenceService persistenceService) {

        this.gmailService = gmailService;
        this.persistenceService = persistenceService;
    }

    // Sincronizar automáticamente al iniciar Spring Boot
    @EventListener(ApplicationReadyEvent.class)
    public void sincronizarCorreosAlIniciar() {

        logger.info("Iniciando sincronización histórica de Gmail...");

        try {

            // Consultar hasta 100 correos de INBOX de los últimos 30 días
            var mensajes = gmailService.readRecentInboxMessages(0);

            int procesados = 0;
            int errores = 0;

            for (var mensaje : mensajes) {

                try {

                    // El servicio de persistencia evita duplicados
                    persistenceService.persist(mensaje);
                    procesados++;

                } catch (Exception exception) {

                    errores++;

                    logger.error(
                        "Error al procesar un correo de Gmail.",
                        exception
                    );
                }
            }

            logger.info(
                "Sincronización Gmail finalizada. Consultados: {}, procesados: {}, errores: {}",
                mensajes.size(),
                procesados,
                errores
            );

        } catch (Exception exception) {

            // Un error de Gmail no debe impedir que inicie HelpDeskPro
            logger.error(
                "No se pudo completar la sincronización histórica de Gmail.",
                exception
            );
        }
    }
}
