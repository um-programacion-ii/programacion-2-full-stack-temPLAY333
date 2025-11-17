package um.prog2.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import um.prog2.dto.notificacion.BackendNotificacionDTO;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificadorBackendServiceTest {

    private WebClient webClient;
    private KafkaTemplate<String, String> kafkaTemplate;
    private ObjectMapper objectMapper;

    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    private WebClient.RequestBodySpec requestBodySpec;
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    private WebClient.ResponseSpec responseSpec;

    private NotificadorBackendService notificadorService;

    @BeforeEach
    void setUp() {
        webClient = mock(WebClient.class);
        kafkaTemplate = mock(KafkaTemplate.class);
        objectMapper = mock(ObjectMapper.class);
        requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        requestBodySpec = mock(WebClient.RequestBodySpec.class);
        requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);

        notificadorService = new NotificadorBackendService(webClient, kafkaTemplate, objectMapper);

        // Configurar valores de properties usando reflection
        ReflectionTestUtils.setField(notificadorService, "backendBaseUrl", "http://localhost:8081");
        ReflectionTestUtils.setField(notificadorService, "webhookPath", "/api/webhooks/evento-cambio");
        ReflectionTestUtils.setField(notificadorService, "backendToken", "test-token-123");
        ReflectionTestUtils.setField(notificadorService, "backendTopic", "backend-eventos");
    }

    @Test
    void notificarCambioDeberiaEnviarWebhookConToken() throws Exception {
        // Arrange
        BackendNotificacionDTO dto = new BackendNotificacionDTO();
        dto.setTopic("evento-cambios");
        dto.setPartition(0);
        dto.setOffset(10L);
        dto.setKey("k1");
        dto.setPayload("{\"tipo\":\"ASIENTO_BLOQUEADO\"}");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        when(objectMapper.writeValueAsString(any())).thenReturn("{json}");
        when(kafkaTemplate.send(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        // Act
        notificadorService.notificarCambio(dto);

        // Assert
        verify(webClient, times(1)).post();
        verify(requestBodySpec, times(1)).header(eq(HttpHeaders.AUTHORIZATION), anyString());
        verify(kafkaTemplate, times(1)).send(eq("backend-eventos"), anyString());
    }
}
