package com.example.demo.proxy;

import com.example.demo.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@IntegrationTest
class UserProxyControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldExposeLoginEndpoint() {
        webTestClient
            .post()
            .uri("/proxy/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{}")
            .exchange()
            .expectStatus()
            .is4xxClientError();
    }
}

