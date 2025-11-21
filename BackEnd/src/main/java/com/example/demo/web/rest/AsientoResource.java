package com.example.demo.web.rest;

import com.example.demo.repository.AsientoRepository;
import com.example.demo.service.AsientoService;
import com.example.demo.service.dto.AsientoDTO;
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
 * REST controller for managing {@link com.example.demo.domain.Asiento}.
 */
@RestController
@RequestMapping("/api/asientos")
public class AsientoResource {

    private static final Logger LOG = LoggerFactory.getLogger(AsientoResource.class);

    private static final String ENTITY_NAME = "asiento";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AsientoService asientoService;

    private final AsientoRepository asientoRepository;

    public AsientoResource(AsientoService asientoService, AsientoRepository asientoRepository) {
        this.asientoService = asientoService;
        this.asientoRepository = asientoRepository;
    }

    /**
     * {@code POST  /asientos} : Create a new asiento.
     *
     * @param asientoDTO the asientoDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new asientoDTO, or with status {@code 400 (Bad Request)} if the asiento has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<AsientoDTO> createAsiento(@Valid @RequestBody AsientoDTO asientoDTO) throws URISyntaxException {
        LOG.debug("REST request to save Asiento : {}", asientoDTO);
        if (asientoDTO.getId() != null) {
            throw new BadRequestAlertException("A new asiento cannot already have an ID", ENTITY_NAME, "idexists");
        }
        asientoDTO = asientoService.save(asientoDTO);
        return ResponseEntity.created(new URI("/api/asientos/" + asientoDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, asientoDTO.getId().toString()))
            .body(asientoDTO);
    }

    /**
     * {@code PUT  /asientos/:id} : Updates an existing asiento.
     *
     * @param id the id of the asientoDTO to save.
     * @param asientoDTO the asientoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated asientoDTO,
     * or with status {@code 400 (Bad Request)} if the asientoDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the asientoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AsientoDTO> updateAsiento(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody AsientoDTO asientoDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Asiento : {}, {}", id, asientoDTO);
        if (asientoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, asientoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!asientoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        asientoDTO = asientoService.update(asientoDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, asientoDTO.getId().toString()))
            .body(asientoDTO);
    }

    /**
     * {@code PATCH  /asientos/:id} : Partial updates given fields of an existing asiento, field will ignore if it is null
     *
     * @param id the id of the asientoDTO to save.
     * @param asientoDTO the asientoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated asientoDTO,
     * or with status {@code 400 (Bad Request)} if the asientoDTO is not valid,
     * or with status {@code 404 (Not Found)} if the asientoDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the asientoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<AsientoDTO> partialUpdateAsiento(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody AsientoDTO asientoDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Asiento partially : {}, {}", id, asientoDTO);
        if (asientoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, asientoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!asientoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<AsientoDTO> result = asientoService.partialUpdate(asientoDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, asientoDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /asientos} : get all the asientos.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of asientos in body.
     */
    @GetMapping("")
    public List<AsientoDTO> getAllAsientos() {
        LOG.debug("REST request to get all Asientos");
        return asientoService.findAll();
    }

    /**
     * {@code GET  /asientos/:id} : get the "id" asiento.
     *
     * @param id the id of the asientoDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the asientoDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AsientoDTO> getAsiento(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Asiento : {}", id);
        Optional<AsientoDTO> asientoDTO = asientoService.findOne(id);
        return ResponseUtil.wrapOrNotFound(asientoDTO);
    }

    /**
     * {@code DELETE  /asientos/:id} : delete the "id" asiento.
     *
     * @param id the id of the asientoDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsiento(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Asiento : {}", id);
        asientoService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
