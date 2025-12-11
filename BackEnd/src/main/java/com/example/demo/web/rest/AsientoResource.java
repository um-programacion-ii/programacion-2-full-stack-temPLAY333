package com.example.demo.web.rest;

import com.example.demo.domain.Evento;
import com.example.demo.repository.EventoRepository;
import com.example.demo.service.AsientoService;
import com.example.demo.service.BloqueoAsientoService;
import com.example.demo.service.dto.AsientoSeleccionDTO;
import com.example.demo.service.dto.BloquearAsientosResponseDTO;
import com.example.demo.service.dto.MapaAsientosDTO;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller para operaciones con asientos.
 */
@RestController
@RequestMapping("/api/asientos")
public class AsientoResource {

    private static final Logger log = LoggerFactory.getLogger(AsientoResource.class);

    private final AsientoService asientoService;
    private final BloqueoAsientoService bloqueoAsientoService;
    private final EventoRepository eventoRepository;

    public AsientoResource(
        AsientoService asientoService,
        BloqueoAsientoService bloqueoAsientoService,
        EventoRepository eventoRepository
    ) {
        this.asientoService = asientoService;
        this.bloqueoAsientoService = bloqueoAsientoService;
        this.eventoRepository = eventoRepository;
    }

    /**
     * GET /api/asientos/evento/{eventoId}/mapa : Obtiene el mapa de asientos de un evento.
     * Solo devuelve asientos bloqueados o vendidos.
     * Los asientos NO presentes se consideran disponibles.
     *
     * @param eventoId ID del evento
     * @return Mapa de asientos con estado
     */
    @GetMapping("/evento/{eventoId}/mapa")
    public ResponseEntity<MapaAsientosDTO> obtenerMapaAsientos(@PathVariable Long eventoId) {
        log.debug("REST request para obtener mapa de asientos del evento {}", eventoId);

        // Obtener información del evento desde la BD local
        Evento evento = eventoRepository.findById(eventoId)
            .orElseThrow(() -> new RuntimeException("Evento no encontrado: " + eventoId));

        // Obtener estado de asientos desde el Proxy (que lee Redis de la Cátedra)
        MapaAsientosDTO mapa = asientoService.obtenerEstadoAsientos(
            eventoId,
            evento.getFilaAsientos(),
            evento.getColumnAsientos()
        );

        return ResponseEntity.ok(mapa);
    }

    /**
     * POST /api/asientos/evento/{eventoId}/bloquear : Bloquea asientos temporalmente (5 minutos).
     *
     * @param eventoId ID del evento
     * @param asientos Lista de asientos a bloquear (máximo 4)
     * @return Respuesta del bloqueo
     */
    @PostMapping("/evento/{eventoId}/bloquear")
    public ResponseEntity<BloquearAsientosResponseDTO> bloquearAsientos(
        @PathVariable Long eventoId,
        @RequestBody List<AsientoSeleccionDTO> asientos
    ) {
        log.debug("REST request para bloquear {} asientos del evento {}", asientos.size(), eventoId);

        // Validar que el evento exista
        Evento evento = eventoRepository.findById(eventoId)
            .orElseThrow(() -> new RuntimeException("Evento no encontrado: " + eventoId));

        // Validar asientos
        boolean validos = bloqueoAsientoService.validarAsientos(
            asientos,
            evento.getFilaAsientos(),
            evento.getColumnAsientos()
        );

        if (!validos) {
            BloquearAsientosResponseDTO errorResponse = new BloquearAsientosResponseDTO();
            errorResponse.setResultado(false);
            errorResponse.setDescripcion("Asientos inválidos. Verifica fila/columna y que no excedan el límite (máx 4).");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Bloquear asientos a través del Proxy
        BloquearAsientosResponseDTO response = bloqueoAsientoService.bloquearAsientos(eventoId, asientos);

        if (Boolean.TRUE.equals(response.getResultado())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * GET /api/asientos/evento/{eventoId}/disponible : Verifica si un asiento está disponible.
     *
     * @param eventoId ID del evento
     * @param fila Fila del asiento
     * @param columna Columna del asiento
     * @return true si está disponible
     */
    @GetMapping("/evento/{eventoId}/disponible")
    public ResponseEntity<Boolean> verificarDisponibilidad(
        @PathVariable Long eventoId,
        @RequestParam Integer fila,
        @RequestParam Integer columna
    ) {
        log.debug("REST request para verificar disponibilidad del asiento ({},{}) del evento {}", fila, columna, eventoId);

        Evento evento = eventoRepository.findById(eventoId)
            .orElseThrow(() -> new RuntimeException("Evento no encontrado: " + eventoId));

        MapaAsientosDTO mapa = asientoService.obtenerEstadoAsientos(
            eventoId,
            evento.getFilaAsientos(),
            evento.getColumnAsientos()
        );

        boolean disponible = asientoService.isAsientoDisponible(mapa, fila, columna);

        return ResponseEntity.ok(disponible);
    }
}

