package um.prog2.web;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import um.prog2.dto.autenticacion.LoginRequestDTO;
import um.prog2.dto.autenticacion.LoginResponseDTO;

/**
 * Controlador proxy para operaciones de usuarios (autenticación).
 */
@RestController
@RequestMapping("/proxy/users")
public class UserProxyController {

    private static final Logger log = LoggerFactory.getLogger(UserProxyController.class);

    private final WebClient webClient;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.catedra.base-url}")
    private String catedraBaseUrl;

    @Value("${app.kafka.producer.topic}")
    private String producerTopic;

    public UserProxyController(WebClient webClient, KafkaTemplate<String, String> kafkaTemplate) {
        this.webClient = webClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * POST: Login de usuario.
     * URL externa: http://192.168.194.250:8080/api/authenticate
     */
    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.debug("Proxy recibió login para usuario: {}", loginRequest.getUsername());

        return webClient.post()
            .uri(catedraBaseUrl + "/api/authenticate")
            .bodyValue(loginRequest)
            .retrieve()
            .bodyToMono(LoginResponseDTO.class)
            .map(ResponseEntity::ok)
            .doOnSuccess(resp -> {
                log.debug("Login exitoso para: {}", loginRequest.getUsername());
                try {
                    String msg = "login:username=" + loginRequest.getUsername() + ":ok";
                    kafkaTemplate.send(producerTopic, msg);
                } catch (Exception ex) {
                    log.warn("No se pudo publicar evento de login en Kafka: {}", ex.getMessage());
                }
            })
            .onErrorResume(err -> {
                log.error("Error en login para {}: {}", loginRequest.getUsername(), err.getMessage());
                try {
                    String msg = "login:username=" + loginRequest.getUsername() + ":error";
                    kafkaTemplate.send(producerTopic, msg);
                } catch (Exception ex) {
                    log.warn("No se pudo publicar evento de login (error) en Kafka: {}", ex.getMessage());
                }
                return Mono.just(ResponseEntity.status(500).build());
            });
    }
}
