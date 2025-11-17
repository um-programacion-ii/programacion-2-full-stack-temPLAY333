package um.prog2.messaging;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import um.prog2.dto.notificacion.BackendNotificacionDTO;
import um.prog2.service.NotificadorBackendService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoCambioConsumerTest {

    @Mock
    private NotificadorBackendService notificadorBackendService;

    private EventoCambioConsumer eventoCambioConsumer;

    @BeforeEach
    void setUp() {
        eventoCambioConsumer = new EventoCambioConsumer(notificadorBackendService);
    }

    @Test
    void onMessageDeberiaReenviarPayloadCrudoAlBackend() {
        // Arrange
        String payload = "{\"tipo\":\"UPDATE\",\"eventoId\":100}";
        ConsumerRecord<String, String> record = new ConsumerRecord<>("evento-cambios", 0, 0L, "key-1", payload);

        // Act
        eventoCambioConsumer.onMessage(record);

        // Assert
        ArgumentCaptor<BackendNotificacionDTO> captor = ArgumentCaptor.forClass(BackendNotificacionDTO.class);
        verify(notificadorBackendService, times(1)).notificarCambio(captor.capture());

        BackendNotificacionDTO dto = captor.getValue();
        assertEquals("evento-cambios", dto.getTopic());
        assertEquals(0, dto.getPartition());
        assertEquals(0L, dto.getOffset());
        assertEquals("key-1", dto.getKey());
        assertEquals(payload, dto.getPayload());
    }
}
