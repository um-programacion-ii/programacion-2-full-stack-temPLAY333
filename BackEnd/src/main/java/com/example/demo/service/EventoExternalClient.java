package com.example.demo.service;

import com.example.demo.service.dto.EventoDTO;
import com.example.demo.service.dto.EventoResumenDTO;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Cliente simple para consumir el servicio proxy de eventos y obtener datos de la cátedra.
 *
 * Este backend NUNCA llama directo a la cátedra, siempre pasa por el proxy
 * (por ejemplo: GET http://backend/proxy/eventos/resumidos).
 */
@Service
public class EventoExternalClient {

    private static final Logger log = LoggerFactory.getLogger(EventoExternalClient.class);

    private final RestTemplate restTemplate;

    /**
     * Base URL del backend-proxy. Normalmente será este mismo backend,
     * por ejemplo: http://localhost:8080
     */
    @Value("${app.proxy.base-url:http://localhost:8080}")
    private String proxyBaseUrl;

    public EventoExternalClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Lista eventos resumidos desde el proxy.
     */
    public List<EventoResumenDTO> listarEventosResumidos() {
        String url = proxyBaseUrl + "/proxy/eventos/resumidos";
        log.debug("Llamando al proxy para listar eventos resumidos: {}", url);
        EventoResumenDTO[] array = restTemplate.getForObject(url, EventoResumenDTO[].class);
        return array != null ? Arrays.asList(array) : List.of();
    }

    /**
     * Lista eventos completos desde el proxy.
     */
    public List<EventoDTO> listarEventosCompletos() {
        String url = proxyBaseUrl + "/proxy/eventos";
        log.debug("Llamando al proxy para listar eventos completos: {}", url);
        EventoDTO[] array = restTemplate.getForObject(url, EventoDTO[].class);
        return array != null ? Arrays.asList(array) : List.of();
    }
}
