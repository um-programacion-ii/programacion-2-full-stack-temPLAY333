package com.example.demo.proxy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Consumer Kafka que recibe mensajes de la cátedra y los reenvía al Backend vía webhook.
 */
@Service
public class EventoCambioConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventoCambioConsumer.class);

    private final ObjectMapper objectMapper;
    private final NotificadorBackendService notificadorBackendService;

    public EventoCambioConsumer(ObjectMapper objectMapper, NotificadorBackendService notificadorBackendService) {
        this.objectMapper = objectMapper;
        this.notificadorBackendService = notificadorBackendService;
    }

    @KafkaListener(topics = "${app.kafka.consumer.topic:eventos-actualizacion}", groupId = "proxy-group")
    public void onMessage(
        @Payload String payload,
        @Header(name = "kafka_receivedPartitionId", required = false) Integer partition,
        @Header(name = "kafka_offset", required = false) Long offset
    ) {
        int partValue = partition != null ? partition : -1;
        long offsetValue = offset != null ? offset : -1L;
        String key = null;
        log.debug("Mensaje Kafka recibido: partition={}, offset={}, key={}, payload={}", partValue, offsetValue, key, payload);

        try {
            JsonNode root = objectMapper.readTree(payload);
            String logicalTopic = detectarTipoMensaje(root);

            BackendNotificacionDTO dto = new BackendNotificacionDTO();
            dto.setTopic(logicalTopic);
            dto.setPayload(payload);
            dto.setPartition(partValue);
            dto.setOffset(offsetValue);
            dto.setKey(key);

            notificadorBackendService.notificar(dto);
        } catch (Exception e) {
            log.error("Error procesando mensaje Kafka para notificación al Backend", e);
        }
    }

    private String detectarTipoMensaje(JsonNode root) {
        if (root.has("ventaId")) {
            return "VENTA_COMPLETADA";
        }
        if (root.has("asientos") && root.has("eventoId")) {
            return "ASIENTOS_BLOQUEADOS";
        }
        if (root.has("evento") || root.has("eventoId")) {
            return "EVENTO_CAMBIADO";
        }
        return "EVENTO_DESCONOCIDO";
    }
}
