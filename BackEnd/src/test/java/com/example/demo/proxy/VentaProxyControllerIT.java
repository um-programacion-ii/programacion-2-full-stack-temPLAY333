package com.example.demo.proxy;

import com.example.demo.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@IntegrationTest
class VentaProxyControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldExposeListarVentasEndpoint() {
        webTestClient
            .get()
            .uri("/proxy/ventas")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk();
    }
}

