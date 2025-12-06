package com.example.demo.proxy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class EventoCambioConsumerTest {

    @Test
    void shouldNotifyBackendOnKafkaMessage() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        NotificadorBackendService notificador = org.mockito.Mockito.mock(NotificadorBackendService.class);
        EventoCambioConsumer consumer = new EventoCambioConsumer(mapper, notificador);

        String payload = "{\"ventaId\":1}";

        consumer.onMessage(payload, 0, 10L);

        verify(notificador).notificar(any(BackendNotificacionDTO.class));
    }
}

