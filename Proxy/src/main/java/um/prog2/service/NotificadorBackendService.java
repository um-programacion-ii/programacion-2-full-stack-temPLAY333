package um.prog2.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
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
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.backend.base-url}")
    private String backendBaseUrl;

    @Value("${app.backend.webhook-path}")
    private String webhookPath;

    @Value("${app.backend.token:}")
    private String backendToken;

    @Value("${app.kafka.producer.topic}")
    private String backendTopic;

    public NotificadorBackendService(WebClient webClient, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void notificarCambio(BackendNotificacionDTO dto) {
        WebClient.RequestBodySpec request = webClient.post()
            .uri(backendBaseUrl + webhookPath);
        if (backendToken != null && !backendToken.isBlank()) {
            request = request.header(HttpHeaders.AUTHORIZATION, "Bearer " + backendToken);
        }
        // 1) Notificar por webhook (prioritario)
        Mono<Void> webhook = request
            .bodyValue(dto)
            .retrieve()
            .bodyToMono(Void.class)
            .doOnSuccess(v -> log.debug("Webhook al backend enviado con éxito"))
            .doOnError(err -> log.error("Error enviando webhook al backend: {}", err.getMessage()))
            .onErrorResume(err -> Mono.empty()); // no propagar error

        // 2) Publicar a Kafka (mejor esfuerzo, no bloqueante)
        Mono<Void> kafka = Mono.<Void>create(sink -> {
                try {
                    String json = objectMapper.writeValueAsString(dto);
                    kafkaTemplate.send(backendTopic, json)
                        .whenComplete((res, ex) -> {
                            if (ex != null) {
                                log.error("Error publicando en Kafka: {}", ex.getMessage());
                            } else {
                                log.debug("Mensaje publicado en Kafka topic {}", backendTopic);
                            }
                            sink.success(null);
                        });
                } catch (Exception ex) {
                    log.error("Error serializando notificación a Kafka: {}", ex.getMessage());
                    sink.success(null);
                }
            })
            .onErrorResume(err -> Mono.empty());

        // Ejecutar en paralelo, sin bloquear
        Mono.whenDelayError(webhook, kafka).subscribe();
    }
}
