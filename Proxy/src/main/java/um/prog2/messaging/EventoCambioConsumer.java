package um.prog2.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import um.prog2.dto.notificacion.*;
import um.prog2.service.NotificadorBackendService;

@Component
public class EventoCambioConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventoCambioConsumer.class);

    private final NotificadorBackendService notificador;
    private final ObjectMapper objectMapper;

    public EventoCambioConsumer(NotificadorBackendService notificador, ObjectMapper objectMapper) {
        this.notificador = notificador;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.consumer.topic}", groupId = "${app.kafka.consumer.group-id}")
    public void onMessage(ConsumerRecord<String, String> record) {
        String payload = record.value();
        try {
            // 1. Intentar leer tipo desde header de Kafka (si Cátedra lo provee)
            String tipo = null;
            if (record.headers().lastHeader("tipo") != null) {
                tipo = new String(record.headers().lastHeader("tipo").value());
            }

            // 2. Si no hay header, detectar tipo por estructura del JSON
            if (tipo == null) {
                tipo = detectarTipoEvento(payload);
            }

            BackendNotificacionDTO dto = new BackendNotificacionDTO();
            dto.setTopic(tipo); // El tipo identificado
            dto.setPartition(record.partition());
            dto.setOffset(record.offset());
            dto.setKey(record.key());
            dto.setPayload(payload); // JSON crudo

            log.debug("Kafka recibido tipo={}, partition={}, offset={}", tipo, record.partition(), record.offset());
            notificador.notificarCambio(dto);
        } catch (Exception ex) {
            log.error("Error procesando mensaje Kafka: {} - payload={} ", ex.getMessage(), payload);
        }
    }

    /**
     * Detecta el tipo de evento analizando la estructura del JSON.
     * Plan B: deserializar a los DTOs específicos para determinar el tipo.
     */
    private String detectarTipoEvento(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);

            // Detectar VentaCompletadaEventoDTO: tiene ventaId y asientos
            if (root.has("ventaId") && root.has("asientos") && root.has("fechaVenta")) {
                // Validar deserialización
                objectMapper.treeToValue(root, VentaCompletadaEventoDTO.class);
                return "VENTA_COMPLETADA";
            }

            // Detectar AsientosBloqueadosEventoDTO: tiene bloqueadoHasta
            if (root.has("eventoId") && root.has("bloqueadoHasta")) {
                objectMapper.treeToValue(root, AsientosBloqueadosEventoDTO.class);
                return "ASIENTOS_BLOQUEADOS";
            }

            // Detectar EventoCambiadoEventoDTO: tiene tipoCambio
            if (root.has("eventoId") && root.has("tipoCambio")) {
                objectMapper.treeToValue(root, EventoCambiadoEventoDTO.class);
                return "EVENTO_CAMBIADO";
            }

            // Si no coincide con ningún tipo conocido, devolver genérico
            log.warn("Tipo de evento desconocido, enviando como EVENTO_GENERICO: {}", payload);
            return "EVENTO_GENERICO";

        } catch (Exception ex) {
            log.error("Error detectando tipo de evento: {}", ex.getMessage());
            return "EVENTO_DESCONOCIDO";
        }
    }
}
