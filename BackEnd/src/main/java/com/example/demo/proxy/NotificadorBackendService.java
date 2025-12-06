package com.example.demo.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Servicio que envía notificaciones al Backend (webhook HTTP).
 */
@Service
public class NotificadorBackendService {

    private static final Logger log = LoggerFactory.getLogger(NotificadorBackendService.class);

    private final RestTemplate restTemplate;

    @Value("${app.backend.base-url:http://localhost:8080}")
    private String backendBaseUrl;

    @Value("${app.backend.webhook-path:/api/proxy-webhook}")
    private String webhookPath;

    public NotificadorBackendService() {
        this.restTemplate = new RestTemplate();
    }

    public void notificar(BackendNotificacionDTO dto) {
        String url = backendBaseUrl + webhookPath;
        log.debug("Enviando notificación al Backend: {}", url);
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(url, dto, Void.class);
            log.debug("Respuesta del Backend al webhook: status={}", response.getStatusCode());
        } catch (Exception ex) {
            log.error("Error enviando notificación al Backend", ex);
        }
    }
}
