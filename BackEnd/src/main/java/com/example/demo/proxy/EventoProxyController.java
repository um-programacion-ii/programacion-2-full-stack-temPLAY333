package com.example.demo.proxy;

import com.example.demo.service.dto.AsientoEstadoDTO;
import com.example.demo.service.dto.BloquearAsientosRequestDTO;
import com.example.demo.service.dto.BloquearAsientosResponseDTO;
import com.example.demo.service.dto.EventoDTO;
import com.example.demo.service.dto.EventoDetalleDTO;
import com.example.demo.service.dto.EventoResumenDTO;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * Proxy REST para eventos contra la Cátedra.
 */
@RestController
@RequestMapping("/proxy/eventos")
public class EventoProxyController {

    private static final Logger log = LoggerFactory.getLogger(EventoProxyController.class);

    private final RestTemplate restTemplate;
    private final NotificadorBackendService notificadorBackendService;
    private final AsientoRedisService asientoRedisService;

    @Value("${app.catedra.base-url:http://192.168.194.250:8080}")
    private String catedraBaseUrl;

    public EventoProxyController(NotificadorBackendService notificadorBackendService, AsientoRedisService asientoRedisService) {
        this.restTemplate = new RestTemplate();
        this.notificadorBackendService = notificadorBackendService;
        this.asientoRedisService = asientoRedisService;
    }

    @GetMapping("/resumidos")
    public ResponseEntity<List<EventoResumenDTO>> listarEventosResumidos() {
        log.debug("Proxy listar eventos resumidos contra Cátedra");
        String url = catedraBaseUrl + "/api/endpoints/v1/eventos-resumidos";
        EventoResumenDTO[] array = restTemplate.getForObject(url, EventoResumenDTO[].class);
        List<EventoResumenDTO> lista = array != null ? Arrays.asList(array) : List.of();
        return ResponseEntity.ok(lista);
    }

    @GetMapping
    public ResponseEntity<List<EventoDTO>> listarEventos() {
        log.debug("Proxy listar eventos completos contra Cátedra");
        String url = catedraBaseUrl + "/api/endpoints/v1/eventos";
        EventoDTO[] array = restTemplate.getForObject(url, EventoDTO[].class);
        List<EventoDTO> lista = array != null ? Arrays.asList(array) : List.of();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoDetalleDTO> obtenerEvento(@PathVariable Long id) {
        log.debug("Proxy obtener evento {} contra Cátedra", id);
        String url = catedraBaseUrl + "/api/endpoints/v1/evento/" + id;
        EventoDetalleDTO evento = restTemplate.getForObject(url, EventoDetalleDTO.class);
        return ResponseEntity.ok(evento);
    }

    @PostMapping("/bloquear-asientos")
    public ResponseEntity<BloquearAsientosResponseDTO> bloquearAsientos(@RequestBody BloquearAsientosRequestDTO request) {
        log.debug("Proxy bloquear asientos contra Cátedra para evento {}", request.getEventoId());

        String url = catedraBaseUrl + "/api/endpoints/v1/bloquear-asientos";
        BloquearAsientosResponseDTO response = restTemplate.postForObject(url, request, BloquearAsientosResponseDTO.class);

        BackendNotificacionDTO notificacion = new BackendNotificacionDTO();
        notificacion.setTopic("BLOQUEO_ASIENTOS");
        notificacion.setPayload("eventoId=" + request.getEventoId());
        notificadorBackendService.notificar(notificacion);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/asientos-estado")
    public ResponseEntity<List<AsientoEstadoDTO>> obtenerEstadoAsientos(@PathVariable Long id) {
        log.debug("Proxy obtener estado de asientos para evento {} desde Redis", id);
        return ResponseEntity.ok(asientoRedisService.obtenerEstadoAsientos(id));
    }
}
