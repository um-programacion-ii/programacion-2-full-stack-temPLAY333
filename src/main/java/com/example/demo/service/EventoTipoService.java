package com.example.demo.service;

import com.example.demo.service.dto.EventoTipoDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.example.demo.domain.EventoTipo}.
 */
public interface EventoTipoService {
    /**
     * Save a eventoTipo.
     *
     * @param eventoTipoDTO the entity to save.
     * @return the persisted entity.
     */
    EventoTipoDTO save(EventoTipoDTO eventoTipoDTO);

    /**
     * Updates a eventoTipo.
     *
     * @param eventoTipoDTO the entity to update.
     * @return the persisted entity.
     */
    EventoTipoDTO update(EventoTipoDTO eventoTipoDTO);

    /**
     * Partially updates a eventoTipo.
     *
     * @param eventoTipoDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<EventoTipoDTO> partialUpdate(EventoTipoDTO eventoTipoDTO);

    /**
     * Get all the eventoTipos.
     *
     * @return the list of entities.
     */
    List<EventoTipoDTO> findAll();

    /**
     * Get all the EventoTipoDTO where Evento is {@code null}.
     *
     * @return the {@link List} of entities.
     */
    List<EventoTipoDTO> findAllWhereEventoIsNull();

    /**
     * Get the "id" eventoTipo.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<EventoTipoDTO> findOne(Long id);

    /**
     * Delete the "id" eventoTipo.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
