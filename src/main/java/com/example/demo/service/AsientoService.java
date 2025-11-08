package com.example.demo.service;

import com.example.demo.service.dto.AsientoDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.example.demo.domain.Asiento}.
 */
public interface AsientoService {
    /**
     * Save a asiento.
     *
     * @param asientoDTO the entity to save.
     * @return the persisted entity.
     */
    AsientoDTO save(AsientoDTO asientoDTO);

    /**
     * Updates a asiento.
     *
     * @param asientoDTO the entity to update.
     * @return the persisted entity.
     */
    AsientoDTO update(AsientoDTO asientoDTO);

    /**
     * Partially updates a asiento.
     *
     * @param asientoDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<AsientoDTO> partialUpdate(AsientoDTO asientoDTO);

    /**
     * Get all the asientos.
     *
     * @return the list of entities.
     */
    List<AsientoDTO> findAll();

    /**
     * Get the "id" asiento.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AsientoDTO> findOne(Long id);

    /**
     * Delete the "id" asiento.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
