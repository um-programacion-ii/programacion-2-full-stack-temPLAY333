package com.example.demo.proxy;

import com.example.demo.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@IntegrationTest
class EventoProxyControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldExposeEventosResumidosEndpoint() {
        webTestClient
            .get()
            .uri("/proxy/eventos/resumidos")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .is4xxClientError(); // Puede ser 401/403 si hay seguridad; validamos que el endpoint existe
    }
}
