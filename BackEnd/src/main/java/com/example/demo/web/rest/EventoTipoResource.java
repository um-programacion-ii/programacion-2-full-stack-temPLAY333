package com.example.demo.web.rest;

import com.example.demo.repository.EventoTipoRepository;
import com.example.demo.service.EventoTipoService;
import com.example.demo.service.dto.EventoTipoDTO;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.example.demo.domain.EventoTipo}.
 */
@RestController
@RequestMapping("/api/evento-tipos")
public class EventoTipoResource {

    private static final Logger LOG = LoggerFactory.getLogger(EventoTipoResource.class);

    private static final String ENTITY_NAME = "eventoTipo";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final EventoTipoService eventoTipoService;

    private final EventoTipoRepository eventoTipoRepository;

    public EventoTipoResource(EventoTipoService eventoTipoService, EventoTipoRepository eventoTipoRepository) {
        this.eventoTipoService = eventoTipoService;
        this.eventoTipoRepository = eventoTipoRepository;
    }

    /**
     * {@code POST  /evento-tipos} : Create a new eventoTipo.
     *
     * @param eventoTipoDTO the eventoTipoDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new eventoTipoDTO, or with status {@code 400 (Bad Request)} if the eventoTipo has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<EventoTipoDTO> createEventoTipo(@Valid @RequestBody EventoTipoDTO eventoTipoDTO) throws URISyntaxException {
        LOG.debug("REST request to save EventoTipo : {}", eventoTipoDTO);
        if (eventoTipoDTO.getId() != null) {
            throw new BadRequestAlertException("A new eventoTipo cannot already have an ID", ENTITY_NAME, "idexists");
        }
        eventoTipoDTO = eventoTipoService.save(eventoTipoDTO);
        return ResponseEntity.created(new URI("/api/evento-tipos/" + eventoTipoDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, eventoTipoDTO.getId().toString()))
            .body(eventoTipoDTO);
    }

    /**
     * {@code PUT  /evento-tipos/:id} : Updates an existing eventoTipo.
     *
     * @param id the id of the eventoTipoDTO to save.
     * @param eventoTipoDTO the eventoTipoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eventoTipoDTO,
     * or with status {@code 400 (Bad Request)} if the eventoTipoDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the eventoTipoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<EventoTipoDTO> updateEventoTipo(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody EventoTipoDTO eventoTipoDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update EventoTipo : {}, {}", id, eventoTipoDTO);
        if (eventoTipoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eventoTipoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eventoTipoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        eventoTipoDTO = eventoTipoService.update(eventoTipoDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, eventoTipoDTO.getId().toString()))
            .body(eventoTipoDTO);
    }

    /**
     * {@code PATCH  /evento-tipos/:id} : Partial updates given fields of an existing eventoTipo, field will ignore if it is null
     *
     * @param id the id of the eventoTipoDTO to save.
     * @param eventoTipoDTO the eventoTipoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eventoTipoDTO,
     * or with status {@code 400 (Bad Request)} if the eventoTipoDTO is not valid,
     * or with status {@code 404 (Not Found)} if the eventoTipoDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the eventoTipoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<EventoTipoDTO> partialUpdateEventoTipo(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody EventoTipoDTO eventoTipoDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update EventoTipo partially : {}, {}", id, eventoTipoDTO);
        if (eventoTipoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eventoTipoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eventoTipoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<EventoTipoDTO> result = eventoTipoService.partialUpdate(eventoTipoDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, eventoTipoDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /evento-tipos} : get all the eventoTipos.
     *
     * @param filter the filter of the request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of eventoTipos in body.
     */
    @GetMapping("")
    public List<EventoTipoDTO> getAllEventoTipos(@RequestParam(name = "filter", required = false) String filter) {
        if ("evento-is-null".equals(filter)) {
            LOG.debug("REST request to get all EventoTipos where evento is null");
            return eventoTipoService.findAllWhereEventoIsNull();
        }
        LOG.debug("REST request to get all EventoTipos");
        return eventoTipoService.findAll();
    }

    /**
     * {@code GET  /evento-tipos/:id} : get the "id" eventoTipo.
     *
     * @param id the id of the eventoTipoDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the eventoTipoDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventoTipoDTO> getEventoTipo(@PathVariable("id") Long id) {
        LOG.debug("REST request to get EventoTipo : {}", id);
        Optional<EventoTipoDTO> eventoTipoDTO = eventoTipoService.findOne(id);
        return ResponseUtil.wrapOrNotFound(eventoTipoDTO);
    }

    /**
     * {@code DELETE  /evento-tipos/:id} : delete the "id" eventoTipo.
     *
     * @param id the id of the eventoTipoDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEventoTipo(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete EventoTipo : {}", id);
        eventoTipoService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
