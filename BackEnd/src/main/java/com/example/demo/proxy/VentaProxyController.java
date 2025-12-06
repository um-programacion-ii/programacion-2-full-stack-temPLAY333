package com.example.demo.proxy;

import com.example.demo.service.dto.RealizarVentaRequestDTO;
import com.example.demo.service.dto.RealizarVentaResponseDTO;
import com.example.demo.service.dto.VentaDTO;
import com.example.demo.service.dto.VentaResumenDTO;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * Proxy REST para ventas contra la Cátedra.
 */
@RestController
@RequestMapping("/proxy/ventas")
public class VentaProxyController {

    private static final Logger log = LoggerFactory.getLogger(VentaProxyController.class);

    private final RestTemplate restTemplate;
    private final NotificadorBackendService notificadorBackendService;

    @Value("${app.catedra.base-url:http://192.168.194.250:8080}")
    private String catedraBaseUrl;

    public VentaProxyController(NotificadorBackendService notificadorBackendService) {
        this.restTemplate = new RestTemplate();
        this.notificadorBackendService = notificadorBackendService;
    }

    @PostMapping("/realizar")
    public ResponseEntity<RealizarVentaResponseDTO> realizarVenta(@RequestBody RealizarVentaRequestDTO request) {
        log.debug("Proxy realizar venta contra Cátedra para evento {}", request.getEventoId());

        String url = catedraBaseUrl + "/api/endpoints/v1/realizar-venta";
        RealizarVentaResponseDTO response = restTemplate.postForObject(url, request, RealizarVentaResponseDTO.class);

        BackendNotificacionDTO notificacion = new BackendNotificacionDTO();
        notificacion.setTopic("VENTA_REALIZADA");
        notificacion.setPayload("eventoId=" + request.getEventoId());
        notificadorBackendService.notificar(notificacion);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<VentaResumenDTO>> listarVentas() {
        log.debug("Proxy listar ventas contra Cátedra");
        String url = catedraBaseUrl + "/api/endpoints/v1/listar-ventas";
        VentaResumenDTO[] array = restTemplate.getForObject(url, VentaResumenDTO[].class);
        List<VentaResumenDTO> lista = array != null ? Arrays.asList(array) : List.of();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaDTO> obtenerVenta(@PathVariable Long id) {
        log.debug("Proxy obtener venta {} contra Cátedra", id);
        String url = catedraBaseUrl + "/api/endpoints/v1/listar-venta/" + id;
        VentaDTO venta = restTemplate.getForObject(url, VentaDTO.class);
        return ResponseEntity.ok(venta);
    }
}
