package com.example.demo.web.rest;

import com.example.demo.service.EventoSyncService;
import com.example.demo.service.dto.EventoDTO;
import com.example.demo.service.dto.EventoResumenDTO;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de consulta de eventos para el cliente móvil.
 * Siempre leen desde la BD local (sin llamar directamente a la cátedra),
 * asumiendo que un proceso de sincronización mantiene los datos actualizados.
 */
@RestController
@RequestMapping("/api/app/eventos")
public class EventoConsultaResource {

    private static final Logger log = LoggerFactory.getLogger(EventoConsultaResource.class);

    private final EventoSyncService eventoSyncService;

    public EventoConsultaResource(EventoSyncService eventoSyncService) {
        this.eventoSyncService = eventoSyncService;
    }

    /**
     * Lista resumida de eventos para el móvil.
     */
    @GetMapping("/resumidos")
    public ResponseEntity<List<EventoResumenDTO>> listarResumidos() {
        log.debug("REST request to get resumidos eventos (BD local)");
        return ResponseEntity.ok(eventoSyncService.obtenerEventosResumidos());
    }

    /**
     * Lista completa de eventos para el móvil.
     */
    @GetMapping("")
    public ResponseEntity<List<EventoDTO>> listarCompletos() {
        log.debug("REST request to get completos eventos (BD local)");
        return ResponseEntity.ok(eventoSyncService.obtenerEventosCompletos());
    }

    /**
     * Detalle de un único evento.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventoDTO> obtenerDetalle(@PathVariable Long id) {
        log.debug("REST request to get detalle evento {} (BD local)", id);
        return eventoSyncService.obtenerEventoPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}

