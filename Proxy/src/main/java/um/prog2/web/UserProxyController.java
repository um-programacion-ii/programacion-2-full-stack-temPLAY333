package um.prog2.web;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
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
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private um.prog2.service.NotificadorBackendService notificadorBackendService;

    @Value("${app.catedra.base-url}")
    private String catedraBaseUrl;

    public UserProxyController(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * POST: Login de usuario.
     * URL externa: http://192.168.194.250:8080/api/authenticate
     */
    /**
     * POST: Login de usuario.
     * URL externa: http://192.168.194.250:8080/api/authenticate
     * Nota: El resultado asíncrono llegará vía Kafka y se notificará al BackEnd vía webhook.
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
                // Notificar al BackEnd con la respuesta síncrona de Cátedra
                try {
                    if (notificadorBackendService != null && resp.getBody() != null) {
                        notificadorBackendService.notificarCambioDesdeHttp("http:login", resp.getBody());
                    }
                } catch (Exception ex) {
                    log.warn("No se pudo notificar al backend el resultado del login: {}", ex.getMessage());
                }
            })
            .onErrorResume(err -> {
                log.error("Error en login para {}: {}", loginRequest.getUsername(), err.getMessage());
                return Mono.just(ResponseEntity.status(500).build());
            });
    }
}
