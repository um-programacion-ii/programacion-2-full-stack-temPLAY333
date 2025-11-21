package um.prog2.integration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración simples para verificar conectividad real
 * sin enviar POST que modifiquen datos en la cátedra.
 *
 * IMPORTANTE: Estos tests requieren que los servicios de la cátedra
 * (web, Redis, Kafka) estén accesibles desde tu red.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProxyIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WebClient webClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    @Disabled("Ejecutar manualmente cuando la catedra esté disponible")
    void getEventosResumidosRealDeberiaResponder200() {
        String url = "http://localhost:" + port + "/proxy/eventos/resumidos";
        ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
        assertTrue(resp.getStatusCode().is2xxSuccessful());
        assertNotNull(resp.getBody());
    }

    @Test
    @Disabled("Ejecutar manualmente cuando la catedra y Redis estén disponibles")
    void getEstadoAsientosRealDeberiaFuncionarAunqueNoHayaDatos() {
        String url = "http://localhost:" + port + "/proxy/eventos/1/asientos-estado";
        ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
        assertTrue(resp.getStatusCode().is2xxSuccessful());
    }
}

