package um.prog2.messaging;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import um.prog2.dto.notificacion.BackendNotificacionDTO;
import um.prog2.service.NotificadorBackendService;

@Component
public class EventoCambioConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventoCambioConsumer.class);

    private final NotificadorBackendService notificador;

    public EventoCambioConsumer(NotificadorBackendService notificador) {
        this.notificador = notificador;
    }

    @KafkaListener(topics = "${app.kafka.consumer.topic}", groupId = "${app.kafka.consumer.group-id}")
    public void onMessage(ConsumerRecord<String, String> record) {
        String payload = record.value();
        try {
            BackendNotificacionDTO dto = new BackendNotificacionDTO();
            dto.setTopic(record.topic());
            dto.setPartition(record.partition());
            dto.setOffset(record.offset());
            dto.setKey(record.key());
            dto.setPayload(payload);
            log.debug("Kafka recibido topic={}, partition={}, offset={}", record.topic(), record.partition(), record.offset());
            notificador.notificarCambio(dto);
        } catch (Exception ex) {
            log.error("Error procesando mensaje Kafka: {} - payload={} ", ex.getMessage(), payload);
        }
    }
}
