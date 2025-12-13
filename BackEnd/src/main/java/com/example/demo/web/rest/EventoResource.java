package com.example.demo.web.rest;

import com.example.demo.repository.EventoRepository;
import com.example.demo.service.EventoService;
import com.example.demo.service.dto.EventoDTO;
import com.example.demo.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.example.demo.domain.Evento}.
 */
@RestController
@RequestMapping("/api/eventos")
public class EventoResource {

    private static final Logger LOG = LoggerFactory.getLogger(EventoResource.class);

    private static final String ENTITY_NAME = "evento";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final EventoService eventoService;

    private final EventoRepository eventoRepository;

    public EventoResource(EventoService eventoService, EventoRepository eventoRepository) {
        this.eventoService = eventoService;
        this.eventoRepository = eventoRepository;
    }

    /**
     * {@code POST  /eventos} : Create a new evento.
     *
     * @param eventoDTO the eventoDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new eventoDTO, or with status {@code 400 (Bad Request)} if the evento has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<EventoDTO> createEvento(@Valid @RequestBody EventoDTO eventoDTO) throws URISyntaxException {
        LOG.debug("REST request to save Evento : {}", eventoDTO);
        if (eventoDTO.getId() != null) {
            throw new BadRequestAlertException("A new evento cannot already have an ID", ENTITY_NAME, "idexists");
        }
        eventoDTO = eventoService.save(eventoDTO);
        return ResponseEntity.created(new URI("/api/eventos/" + eventoDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, eventoDTO.getId().toString()))
            .body(eventoDTO);
    }

    /**
     * {@code PUT  /eventos/:id} : Updates an existing evento.
     *
     * @param id the id of the eventoDTO to save.
     * @param eventoDTO the eventoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eventoDTO,
     * or with status {@code 400 (Bad Request)} if the eventoDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the eventoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<EventoDTO> updateEvento(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody EventoDTO eventoDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Evento : {}, {}", id, eventoDTO);
        if (eventoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eventoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eventoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        eventoDTO = eventoService.update(eventoDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, eventoDTO.getId().toString()))
            .body(eventoDTO);
    }

    /**
     * {@code PATCH  /eventos/:id} : Partial updates given fields of an existing evento, field will ignore if it is null
     *
     * @param id the id of the eventoDTO to save.
     * @param eventoDTO the eventoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eventoDTO,
     * or with status {@code 400 (Bad Request)} if the eventoDTO is not valid,
     * or with status {@code 404 (Not Found)} if the eventoDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the eventoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<EventoDTO> partialUpdateEvento(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody EventoDTO eventoDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Evento partially : {}, {}", id, eventoDTO);
        if (eventoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eventoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eventoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<EventoDTO> result = eventoService.partialUpdate(eventoDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, eventoDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /eventos} : get all the eventos.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of eventos in body.
     */
    @GetMapping("")
    public ResponseEntity<List<EventoDTO>> getAllEventos(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of Eventos");
        Page<EventoDTO> page;
        if (eagerload) {
            page = eventoService.findAllWithEagerRelationships(pageable);
        } else {
            page = eventoService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /eventos/:id} : get the "id" evento.
     *
     * @param id the id of the eventoDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the eventoDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventoDTO> getEvento(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Evento : {}", id);
        Optional<EventoDTO> eventoDTO = eventoService.findOne(id);
        return ResponseUtil.wrapOrNotFound(eventoDTO);
    }

    /**
     * {@code DELETE  /eventos/:id} : delete the "id" evento.
     *
     * @param id the id of the eventoDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvento(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Evento : {}", id);
        eventoService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
