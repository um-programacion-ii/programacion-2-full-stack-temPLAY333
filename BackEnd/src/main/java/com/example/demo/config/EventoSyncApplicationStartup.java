package com.example.demo.config;

import com.example.demo.service.EventoSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Ejecuta sincronización inicial de eventos al iniciar la aplicación.
 *
 * Controlado por la propiedad: app.sync.on-startup=true/false
 * Para activarlo, agrega en application.yml o application-dev.yml:
 * app:
 *   sync:
 *     on-startup: true
 */
@Component
@ConditionalOnProperty(name = "app.sync.on-startup", havingValue = "true", matchIfMissing = false)
public class EventoSyncApplicationStartup implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(EventoSyncApplicationStartup.class);

    private final EventoSyncService eventoSyncService;

    public EventoSyncApplicationStartup(EventoSyncService eventoSyncService) {
        this.eventoSyncService = eventoSyncService;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("========================================");
        log.info("Iniciando sincronización de eventos desde la Cátedra al arranque de la aplicación");
        log.info("========================================");

        try {
            eventoSyncService.syncEventsFromCatedra();
            log.info("Sincronización inicial completada exitosamente");
        } catch (Exception e) {
            log.error("Error durante sincronización inicial de eventos", e);
            // No lanzamos la excepción para que no impida el arranque de la aplicación
        }

        log.info("========================================");
    }
}

