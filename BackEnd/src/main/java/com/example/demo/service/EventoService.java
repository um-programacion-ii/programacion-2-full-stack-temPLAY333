package com.example.demo.service;

import com.example.demo.service.dto.EventoDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.example.demo.domain.Evento}.
 */
public interface EventoService {
    /**
     * Save a evento.
     *
     * @param eventoDTO the entity to save.
     * @return the persisted entity.
     */
    EventoDTO save(EventoDTO eventoDTO);

    /**
     * Updates a evento.
     *
     * @param eventoDTO the entity to update.
     * @return the persisted entity.
     */
    EventoDTO update(EventoDTO eventoDTO);

    /**
     * Partially updates a evento.
     *
     * @param eventoDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<EventoDTO> partialUpdate(EventoDTO eventoDTO);

    /**
     * Get all the eventos.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<EventoDTO> findAll(Pageable pageable);

    /**
     * Get all the eventos with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<EventoDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" evento.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<EventoDTO> findOne(Long id);

    /**
     * Delete the "id" evento.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
