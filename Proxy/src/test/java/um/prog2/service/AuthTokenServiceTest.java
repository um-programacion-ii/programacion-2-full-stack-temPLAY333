package um.prog2.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import um.prog2.dto.autenticacion.LoginResponseDTO;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthTokenServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private AuthTokenService authTokenService;

    @BeforeEach
    void setUp() {
        authTokenService = new AuthTokenService();

        // Configurar valores usando reflection
        ReflectionTestUtils.setField(authTokenService, "catedraBaseUrl", "http://test:8080");
        ReflectionTestUtils.setField(authTokenService, "apiBase", "/api");
        ReflectionTestUtils.setField(authTokenService, "username", "testuser");
        ReflectionTestUtils.setField(authTokenService, "password", "testpass");
        ReflectionTestUtils.setField(authTokenService, "tokenTtlSeconds", 3600L);
        ReflectionTestUtils.setField(authTokenService, "webClientRaw", webClient);
    }

    @Test
    void getCurrentToken_deberiaObtenerTokenSiNoExiste() {
        // Arrange
        LoginResponseDTO mockResponse = new LoginResponseDTO();
        mockResponse.setIdToken("mock-jwt-token-12345");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(LoginResponseDTO.class)).thenReturn(Mono.just(mockResponse));

        // Act
        Optional<String> token = authTokenService.getCurrentToken();

        // Assert
        assertTrue(token.isPresent());
        assertEquals("mock-jwt-token-12345", token.get());
        verify(webClient, times(1)).post();
    }

    @Test
    void forceRefresh_deberiaSolicitarNuevoToken() {
        // Arrange
        LoginResponseDTO mockResponse = new LoginResponseDTO();
        mockResponse.setIdToken("new-token-67890");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(LoginResponseDTO.class)).thenReturn(Mono.just(mockResponse));

        // Act
        Optional<String> token = authTokenService.forceRefresh();

        // Assert
        assertTrue(token.isPresent());
        assertEquals("new-token-67890", token.get());
        verify(webClient, times(1)).post();
    }

    @Test
    void getCurrentToken_deberiaDevolverTokenExistenteSiEsValido() {
        // Arrange - Primer request para establecer el token
        LoginResponseDTO mockResponse = new LoginResponseDTO();
        mockResponse.setIdToken("initial-token");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(LoginResponseDTO.class)).thenReturn(Mono.just(mockResponse));

        // Primera llamada para establecer el token
        authTokenService.getCurrentToken();

        // Reset del mock para verificar que no se llame de nuevo
        reset(webClient);

        // Act - Segunda llamada debería devolver el mismo token sin hacer request
        Optional<String> token = authTokenService.getCurrentToken();

        // Assert
        assertTrue(token.isPresent());
        assertEquals("initial-token", token.get());
        // No debería haber hecho una nueva llamada HTTP
        verify(webClient, never()).post();
    }

    @Test
    void scheduledTokenRefresh_deberiaRenovarToken() {
        // Arrange
        LoginResponseDTO mockResponse = new LoginResponseDTO();
        mockResponse.setIdToken("scheduled-refresh-token");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(LoginResponseDTO.class)).thenReturn(Mono.just(mockResponse));

        // Act
        authTokenService.scheduledTokenRefresh();

        // Assert
        verify(webClient, times(1)).post();
        Optional<String> token = authTokenService.getCurrentToken();
        assertTrue(token.isPresent());
        assertEquals("scheduled-refresh-token", token.get());
    }

    @Test
    void init_conCredencialesConfiguradasDeberiaObtenerToken() {
        // Arrange - Las credenciales ya están configuradas via ReflectionTestUtils en setUp()
        LoginResponseDTO mockResponse = new LoginResponseDTO();
        mockResponse.setIdToken("init-token");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(LoginResponseDTO.class)).thenReturn(Mono.just(mockResponse));

        // Act
        authTokenService.init();

        // Assert
        verify(webClient, atLeastOnce()).post();
        Optional<String> token = authTokenService.getCurrentToken();
        assertTrue(token.isPresent(), "El token debería estar presente después de init()");
    }

    @Test
    void init_sinCredencialesNoDeberiaIntentarObtenerToken() {
        // Arrange - Crear service sin credenciales
        AuthTokenService serviceNoCreds = new AuthTokenService();

        ReflectionTestUtils.setField(serviceNoCreds, "catedraBaseUrl", "http://test:8080");
        ReflectionTestUtils.setField(serviceNoCreds, "apiBase", "/api");
        ReflectionTestUtils.setField(serviceNoCreds, "username", "");
        ReflectionTestUtils.setField(serviceNoCreds, "password", "");
        ReflectionTestUtils.setField(serviceNoCreds, "tokenTtlSeconds", 3600L);

        // Act
        serviceNoCreds.init();

        // Assert
        Optional<String> token = serviceNoCreds.getCurrentToken();
        assertFalse(token.isPresent(), "No debería haber token si no hay credenciales");
    }
}

