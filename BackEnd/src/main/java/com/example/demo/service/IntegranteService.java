package com.example.demo.service;

import com.example.demo.service.dto.IntegranteDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.example.demo.domain.Integrante}.
 */
public interface IntegranteService {
    /**
     * Save a integrante.
     *
     * @param integranteDTO the entity to save.
     * @return the persisted entity.
     */
    IntegranteDTO save(IntegranteDTO integranteDTO);

    /**
     * Updates a integrante.
     *
     * @param integranteDTO the entity to update.
     * @return the persisted entity.
     */
    IntegranteDTO update(IntegranteDTO integranteDTO);

    /**
     * Partially updates a integrante.
     *
     * @param integranteDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<IntegranteDTO> partialUpdate(IntegranteDTO integranteDTO);

    /**
     * Get all the integrantes.
     *
     * @return the list of entities.
     */
    List<IntegranteDTO> findAll();

    /**
     * Get the "id" integrante.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<IntegranteDTO> findOne(Long id);

    /**
     * Delete the "id" integrante.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
