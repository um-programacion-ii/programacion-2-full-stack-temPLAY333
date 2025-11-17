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
import um.prog2.dto.venta.RealizarVentaRequestDTO;
import um.prog2.dto.venta.RealizarVentaResponseDTO;
import um.prog2.dto.consultaventas.VentaDetalleDTO;
import um.prog2.dto.consultaventas.VentaResumenDTO;

import java.util.List;

/**
 * Controlador proxy para operaciones de ventas.
 */
@RestController
@RequestMapping("/proxy/ventas")
public class VentaProxyController {

    private static final Logger log = LoggerFactory.getLogger(VentaProxyController.class);

    private final WebClient webClient;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.catedra.base-url}")
    private String catedraBaseUrl;

    @Value("${app.kafka.producer.topic}")
    private String producerTopic;

    public VentaProxyController(WebClient webClient, KafkaTemplate<String, String> kafkaTemplate) {
        this.webClient = webClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * POST: Venta de asientos por un evento.
     * URL externa: http://192.168.194.250:8080/api/endpoints/v1/realizar-venta
     */
    @PostMapping("/realizar")
    public Mono<ResponseEntity<RealizarVentaResponseDTO>> realizarVenta(
            @Valid @RequestBody RealizarVentaRequestDTO request) {
        log.debug("Proxy realizando venta para evento: {}", request.getEventoId());

        return webClient.post()
            .uri(catedraBaseUrl + "/api/endpoints/v1/realizar-venta")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(RealizarVentaResponseDTO.class)
            .map(response -> ResponseEntity.ok(response))
            .doOnSuccess(resp -> {
                if (resp.getBody() != null) {
                    log.debug("Venta realizada, resultado: {}", resp.getBody().getResultado());
                }
                try {
                    String mensaje = "realizar-venta:evento=" + request.getEventoId();
                    kafkaTemplate.send(producerTopic, mensaje);
                } catch (Exception ex) {
                    log.warn("No se pudo publicar mensaje de auditoría en Kafka: {}", ex.getMessage());
                }
            })
            .onErrorResume(err -> {
                log.error("Error realizando venta: {}", err.getMessage());
                return Mono.just(ResponseEntity.status(500).build());
            });
    }

    /**
     * GET: Listado completo de ventas por cada alumno (datos resumidos).
     * URL externa: http://192.168.194.250:8080/api/endpoints/v1/listar-ventas
     */
    @GetMapping
    public Mono<ResponseEntity<List<VentaResumenDTO>>> listarVentas() {
        log.debug("Proxy solicitando listado de ventas");

        return webClient.get()
            .uri(catedraBaseUrl + "/api/endpoints/v1/listar-ventas")
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<VentaResumenDTO>>() {})
            .map(response -> ResponseEntity.ok(response))
            .doOnSuccess(resp -> {
                if (resp.getBody() != null) {
                    log.debug("Ventas obtenidas: {} items", resp.getBody().size());
                }
            })
            .doOnError(err -> log.error("Error obteniendo ventas: {}", err.getMessage()));
    }

    /**
     * GET: Ver datos de una venta particular.
     * URL externa: http://192.168.194.250:8080/api/endpoints/v1/listar-venta/{id}
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<VentaDetalleDTO>> obtenerVenta(@PathVariable Long id) {
        log.debug("Proxy solicitando venta con id: {}", id);

        return webClient.get()
            .uri(catedraBaseUrl + "/api/endpoints/v1/listar-venta/{id}", id)
            .retrieve()
            .bodyToMono(VentaDetalleDTO.class)
            .map(response -> ResponseEntity.ok(response))
            .doOnSuccess(resp -> log.debug("Venta {} obtenida", id))
            .doOnError(err -> log.error("Error obteniendo venta {}: {}", id, err.getMessage()));
    }
}
