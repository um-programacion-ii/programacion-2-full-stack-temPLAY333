package com.example.demo.web.rest;

import com.example.demo.service.EventoWebhookService;
import com.example.demo.service.dto.BackendNotificacionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller para recibir notificaciones del Proxy vía webhook.
 * El Proxy envía eventos asíncronos de Kafka aquí.
 */
@RestController
@RequestMapping("/api/webhooks")
public class EventoWebhookResource {

    private static final Logger log = LoggerFactory.getLogger(EventoWebhookResource.class);

    private final EventoWebhookService eventoWebhookService;

    public EventoWebhookResource(EventoWebhookService eventoWebhookService) {
        this.eventoWebhookService = eventoWebhookService;
    }

    /**
     * POST /api/webhooks/evento-cambio : Recibe notificaciones del Proxy.
     *
     * El Proxy envía aquí los eventos que recibe de Kafka de la Cátedra.
     */
    @PostMapping("/evento-cambio")
    public ResponseEntity<String> recibirNotificacion(@RequestBody BackendNotificacionDTO notificacion) {
        log.info("Webhook recibido del Proxy - Topic: {}, Timestamp: {}",
            notificacion.getTopic(), notificacion.getTimestamp());

        try {
            eventoWebhookService.procesarNotificacion(notificacion);
            return ResponseEntity.ok("Notificación procesada correctamente");
        } catch (Exception e) {
            log.error("Error al procesar notificación del Proxy", e);
            return ResponseEntity.internalServerError()
                .body("Error al procesar notificación: " + e.getMessage());
        }
    }

    /**
     * GET /api/webhooks/health : Health check del webhook.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Webhook activo y listo para recibir notificaciones");
    }
}

