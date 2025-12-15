package com.example.demo.web.rest;

import com.example.demo.service.EventoSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller para forzar sincronización de eventos desde la Cátedra.
 * Útil para testing y debugging.
 */
@RestController
@RequestMapping("/api/eventos-sync")
public class EventoSyncResource {

    private static final Logger log = LoggerFactory.getLogger(EventoSyncResource.class);

    private final EventoSyncService eventoSyncService;

    public EventoSyncResource(EventoSyncService eventoSyncService) {
        this.eventoSyncService = eventoSyncService;
    }

    /**
     * POST /api/eventos-sync/manual : Sincroniza todos los eventos manualmente.
     */
    @PostMapping("/manual")
    public ResponseEntity<String> syncEventsManually() {
        log.info("Solicitud de sincronización manual de eventos");
        try {
            eventoSyncService.syncEventsFromCatedra();
            return ResponseEntity.ok("Sincronización completada exitosamente");
        } catch (Exception e) {
            log.error("Error en sincronización manual", e);
            return ResponseEntity.internalServerError()
                .body("Error en sincronización: " + e.getMessage());
        }
    }

    /**
     * POST /api/eventos-sync/evento/{id} : Sincroniza un evento específico.
     */
    @PostMapping("/evento/{id}")
    public ResponseEntity<String> syncEventById(@PathVariable Long id) {
        log.info("Solicitud de sincronización de evento {}", id);
        try {
            eventoSyncService.syncEventoById(id);
            return ResponseEntity.ok("Evento " + id + " sincronizado exitosamente");
        } catch (Exception e) {
            log.error("Error en sincronización de evento {}", id, e);
            return ResponseEntity.internalServerError()
                .body("Error en sincronización: " + e.getMessage());
        }
    }

    /**
     * GET /api/eventos-sync/status : Obtiene información del estado de sincronización.
     */
    @GetMapping("/status")
    public ResponseEntity<SyncStatus> getSyncStatus() {
        log.debug("Consulta de estado de sincronización");

        SyncStatus status = new SyncStatus();
        status.setMessage("Servicio de sincronización activo");
        status.setScheduleInfo("Sincronización automática cada hora (cron: 0 0 * * * *)");

        return ResponseEntity.ok(status);
    }

    // Inner class para el status
    public static class SyncStatus {
        private String message;
        private String scheduleInfo;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getScheduleInfo() {
            return scheduleInfo;
        }

        public void setScheduleInfo(String scheduleInfo) {
            this.scheduleInfo = scheduleInfo;
        }
    }
}

