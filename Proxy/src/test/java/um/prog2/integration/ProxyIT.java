package um.prog2.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import um.prog2.service.AuthTokenService;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración simples para verificar conectividad real
 * sin enviar POST que modifiquen datos en la cátedra.
 *
 * IMPORTANTE: Estos tests requieren que los servicios de la cátedra
 * (web, Redis, Kafka) estén accesibles desde tu red.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
class ProxyIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WebClient webClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired(required = false)
    private AuthTokenService authTokenService;

    @BeforeEach
    void refreshTokenIfPossible() {
        if (authTokenService != null) {
            authTokenService.forceRefresh();
        }
    }

    @Test
    void getEventosResumidosRealDeberiaResponder200() {
        // Verificar que el token está disponible
        assertNotNull(authTokenService, "AuthTokenService debe estar disponible");
        assertTrue(authTokenService.getCurrentToken().isPresent(),
                   "El token JWT debe estar disponible antes de hacer la request");

        String url = "http://localhost:" + port + "/api/eventos/resumidos";
        ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);

        assertTrue(resp.getStatusCode().is2xxSuccessful(),
                   "Debería responder 200 OK (obtuvo: " + resp.getStatusCode() + ")");
        assertNotNull(resp.getBody(), "El body no debe ser null");
        assertTrue(resp.getBody().contains("["),
                   "La respuesta debería ser un array JSON (aunque puede estar vacío)");
    }

    @Test
    void getEventosCompletosRealDeberiaResponder200() {
        // Verificar que el token está disponible
        assertTrue(authTokenService.getCurrentToken().isPresent(),
                   "El token JWT debe estar disponible");

        String url = "http://localhost:" + port + "/api/eventos";
        ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);

        assertTrue(resp.getStatusCode().is2xxSuccessful(),
                   "Debería responder 200 OK (obtuvo: " + resp.getStatusCode() + ")");
        assertNotNull(resp.getBody(), "El body no debe ser null");
        assertTrue(resp.getBody().contains("["),
                   "La respuesta debería ser un array JSON");
    }

    @Test
    void getEstadoAsientosRealDeberiaFuncionarAunqueNoHayaDatos() {
        String url = "http://localhost:" + port + "/api/eventos/1/asientos-estado";
        ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);

        assertTrue(resp.getStatusCode().is2xxSuccessful(),
                   "Debería responder 200 OK incluso sin datos en Redis");
        assertNotNull(resp.getBody(), "El body no debe ser null");
        assertTrue(resp.getBody().contains("["),
                   "La respuesta debería ser un array JSON (vacío si no hay datos en Redis)");
    }

    @Test
    void healthCheckDeberiaResponderUp() {
        String url = "http://localhost:" + port + "/actuator/health";
        ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);

        assertTrue(resp.getStatusCode().is2xxSuccessful());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().contains("UP"),
                   "El health check debería indicar que el servicio está UP");
    }

    @Test
    void authStatusDeberiaIndicarTokenDisponible() {
        String url = "http://localhost:" + port + "/actuator/auth/status";
        ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);

        assertTrue(resp.getStatusCode().is2xxSuccessful());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().contains("\"hasToken\":true"),
                   "El AuthTokenService debería tener un token disponible");
        assertTrue(resp.getBody().contains("tokenLength"),
                   "Debería incluir la longitud del token");
    }
}
