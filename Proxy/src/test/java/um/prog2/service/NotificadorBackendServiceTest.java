package um.prog2.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import um.prog2.dto.notificacion.BackendNotificacionDTO;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificadorBackendServiceTest {

    private WebClient webClient;
    private ObjectMapper objectMapper;

    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    private WebClient.RequestBodySpec requestBodySpec;
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    private WebClient.ResponseSpec responseSpec;

    private NotificadorBackendService notificadorService;

    @BeforeEach
    void setUp() {
        webClient = mock(WebClient.class);
        objectMapper = mock(ObjectMapper.class);
        requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        requestBodySpec = mock(WebClient.RequestBodySpec.class);
        requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);

        notificadorService = new NotificadorBackendService(webClient, objectMapper);

        // Configurar valores de properties usando reflection
        ReflectionTestUtils.setField(notificadorService, "backendBaseUrl", "http://localhost:8081");
        ReflectionTestUtils.setField(notificadorService, "webhookPath", "/api/webhooks/evento-cambio");
        ReflectionTestUtils.setField(notificadorService, "backendToken", "test-token-123");
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

        // Act
        notificadorService.notificarCambio(dto);

        // Assert: Verificar que se envió el webhook HTTP con token
        verify(webClient, times(1)).post();
        verify(requestBodySpec, times(1)).header(eq(HttpHeaders.AUTHORIZATION), anyString());
        verify(requestBodySpec, times(1)).bodyValue(dto);
    }
}
