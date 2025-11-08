package com.example.demo.web.rest;

import com.example.demo.repository.AlumnoProfileRepository;
import com.example.demo.service.AlumnoProfileService;
import com.example.demo.service.dto.AlumnoProfileDTO;
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
 * REST controller for managing {@link com.example.demo.domain.AlumnoProfile}.
 */
@RestController
@RequestMapping("/api/alumno-profiles")
public class AlumnoProfileResource {

    private static final Logger LOG = LoggerFactory.getLogger(AlumnoProfileResource.class);

    private static final String ENTITY_NAME = "alumnoProfile";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AlumnoProfileService alumnoProfileService;

    private final AlumnoProfileRepository alumnoProfileRepository;

    public AlumnoProfileResource(AlumnoProfileService alumnoProfileService, AlumnoProfileRepository alumnoProfileRepository) {
        this.alumnoProfileService = alumnoProfileService;
        this.alumnoProfileRepository = alumnoProfileRepository;
    }

    /**
     * {@code POST  /alumno-profiles} : Create a new alumnoProfile.
     *
     * @param alumnoProfileDTO the alumnoProfileDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new alumnoProfileDTO, or with status {@code 400 (Bad Request)} if the alumnoProfile has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<AlumnoProfileDTO> createAlumnoProfile(@Valid @RequestBody AlumnoProfileDTO alumnoProfileDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save AlumnoProfile : {}", alumnoProfileDTO);
        if (alumnoProfileDTO.getId() != null) {
            throw new BadRequestAlertException("A new alumnoProfile cannot already have an ID", ENTITY_NAME, "idexists");
        }
        alumnoProfileDTO = alumnoProfileService.save(alumnoProfileDTO);
        return ResponseEntity.created(new URI("/api/alumno-profiles/" + alumnoProfileDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, alumnoProfileDTO.getId().toString()))
            .body(alumnoProfileDTO);
    }

    /**
     * {@code PUT  /alumno-profiles/:id} : Updates an existing alumnoProfile.
     *
     * @param id the id of the alumnoProfileDTO to save.
     * @param alumnoProfileDTO the alumnoProfileDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated alumnoProfileDTO,
     * or with status {@code 400 (Bad Request)} if the alumnoProfileDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the alumnoProfileDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AlumnoProfileDTO> updateAlumnoProfile(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody AlumnoProfileDTO alumnoProfileDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update AlumnoProfile : {}, {}", id, alumnoProfileDTO);
        if (alumnoProfileDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, alumnoProfileDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!alumnoProfileRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        alumnoProfileDTO = alumnoProfileService.update(alumnoProfileDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, alumnoProfileDTO.getId().toString()))
            .body(alumnoProfileDTO);
    }

    /**
     * {@code PATCH  /alumno-profiles/:id} : Partial updates given fields of an existing alumnoProfile, field will ignore if it is null
     *
     * @param id the id of the alumnoProfileDTO to save.
     * @param alumnoProfileDTO the alumnoProfileDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated alumnoProfileDTO,
     * or with status {@code 400 (Bad Request)} if the alumnoProfileDTO is not valid,
     * or with status {@code 404 (Not Found)} if the alumnoProfileDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the alumnoProfileDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<AlumnoProfileDTO> partialUpdateAlumnoProfile(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody AlumnoProfileDTO alumnoProfileDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update AlumnoProfile partially : {}, {}", id, alumnoProfileDTO);
        if (alumnoProfileDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, alumnoProfileDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!alumnoProfileRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<AlumnoProfileDTO> result = alumnoProfileService.partialUpdate(alumnoProfileDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, alumnoProfileDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /alumno-profiles} : get all the alumnoProfiles.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of alumnoProfiles in body.
     */
    @GetMapping("")
    public List<AlumnoProfileDTO> getAllAlumnoProfiles(
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get all AlumnoProfiles");
        return alumnoProfileService.findAll();
    }

    /**
     * {@code GET  /alumno-profiles/:id} : get the "id" alumnoProfile.
     *
     * @param id the id of the alumnoProfileDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the alumnoProfileDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AlumnoProfileDTO> getAlumnoProfile(@PathVariable("id") Long id) {
        LOG.debug("REST request to get AlumnoProfile : {}", id);
        Optional<AlumnoProfileDTO> alumnoProfileDTO = alumnoProfileService.findOne(id);
        return ResponseUtil.wrapOrNotFound(alumnoProfileDTO);
    }

    /**
     * {@code DELETE  /alumno-profiles/:id} : delete the "id" alumnoProfile.
     *
     * @param id the id of the alumnoProfileDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlumnoProfile(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete AlumnoProfile : {}", id);
        alumnoProfileService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
