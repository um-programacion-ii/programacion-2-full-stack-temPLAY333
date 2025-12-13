package com.example.demo.service;

import com.example.demo.service.dto.AsientoSeleccionDTO;
import com.example.demo.service.dto.AsientoPosicionDTO;
import com.example.demo.service.dto.BloquearAsientosRequestDTO;
import com.example.demo.service.dto.BloquearAsientosResponseDTO;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * Servicio para bloquear asientos temporalmente (5 minutos) a través del Proxy.
 */
@Service
@Transactional
public class BloqueoAsientoService {

    private static final Logger log = LoggerFactory.getLogger(BloqueoAsientoService.class);

    private final RestTemplate restTemplate;

    @Value("${app.proxy.base-url:http://localhost:8080}")
    private String proxyBaseUrl;

    public BloqueoAsientoService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Bloquea asientos para un evento específico.
     * El bloqueo dura 5 minutos.
     *
     * @param eventoId ID del evento
     * @param asientos Lista de asientos a bloquear
     * @return Respuesta del bloqueo
     */
    public BloquearAsientosResponseDTO bloquearAsientos(Long eventoId, List<AsientoSeleccionDTO> asientos) {
        log.info("Bloqueando {} asientos para evento {}", asientos.size(), eventoId);

        try {
            // Crear request DTO
            BloquearAsientosRequestDTO request = new BloquearAsientosRequestDTO();
            request.setEventoId(eventoId);

            // Convertir AsientoSeleccionDTO a AsientoPosicionDTO
            List<AsientoPosicionDTO> asientosPosicion = asientos.stream()
                .map(a -> {
                    AsientoPosicionDTO dto = new AsientoPosicionDTO();
                    dto.setFila(a.getFila());
                    dto.setColumna(a.getColumna());
                    return dto;
                })
                .collect(Collectors.toList());

            request.setAsientos(asientosPosicion);

            // Llamar al Proxy
            String url = proxyBaseUrl + "/api/eventos/" + eventoId + "/bloquear-asientos";
            ResponseEntity<BloquearAsientosResponseDTO> responseEntity = restTemplate.postForEntity(
                url,
                request,
                BloquearAsientosResponseDTO.class
            );

            BloquearAsientosResponseDTO response = responseEntity.getBody();

            if (response != null && Boolean.TRUE.equals(response.getResultado())) {
                log.info("Asientos bloqueados exitosamente para evento {}", eventoId);
            } else {
                log.warn("Bloqueo de asientos para evento {} no fue exitoso: {}", eventoId, response != null ? response.getDescripcion() : "Sin respuesta");
            }

            return response;
        } catch (Exception e) {
            log.error("Error al bloquear asientos para evento {}", eventoId, e);

            // Retornar respuesta de error
            BloquearAsientosResponseDTO errorResponse = new BloquearAsientosResponseDTO();
            errorResponse.setResultado(false);
            errorResponse.setDescripcion("Error al comunicarse con el servicio: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Valida que los asientos seleccionados sean válidos.
     *
     * @param asientos Lista de asientos
     * @param maxFilas Máximo de filas del evento
     * @param maxColumnas Máximo de columnas del evento
     * @return true si todos son válidos
     */
    public boolean validarAsientos(List<AsientoSeleccionDTO> asientos, int maxFilas, int maxColumnas) {
        if (asientos == null || asientos.isEmpty()) {
            log.warn("Lista de asientos vacía");
            return false;
        }

        if (asientos.size() > 4) {
            log.warn("No se pueden seleccionar más de 4 asientos");
            return false;
        }

        for (AsientoSeleccionDTO asiento : asientos) {
            Integer fila = asiento.getFila();
            Integer columna = asiento.getColumna();

            if (fila == null || columna == null) {
                log.warn("Asiento con fila o columna nula");
                return false;
            }

            if (fila <= 0 || columna <= 0) {
                log.warn("Fila o columna con valor inválido: fila={}, columna={}", fila, columna);
                return false;
            }

            if (fila > maxFilas || columna > maxColumnas) {
                log.warn("Asiento fuera de rango: fila={}/{}, columna={}/{}", fila, maxFilas, columna, maxColumnas);
                return false;
            }
        }

        return true;
    }
}

