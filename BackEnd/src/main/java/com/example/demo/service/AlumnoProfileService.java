package com.example.demo.service;

import com.example.demo.service.dto.AlumnoProfileDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.example.demo.domain.AlumnoProfile}.
 */
public interface AlumnoProfileService {
    /**
     * Save a alumnoProfile.
     *
     * @param alumnoProfileDTO the entity to save.
     * @return the persisted entity.
     */
    AlumnoProfileDTO save(AlumnoProfileDTO alumnoProfileDTO);

    /**
     * Updates a alumnoProfile.
     *
     * @param alumnoProfileDTO the entity to update.
     * @return the persisted entity.
     */
    AlumnoProfileDTO update(AlumnoProfileDTO alumnoProfileDTO);

    /**
     * Partially updates a alumnoProfile.
     *
     * @param alumnoProfileDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<AlumnoProfileDTO> partialUpdate(AlumnoProfileDTO alumnoProfileDTO);

    /**
     * Get all the alumnoProfiles.
     *
     * @return the list of entities.
     */
    List<AlumnoProfileDTO> findAll();

    /**
     * Get all the alumnoProfiles with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AlumnoProfileDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" alumnoProfile.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AlumnoProfileDTO> findOne(Long id);

    /**
     * Delete the "id" alumnoProfile.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
