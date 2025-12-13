package um.prog2.web;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import um.prog2.dto.evento.consulta.EventoResumenDTO;
import um.prog2.dto.evento.consulta.EventoDTO;
import um.prog2.dto.evento.consulta.EventoDetalleDTO;
import um.prog2.dto.evento.bloqueo.AsientoEstadoDTO;
import um.prog2.dto.evento.bloqueo.BloquearAsientosRequestDTO;
import um.prog2.dto.evento.bloqueo.BloquearAsientosResponseDTO;
import um.prog2.service.AsientoRedisService;

import java.util.List;

/**
 * Controlador proxy para operaciones de eventos y asientos.
 */
@RestController
@RequestMapping("/api/eventos")
public class EventoProxyController {

    private static final Logger log = LoggerFactory.getLogger(EventoProxyController.class);

    private final WebClient webClient;
    private final AsientoRedisService asientoRedisService;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private um.prog2.service.NotificadorBackendService notificadorBackendService;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private um.prog2.service.MockCatedraService mockCatedraService;

    @Value("${app.catedra.base-url}")
    private String catedraBaseUrl;

    public EventoProxyController(WebClient webClient, AsientoRedisService asientoRedisService) {
        this.webClient = webClient;
        this.asientoRedisService = asientoRedisService;
    }

    /**
     * GET: Listado completo de eventos (datos resumidos).
     * URL externa: http://192.168.194.250:8080/api/endpoints/v1/eventos-resumidos
     */
    @GetMapping("/resumidos")
    public Mono<ResponseEntity<List<EventoResumenDTO>>> listarEventosResumidos() {
        log.debug("Proxy solicitando eventos resumidos");

        // Si el mock está activo, usarlo
        if (mockCatedraService != null) {
            log.debug("Usando MockCatedraService para eventos resumidos");
            return Mono.just(ResponseEntity.ok(mockCatedraService.listarEventosResumidos()));
        }

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

        // Si el mock está activo, usarlo
        if (mockCatedraService != null) {
            log.debug("Usando MockCatedraService para eventos completos");
            return Mono.just(ResponseEntity.ok(mockCatedraService.listarEventosCompletos()));
        }

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

        // Si el mock está activo, usarlo
        if (mockCatedraService != null) {
            log.debug("Usando MockCatedraService para evento {}", id);
            EventoDetalleDTO detalle = mockCatedraService.obtenerEventoDetalle(id);
            if (detalle != null) {
                return Mono.just(ResponseEntity.ok(detalle));
            } else {
                return Mono.just(ResponseEntity.notFound().build());
            }
        }

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
     * Nota: El resultado asíncrono llegará vía Kafka y se notificará al BackEnd vía webhook.
     */
    @PostMapping("/bloquear-asientos")
    public Mono<ResponseEntity<BloquearAsientosResponseDTO>> bloquearAsientos(
            @Valid @RequestBody BloquearAsientosRequestDTO request) {
        log.debug("Proxy bloqueando asientos para evento: {}", request.getEventoId());

        // Si el mock está activo, usarlo
        if (mockCatedraService != null) {
            log.debug("Usando MockCatedraService para bloquear asientos");
            BloquearAsientosResponseDTO response = mockCatedraService.bloquearAsientos(
                request.getEventoId(),
                request.getAsientos()
            );
            return Mono.just(ResponseEntity.ok(response));
        }

        return webClient.post()
            .uri(catedraBaseUrl + "/api/endpoints/v1/bloquear-asientos")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(BloquearAsientosResponseDTO.class)
            .map(ResponseEntity::ok)
            .doOnSuccess(resp -> {
                log.debug("Bloqueo de asientos resultado: {}", resp.getBody() != null ? resp.getBody().getResultado() : null);
                // Notificar al BackEnd con la respuesta síncrona de Cátedra
                try {
                    if (notificadorBackendService != null && resp.getBody() != null) {
                        notificadorBackendService.notificarCambioDesdeHttp("http:bloquear-asientos", resp.getBody());
                    }
                } catch (Exception ex) {
                    log.warn("No se pudo notificar al backend el resultado de bloqueo: {}", ex.getMessage());
                }
            })
            .doOnError(err -> log.error("Error bloqueando asientos: {}", err.getMessage()));
    }

    /**
     * GET: Estado actual de los asientos de un evento desde Redis.
     * Si existe hash 'evento:{id}:asientos' se usa; si no, keys 'evento:{id}:asiento:*'.
     */
    @GetMapping("/{id}/asientos-estado")
    public Mono<ResponseEntity<List<AsientoEstadoDTO>>> obtenerEstadoAsientos(@PathVariable Long id) {
        // Si el mock está activo, usarlo
        if (mockCatedraService != null) {
            log.debug("Usando MockCatedraService para estado de asientos del evento {}", id);
            return Mono.just(ResponseEntity.ok(mockCatedraService.obtenerEstadoAsientos(id)));
        }

        return Mono.defer(() -> Mono.just(asientoRedisService.obtenerEstadoAsientos(id)))
            .map(ResponseEntity::ok)
            .onErrorResume(err -> {
                log.error("Error obteniendo estado de asientos para evento {}: {}", id, err.getMessage());
                return Mono.just(ResponseEntity.status(500).build());
            });
    }
}
