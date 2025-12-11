package com.example.demo.web.rest;

import com.example.demo.repository.EventoRepository;
import com.example.demo.service.EventoService;
import com.example.demo.service.EventoSyncService;
import com.example.demo.service.dto.EventoDTO;
import com.example.demo.service.dto.EventoResumenDTO;
import com.example.demo.service.mapper.EventoMapper;
import com.example.demo.service.mapper.EventoResumenMapper;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

/**
 * REST controller para servir eventos al móvil desde la base de datos local.
 * NO usa el proxy, lee directamente de la BD sincronizada.
 */
@RestController
@RequestMapping("/api/mobile")
public class EventoMobileResource {

    private static final Logger log = LoggerFactory.getLogger(EventoMobileResource.class);

    private final EventoService eventoService;
    private final EventoRepository eventoRepository;
    private final EventoResumenMapper eventoResumenMapper;
    private final EventoSyncService eventoSyncService;

    public EventoMobileResource(
        EventoService eventoService,
        EventoRepository eventoRepository,
        EventoResumenMapper eventoResumenMapper,
        EventoSyncService eventoSyncService
    ) {
        this.eventoService = eventoService;
        this.eventoRepository = eventoRepository;
        this.eventoResumenMapper = eventoResumenMapper;
        this.eventoSyncService = eventoSyncService;
    }

    /**
     * {@code GET  /mobile/eventos/resumidos} : Obtener todos los eventos resumidos desde BD local.
     *
     * @param pageable the pagination information.
     * @return la lista de eventos resumidos con paginación.
     */
    @GetMapping("/eventos/resumidos")
    public ResponseEntity<List<EventoResumenDTO>> getAllEventosResumidos(Pageable pageable) {
        log.debug("REST request to get a page of Eventos resumidos from local DB");

        Page<EventoResumenDTO> page = eventoRepository
            .findAllWithEagerRelationships(pageable)
            .map(eventoResumenMapper::toDto);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /mobile/eventos} : Obtener todos los eventos completos desde BD local.
     *
     * @param pageable the pagination information.
     * @return la lista de eventos completos con paginación.
     */
    @GetMapping("/eventos")
    public ResponseEntity<List<EventoDTO>> getAllEventos(Pageable pageable) {
        log.debug("REST request to get a page of Eventos from local DB");

        Page<EventoDTO> page = eventoService.findAllWithEagerRelationships(pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /mobile/eventos/:id} : Obtener un evento específico desde BD local.
     *
     * @param id el id del evento.
     * @return el evento con status 200 (OK) o 404 (Not Found).
     */
    @GetMapping("/eventos/{id}")
    public ResponseEntity<EventoDTO> getEvento(@PathVariable("id") Long id) {
        log.debug("REST request to get Evento {} from local DB", id);

        Optional<EventoDTO> eventoDTO = eventoService.findOne(id);
        return eventoDTO.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@code POST  /mobile/eventos/sync} : Forzar sincronización manual de eventos.
     *
     * @return status 202 (Accepted).
     */
    @PostMapping("/eventos/sync")
    public ResponseEntity<Void> syncEventos() {
        log.debug("REST request to manually sync Eventos from Cátedra");

        // Ejecutar sincronización de forma asíncrona para no bloquear
        new Thread(() -> eventoSyncService.syncEventsFromCatedra()).start();

        return ResponseEntity.accepted().build();
    }

    /**
     * {@code POST  /mobile/eventos/{id}/sync} : Forzar sincronización de un evento específico.
     *
     * @param id el id del evento a sincronizar.
     * @return status 202 (Accepted).
     */
    @PostMapping("/eventos/{id}/sync")
    public ResponseEntity<Void> syncEvento(@PathVariable("id") Long id) {
        log.debug("REST request to manually sync Evento {} from Cátedra", id);

        // Ejecutar sincronización de forma asíncrona
        new Thread(() -> eventoSyncService.syncEventoById(id)).start();

        return ResponseEntity.accepted().build();
    }
}

