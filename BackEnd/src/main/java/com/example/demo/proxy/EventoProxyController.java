package com.example.demo.proxy;

import com.example.demo.service.dto.EventoDTO;
import com.example.demo.service.dto.EventoDetalleDTO;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class EventoProxyController {

    private static final Logger log = LoggerFactory.getLogger(EventoProxyController.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.proxy.base-url:http://localhost:8080}")
    private String proxyBaseUrl;

    public ResponseEntity<List<EventoDTO>> listarEventos() {
        String url = proxyBaseUrl + "/api/eventos";
        log.debug("Llamando al Proxy: {}", url);
        EventoDTO[] arr = restTemplate.getForObject(url, EventoDTO[].class);
        List<EventoDTO> list = arr == null ? List.of() : Arrays.asList(arr);
        return ResponseEntity.ok(list);
    }

    public ResponseEntity<EventoDetalleDTO> obtenerEvento(Long id) {
        String url = proxyBaseUrl + "/api/eventos/" + id;
        log.debug("Llamando al Proxy: {}", url);
        EventoDetalleDTO detalle = restTemplate.getForObject(url, EventoDetalleDTO.class);
        return ResponseEntity.ok(detalle);
    }
}

