package com.example.demo.proxy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

class NotificadorBackendServiceTest {

    @Test
    void shouldPostNotificationToBackend() {
        RestTemplate restTemplate = org.mockito.Mockito.mock(RestTemplate.class);
        NotificadorBackendService service = new NotificadorBackendService() {
            {
                // override restTemplate field via reflection
                ReflectionTestUtils.setField(this, "restTemplate", restTemplate);
                ReflectionTestUtils.setField(this, "backendBaseUrl", "http://localhost:8080");
                ReflectionTestUtils.setField(this, "webhookPath", "/api/proxy-webhook");
            }
        };

        when(restTemplate.postForEntity(any(String.class), any(BackendNotificacionDTO.class), eq(Void.class)))
            .thenReturn(new ResponseEntity<Void>(HttpStatus.OK));

        BackendNotificacionDTO dto = new BackendNotificacionDTO();
        service.notificar(dto);

        verify(restTemplate).postForEntity(eq("http://localhost:8080/api/proxy-webhook"), any(BackendNotificacionDTO.class), eq(Void.class));
    }
}

