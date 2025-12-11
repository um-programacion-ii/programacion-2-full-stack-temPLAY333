package com.example.demo.service;

import com.example.demo.service.dto.AsientoRedisDTO;
import com.example.demo.service.dto.MapaAsientosDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Servicio para obtener el estado de los asientos desde el Proxy.
 * El Proxy lee Redis de la Cátedra que contiene los asientos bloqueados/vendidos.
 */
@Service
public class AsientoService {

    private static final Logger log = LoggerFactory.getLogger(AsientoService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.proxy.base-url:http://localhost:8080}")
    private String proxyBaseUrl;

    public AsientoService(ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    /**
     * Obtiene el estado de los asientos de un evento desde el Proxy.
     * Solo devuelve asientos bloqueados o vendidos.
     * Los asientos NO presentes en la lista se consideran disponibles.
     *
     * @param eventoId ID del evento
     * @param filas Total de filas del evento
     * @param columnas Total de columnas del evento
     * @return Mapa de asientos con estado
     */
    public MapaAsientosDTO obtenerEstadoAsientos(Long eventoId, Integer filas, Integer columnas) {
        log.debug("Obteniendo estado de asientos para evento {} desde Proxy", eventoId);

        try {
            // Llamar al Proxy para obtener datos de Redis
            String url = proxyBaseUrl + "/api/eventos/" + eventoId + "/asientos-estado";
            String jsonResponse = restTemplate.getForObject(url, String.class);

            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                log.warn("No se obtuvieron datos de asientos para evento {}", eventoId);
                return crearMapaVacio(eventoId, filas, columnas);
            }

            // Parsear respuesta JSON
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            MapaAsientosDTO mapa = new MapaAsientosDTO();
            mapa.setEventoId(eventoId);
            mapa.setTotalFilas(filas);
            mapa.setTotalColumnas(columnas);

            List<AsientoRedisDTO> asientos = new ArrayList<>();

            // Verificar si es un objeto con campo "asientos" o un array directo
            JsonNode asientosNode = rootNode.has("asientos") ? rootNode.get("asientos") : rootNode;

            if (asientosNode.isArray()) {
                for (JsonNode asientoNode : asientosNode) {
                    AsientoRedisDTO asiento = new AsientoRedisDTO();
                    asiento.setFila(asientoNode.get("fila").asInt());
                    asiento.setColumna(asientoNode.get("columna").asInt());
                    asiento.setEstado(asientoNode.get("estado").asText());

                    // Si tiene fecha de expiración (solo bloqueados)
                    if (asientoNode.has("expira") && !asientoNode.get("expira").isNull()) {
                        String expiraStr = asientoNode.get("expira").asText();
                        asiento.setExpira(Instant.parse(expiraStr));
                    }

                    asientos.add(asiento);
                }
            }

            mapa.setAsientos(asientos);

            log.debug("Se obtuvieron {} asientos bloqueados/vendidos para evento {}", asientos.size(), eventoId);

            return mapa;
        } catch (Exception e) {
            log.error("Error al obtener estado de asientos para evento {} desde Proxy", eventoId, e);
            return crearMapaVacio(eventoId, filas, columnas);
        }
    }

    /**
     * Verifica si un asiento está disponible (no bloqueado ni vendido).
     *
     * @param mapa Mapa de asientos
     * @param fila Fila del asiento
     * @param columna Columna del asiento
     * @return true si está disponible
     */
    public boolean isAsientoDisponible(MapaAsientosDTO mapa, int fila, int columna) {
        Instant ahora = Instant.now();

        for (AsientoRedisDTO asiento : mapa.getAsientos()) {
            if (asiento.getFila() == fila && asiento.getColumna() == columna) {
                String estado = asiento.getEstado();

                // Si está vendido, no está disponible
                if ("Vendido".equalsIgnoreCase(estado)) {
                    return false;
                }

                // Si está bloqueado, verificar si expiró
                if ("Bloqueado".equalsIgnoreCase(estado)) {
                    Instant expira = asiento.getExpira();
                    if (expira != null && ahora.isBefore(expira)) {
                        // Bloqueado y NO expirado
                        return false;
                    }
                    // Bloqueado pero expirado → disponible
                    return true;
                }
            }
        }

        // No está en la lista → disponible
        return true;
    }

    private MapaAsientosDTO crearMapaVacio(Long eventoId, Integer filas, Integer columnas) {
        MapaAsientosDTO mapa = new MapaAsientosDTO();
        mapa.setEventoId(eventoId);
        mapa.setTotalFilas(filas);
        mapa.setTotalColumnas(columnas);
        mapa.setAsientos(new ArrayList<>());
        return mapa;
    }
}

