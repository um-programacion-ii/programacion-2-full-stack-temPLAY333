package um.prog2.web;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import um.prog2.dto.evento.consulta.EventoResumenDTO;
import um.prog2.dto.evento.consulta.EventoDTO;
import um.prog2.dto.evento.consulta.EventoDetalleDTO;
import um.prog2.dto.evento.asientos.AsientoBloqueoEstadoDTO;
import um.prog2.dto.evento.bloqueo.BloquearAsientosRequestDTO;
import um.prog2.dto.evento.bloqueo.BloquearAsientosResponseDTO;
import um.prog2.service.AsientoRedisService;

import java.util.List;

/**
 * Controlador proxy para operaciones de eventos y asientos.
 */
@RestController
@RequestMapping("/proxy/eventos")
public class EventoProxyController {

    private static final Logger log = LoggerFactory.getLogger(EventoProxyController.class);

    private final WebClient webClient;
    private final AsientoRedisService asientoRedisService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.catedra.base-url}")
    private String catedraBaseUrl;

    @Value("${app.kafka.producer.topic}")
    private String producerTopic;

    public EventoProxyController(WebClient webClient, AsientoRedisService asientoRedisService, KafkaTemplate<String, String> kafkaTemplate) {
        this.webClient = webClient;
        this.asientoRedisService = asientoRedisService;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * GET: Listado completo de eventos (datos resumidos).
     * URL externa: http://192.168.194.250:8080/api/endpoints/v1/eventos-resumidos
     */
    @GetMapping("/resumidos")
    public Mono<ResponseEntity<List<EventoResumenDTO>>> listarEventosResumidos() {
        log.debug("Proxy solicitando eventos resumidos");

        return webClient.get()
            .uri(catedraBaseUrl + "/api/endpoints/v1/eventos-resumidos")
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<EventoResumenDTO>>() {})
            .map(ResponseEntity::ok)
            .doOnSuccess(resp -> log.debug("Eventos resumidos obtenidos: {} items", resp.getBody() != null ? resp.getBody().size() : 0))
            .doOnError(err -> log.error("Error obteniendo eventos resumidos: {}", err.getMessage()));
    }

    /**
     * GET: Listado completo de eventos (con todos los datos).
     * URL externa: http://192.168.194.250:8080/api/endpoints/v1/eventos
     */
    @GetMapping
    public Mono<ResponseEntity<List<EventoDTO>>> listarEventosCompletos() {
        log.debug("Proxy solicitando eventos completos");

        return webClient.get()
            .uri(catedraBaseUrl + "/api/endpoints/v1/eventos")
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<EventoDTO>>() {})
            .map(ResponseEntity::ok)
            .doOnSuccess(resp -> log.debug("Eventos completos obtenidos: {} items", resp.getBody() != null ? resp.getBody().size() : 0))
            .doOnError(err -> log.error("Error obteniendo eventos completos: {}", err.getMessage()));
    }

    /**
     * GET: Datos completos de un evento.
     * URL externa: http://192.168.194.250:8080/api/endpoints/v1/evento/{id}
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<EventoDetalleDTO>> obtenerEvento(@PathVariable Long id) {
        log.debug("Proxy solicitando evento con id: {}", id);

        return webClient.get()
            .uri(catedraBaseUrl + "/api/endpoints/v1/evento/{id}", id)
            .retrieve()
            .bodyToMono(EventoDetalleDTO.class)
            .map(ResponseEntity::ok)
            .doOnSuccess(resp -> log.debug("Evento {} obtenido", id))
            .doOnError(err -> log.error("Error obteniendo evento {}: {}", id, err.getMessage()));
    }

    /**
     * POST: Bloqueo de asiento por evento.
     * URL externa: http://192.168.194.250:8080/api/endpoints/v1/bloquear-asientos
     */
    @PostMapping("/bloquear-asientos")
    public Mono<ResponseEntity<BloquearAsientosResponseDTO>> bloquearAsientos(
            @Valid @RequestBody BloquearAsientosRequestDTO request) {
        log.debug("Proxy bloqueando asientos para evento: {}", request.getEventoId());

        return webClient.post()
            .uri(catedraBaseUrl + "/api/endpoints/v1/bloquear-asientos")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(BloquearAsientosResponseDTO.class)
            .map(ResponseEntity::ok)
            .doOnSuccess(resp -> {
                log.debug("Bloqueo de asientos resultado: {}", resp.getBody() != null ? resp.getBody().getResultado() : null);
                // Publicar a Kafka para auditoría/seguimiento (POST => Kafka)
                try {
                    String mensaje = "bloquear-asientos:evento=" + request.getEventoId();
                    kafkaTemplate.send(producerTopic, mensaje);
                } catch (Exception ex) {
                    log.warn("No se pudo publicar mensaje de auditoría en Kafka: {}", ex.getMessage());
                }
            })
            .doOnError(err -> log.error("Error bloqueando asientos: {}", err.getMessage()));
    }

    /**
     * GET: Estado actual de los asientos de un evento desde Redis.
     * Si existe hash 'evento:{id}:asientos' se usa; si no, keys 'evento:{id}:asiento:*'.
     */
    @GetMapping("/{id}/asientos-estado")
    public Mono<ResponseEntity<List<AsientoBloqueoEstadoDTO>>> obtenerEstadoAsientos(@PathVariable Long id) {
        return Mono.defer(() -> Mono.just(asientoRedisService.obtenerEstadoAsientos(id)))
            .map(ResponseEntity::ok)
            .onErrorResume(err -> {
                log.error("Error obteniendo estado de asientos para evento {}: {}", id, err.getMessage());
                return Mono.just(ResponseEntity.status(500).build());
            });
    }
}
