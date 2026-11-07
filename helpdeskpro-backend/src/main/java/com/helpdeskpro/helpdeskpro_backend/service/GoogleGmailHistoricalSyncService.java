
package com.helpdeskpro.helpdeskpro_backend.service;

import com.helpdeskpro.helpdeskpro_backend.repository.EventoRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "google.gmail.historical-sync.enabled", havingValue = "true")
public class GoogleGmailHistoricalSyncService {

    private static final Logger logger =
            LoggerFactory.getLogger(GoogleGmailHistoricalSyncService.class);

    // Tras la primera revisión basta con mirar los correos más recientes.
    private static final int LIMITE_REVISION = 20;

    private final GoogleGmailService gmailService;
    private final GoogleGmailEventPersistenceService persistenceService;
    private final EventoRepository eventoRepository;

    private boolean primeraRevision = true;

    public GoogleGmailHistoricalSyncService(
            GoogleGmailService gmailService,
            GoogleGmailEventPersistenceService persistenceService,
            EventoRepository eventoRepository) {

        this.gmailService = gmailService;
        this.persistenceService = persistenceService;
        this.eventoRepository = eventoRepository;
    }

    // Revisa INBOX al iniciar y luego de forma periódica. Recupera los correos
    // cuya notificación de Pub/Sub se perdió, por ejemplo por un corte de red.
    // El servicio de persistencia evita duplicados.
    @Scheduled(fixedDelayString = "${google.gmail.historical-sync.interval-ms:30000}")
    public void revisarCorreos() {

        try {

            long eventosAntes = eventoRepository.count();

            // 0 = hasta 100 correos de los últimos 30 días
            var mensajes = gmailService.readRecentInboxMessages(
                    primeraRevision ? 0 : LIMITE_REVISION
            );

            int errores = 0;

            for (var mensaje : mensajes) {

                try {

                    persistenceService.persist(mensaje);

                } catch (Exception exception) {

                    errores++;

                    logger.error(
                        "Error al procesar un correo de Gmail.",
                        exception
                    );
                }
            }

            primeraRevision = false;

            logger.info(
                "Revisión de Gmail finalizada. Consultados: {}, nuevos: {}, errores: {}",
                mensajes.size(),
                eventoRepository.count() - eventosAntes,
                errores
            );

        } catch (Exception exception) {

            // Un corte de red no debe detener la aplicación: se reintenta en la próxima revisión.
            logger.warn(
                "No se pudo revisar Gmail; se reintentará en la próxima revisión: {}",
                exception.getMessage()
            );
        }
    }
}
