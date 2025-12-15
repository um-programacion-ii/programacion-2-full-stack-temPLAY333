package com.example.demo.web.rest;

import com.example.demo.domain.Evento;
import com.example.demo.repository.EventoRepository;
import com.example.demo.service.EventoService;
import com.example.demo.service.EventoSyncService;
import com.example.demo.service.dto.EventoDTO;
import com.example.demo.service.dto.EventoResumenDTO;
import com.example.demo.service.mapper.EventoResumenMapper;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

/**
 * Endpoints públicos de consulta de eventos (para Mobile/Frontend).
 * Siempre leen desde la BD local (sin llamar directamente a la cátedra),
 * asumiendo que un proceso de sincronización mantiene los datos actualizados.
 *
 * Paginación opcional: si se proporciona Pageable (query params: ?page=0&size=20), se usa paginación;
 * si no se proporciona, se devuelven todos los eventos.
 */
@RestController
@RequestMapping("/api/eventos")
public class EventoConsultaResource {

    private static final Logger log = LoggerFactory.getLogger(EventoConsultaResource.class);

    private final EventoSyncService eventoSyncService;
    private final EventoService eventoService;
    private final EventoRepository eventoRepository;
    private final EventoResumenMapper eventoResumenMapper;

    public EventoConsultaResource(
        EventoSyncService eventoSyncService,
        EventoService eventoService,
        EventoRepository eventoRepository,
        EventoResumenMapper eventoResumenMapper
    ) {
        this.eventoSyncService = eventoSyncService;
        this.eventoService = eventoService;
        this.eventoRepository = eventoRepository;
        this.eventoResumenMapper = eventoResumenMapper;
    }

    /**
     * Lista resumida de eventos.
     * Paginación opcional: agregar ?page=0&size=20 para paginar, o omitir para obtener todos.
     *
     * Ejemplos:
     * - GET /api/eventos/resumidos → todos los eventos resumidos
     * - GET /api/eventos/resumidos?page=0&size=10 → primera página con 10 eventos
     */
    @GetMapping("/resumidos")
    @Transactional(readOnly = true)
    public ResponseEntity<List<EventoResumenDTO>> listarResumidos(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get resumidos eventos (BD local), paginado: {}", pageable != null && pageable.isPaged());

        if (pageable != null && pageable.isPaged()) {
            // Con paginación
            Page<Evento> eventosPage = eventoRepository.findAllWithEagerRelationships(pageable);
            eventosPage.getContent().forEach(evento -> {
                if (evento.getEventoTipo() != null) {
                    evento.getEventoTipo().getNombre();
                }
            });
            Page<EventoResumenDTO> page = eventosPage.map(eventoResumenMapper::toDto);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            return ResponseEntity.ok().headers(headers).body(page.getContent());
        } else {
            // Sin paginación - devolver todos
            return ResponseEntity.ok(eventoSyncService.obtenerEventosResumidos());
        }
    }

    /**
     * Lista completa de eventos.
     * Paginación opcional: agregar ?page=0&size=20 para paginar, o omitir para obtener todos.
     *
     * Ejemplos:
     * - GET /api/eventos → todos los eventos completos
     * - GET /api/eventos?page=0&size=10 → primera página con 10 eventos
     */
    @GetMapping("")
    @Transactional(readOnly = true)
    public ResponseEntity<List<EventoDTO>> listarCompletos(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get completos eventos (BD local), paginado: {}", pageable != null && pageable.isPaged());

        if (pageable != null && pageable.isPaged()) {
            // Con paginación
            Page<EventoDTO> page = eventoService.findAllWithEagerRelationships(pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            return ResponseEntity.ok().headers(headers).body(page.getContent());
        } else {
            // Sin paginación - devolver todos
            return ResponseEntity.ok(eventoSyncService.obtenerEventosCompletos());
        }
    }

    /**
     * Detalle de un único evento.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventoDTO> obtenerDetalle(@PathVariable Long id) {
        log.debug("REST request to get detalle evento {} (BD local)", id);
        return eventoSyncService.obtenerEventoPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Forzar sincronización manual de eventos desde la Cátedra.
     * Ejecuta de forma asíncrona para no bloquear la respuesta.
     */
    @PostMapping("/sync")
    public ResponseEntity<Void> syncEventos() {
        log.debug("REST request to manually sync Eventos from Cátedra");
        new Thread(() -> eventoSyncService.syncEventsFromCatedra()).start();
        return ResponseEntity.accepted().build();
    }

    /**
     * Forzar sincronización de un evento específico desde la Cátedra.
     * Ejecuta de forma asíncrona para no bloquear la respuesta.
     */
    @PostMapping("/{id}/sync")
    public ResponseEntity<Void> syncEvento(@PathVariable Long id) {
        log.debug("REST request to manually sync Evento {} from Cátedra", id);
        new Thread(() -> eventoSyncService.syncEventoById(id)).start();
        return ResponseEntity.accepted().build();
    }
}

