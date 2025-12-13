package um.prog2.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.ObjectMapper;
import um.prog2.dto.notificacion.BackendNotificacionDTO;

@Service
public class NotificadorBackendService {

    private static final Logger log = LoggerFactory.getLogger(NotificadorBackendService.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${app.backend.base-url}")
    private String backendBaseUrl;

    @Value("${app.backend.webhook-path}")
    private String webhookPath;

    @Value("${app.backend.token:}")
    private String backendToken;

    public NotificadorBackendService(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Helper para notificar al backend ante respuestas de POST HTTP del proxy.
     * Serializa el objeto de respuesta en JSON y lo envuelve en BackendNotificacionDTO.
     * @param source etiqueta o nombre lógico de la operación (por ejemplo, "http:bloquear-asientos").
     * @param payloadObj DTO de respuesta recibido desde el servicio externo.
     */
    public void notificarCambioDesdeHttp(String source, Object payloadObj) {
        try {
            String json = objectMapper.writeValueAsString(payloadObj);
            BackendNotificacionDTO dto = new BackendNotificacionDTO();
            dto.setTopic(source);
            dto.setPayload(json);
            // partition/offset/key se dejan nulos porque no proviene de Kafka
            notificarCambio(dto);
        } catch (Exception ex) {
            log.error("No se pudo serializar payload para notificar al backend: {}", ex.getMessage());
        }
    }

    public void notificarCambio(BackendNotificacionDTO dto) {
        if (backendBaseUrl == null || backendBaseUrl.isBlank() || webhookPath == null || webhookPath.isBlank()) {
            log.warn("Saltando webhook: app.backend.base-url o app.backend.webhook-path no configurados");
            return;
        }

        WebClient.RequestBodySpec request = webClient.post()
            .uri(backendBaseUrl + webhookPath);
        if (backendToken != null && !backendToken.isBlank()) {
            request = request.header(HttpHeaders.AUTHORIZATION, "Bearer " + backendToken);
        }

        // Notificar al BackEnd vía webhook HTTP
        request
            .bodyValue(dto)
            .retrieve()
            .bodyToMono(Void.class)
            .doOnSuccess(v -> log.debug("Webhook al backend enviado con éxito para evento tipo={}", dto.getTopic()))
            .doOnError(err -> log.error("Error enviando webhook al backend: {}", err.getMessage()))
            .onErrorResume(err -> Mono.empty()) // No propagar error
            .subscribe();
    }
}
