package com.example.demo.web.rest;

import com.example.demo.service.WebhookService;
import com.example.demo.service.dto.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para recibir webhooks del Proxy.
 * El Proxy envía UN SOLO webhook genérico para todos los tipos de eventos.
 */
@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final WebhookService webhookService;
    private final ObjectMapper objectMapper;

    public WebhookController(WebhookService webhookService, ObjectMapper objectMapper) {
        this.webhookService = webhookService;
        this.objectMapper = objectMapper;
    }

    /**
     * POST /api/webhooks/evento-cambio : Recibe notificaciones del Proxy.
     *
     * El Proxy envía todos los eventos a este único endpoint.
     * El tipo de evento se discrimina por el campo 'topic'.
     *
     * @param notificacion Notificación del Proxy con topic, payload, etc.
     * @return 200 OK si se procesó correctamente, 500 si hubo error
     */
    @PostMapping("/evento-cambio")
    public ResponseEntity<Void> recibirEvento(@RequestBody BackendNotificacionDTO notificacion) {

        log.info("Webhook recibido - Topic: {}, Partition: {}, Offset: {}",
            notificacion.getTopic(), notificacion.getPartition(), notificacion.getOffset());

        try {
            // 1. Verificar idempotencia (solo para eventos de Kafka)
            if (notificacion.getPartition() != null && notificacion.getOffset() != null) {
                String idempotencyKey = String.format("kafka-%d-%d",
                    notificacion.getPartition(), notificacion.getOffset());

                if (webhookService.yaFueProcesado(idempotencyKey)) {
                    log.info("Evento ya procesado, ignorando: {}", idempotencyKey);
                    return ResponseEntity.ok().build();
                }

                // Marcar como procesado ANTES de procesar (evita duplicados en caso de retry)
                webhookService.marcarProcesado(
                    idempotencyKey,
                    notificacion.getTopic(),
                    notificacion.getPartition(),
                    notificacion.getOffset()
                );
            }

            // 2. Discriminar por topic y procesar
            String topic = notificacion.getTopic();
            String payloadJson = notificacion.getPayload();

            if (payloadJson == null || payloadJson.isEmpty()) {
                log.warn("Payload vacío para topic: {}", topic);
                return ResponseEntity.badRequest().build();
            }

            switch (topic) {
                case "VENTA_COMPLETADA":
                    RealizarVentaResponseDTO venta = objectMapper.readValue(
                        payloadJson, RealizarVentaResponseDTO.class);
                    webhookService.procesarVentaCompletada(venta);
                    break;

                case "ASIENTOS_BLOQUEADOS":
                    BloquearAsientosResponseDTO bloqueo = objectMapper.readValue(
                        payloadJson, BloquearAsientosResponseDTO.class);
                    webhookService.procesarAsientosBloqueados(bloqueo);
                    break;

                case "EVENTO_CAMBIADO":
                    // Parsear para obtener el eventoId
                    JsonNode cambio = objectMapper.readTree(payloadJson);
                    Long eventoId = cambio.get("eventoId").asLong();
                    webhookService.procesarEventoCambiado(eventoId);
                    break;

                case "http:bloquear-asientos":
                case "http:realizar-venta":
                case "http:login":
                    // Eventos HTTP síncronos - posiblemente duplicados, ignorar
                    log.debug("Evento HTTP recibido: {}, ignorando (ya procesado síncronamente)", topic);
                    break;

                case "EVENTO_GENERICO":
                    log.warn("Evento genérico no reconocido: {}", payloadJson);
                    break;

                default:
                    log.warn("Tipo de evento desconocido: {}", topic);
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error procesando webhook - Topic: {}, Error: {}",
                notificacion.getTopic(), e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}

