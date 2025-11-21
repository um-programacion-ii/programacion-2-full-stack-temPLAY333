package um.prog2.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Para soportar Java 8 time
        eventoCambioConsumer = new EventoCambioConsumer(notificadorBackendService, objectMapper);
    }

    @Test
    void onMessageDeberiaDetectarTipoYReenviarPayloadAlBackend() {
        // Arrange - VentaCompletadaEventoDTO
        String payload = "{\"ventaId\":123,\"eventoId\":100,\"usuarioEmail\":\"test@um.edu.ar\",\"resultado\":true,\"fechaVenta\":\"2025-01-27T10:30:00Z\",\"precioVenta\":150.00,\"asientos\":[{\"fila\":1,\"columna\":5,\"persona\":\"Juan\"}]}";
        ConsumerRecord<String, String> record = new ConsumerRecord<>("evento-cambios", 0, 0L, "key-1", payload);

        // Act
        eventoCambioConsumer.onMessage(record);

        // Assert
        ArgumentCaptor<BackendNotificacionDTO> captor = ArgumentCaptor.forClass(BackendNotificacionDTO.class);
        verify(notificadorBackendService, times(1)).notificarCambio(captor.capture());

        BackendNotificacionDTO dto = captor.getValue();
        assertEquals("VENTA_COMPLETADA", dto.getTopic()); // Detecta el tipo correctamente
        assertEquals(0, dto.getPartition());
        assertEquals(0L, dto.getOffset());
        assertEquals("key-1", dto.getKey());
        assertEquals(payload, dto.getPayload()); // JSON crudo se mantiene
    }

    @Test
    void onMessageDeberiaDetectarAsientosBloqueados() {
        // Arrange - AsientosBloqueadosEventoDTO
        String payload = "{\"eventoId\":200,\"resultado\":true,\"bloqueadoHasta\":\"2025-01-27T10:35:00Z\",\"asientos\":[{\"fila\":2,\"columna\":3,\"estado\":\"BLOQUEADO\"}]}";
        ConsumerRecord<String, String> record = new ConsumerRecord<>("evento-cambios", 0, 1L, "key-2", payload);

        // Act
        eventoCambioConsumer.onMessage(record);

        // Assert
        ArgumentCaptor<BackendNotificacionDTO> captor = ArgumentCaptor.forClass(BackendNotificacionDTO.class);
        verify(notificadorBackendService, times(1)).notificarCambio(captor.capture());

        BackendNotificacionDTO dto = captor.getValue();
        assertEquals("ASIENTOS_BLOQUEADOS", dto.getTopic());
        assertEquals(payload, dto.getPayload());
    }
}
