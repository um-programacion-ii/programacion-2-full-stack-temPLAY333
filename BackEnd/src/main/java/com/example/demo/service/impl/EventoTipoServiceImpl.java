package com.example.demo.service.impl;

import com.example.demo.domain.EventoTipo;
import com.example.demo.repository.EventoTipoRepository;
import com.example.demo.service.EventoTipoService;
import com.example.demo.service.dto.EventoTipoDTO;
import com.example.demo.service.mapper.EventoTipoMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.example.demo.domain.EventoTipo}.
 */
@Service
@Transactional
public class EventoTipoServiceImpl implements EventoTipoService {

    private static final Logger LOG = LoggerFactory.getLogger(EventoTipoServiceImpl.class);

    private final EventoTipoRepository eventoTipoRepository;

    private final EventoTipoMapper eventoTipoMapper;

    public EventoTipoServiceImpl(EventoTipoRepository eventoTipoRepository, EventoTipoMapper eventoTipoMapper) {
        this.eventoTipoRepository = eventoTipoRepository;
        this.eventoTipoMapper = eventoTipoMapper;
    }

    @Override
    public EventoTipoDTO save(EventoTipoDTO eventoTipoDTO) {
        LOG.debug("Request to save EventoTipo : {}", eventoTipoDTO);
        EventoTipo eventoTipo = eventoTipoMapper.toEntity(eventoTipoDTO);
        eventoTipo = eventoTipoRepository.save(eventoTipo);
        return eventoTipoMapper.toDto(eventoTipo);
    }

    @Override
    public EventoTipoDTO update(EventoTipoDTO eventoTipoDTO) {
        LOG.debug("Request to update EventoTipo : {}", eventoTipoDTO);
        EventoTipo eventoTipo = eventoTipoMapper.toEntity(eventoTipoDTO);
        eventoTipo = eventoTipoRepository.save(eventoTipo);
        return eventoTipoMapper.toDto(eventoTipo);
    }

    @Override
    public Optional<EventoTipoDTO> partialUpdate(EventoTipoDTO eventoTipoDTO) {
        LOG.debug("Request to partially update EventoTipo : {}", eventoTipoDTO);

        return eventoTipoRepository
            .findById(eventoTipoDTO.getId())
            .map(existingEventoTipo -> {
                eventoTipoMapper.partialUpdate(existingEventoTipo, eventoTipoDTO);

                return existingEventoTipo;
            })
            .map(eventoTipoRepository::save)
            .map(eventoTipoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoTipoDTO> findAll() {
        LOG.debug("Request to get all EventoTipos");
        return eventoTipoRepository.findAll().stream().map(eventoTipoMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     *  Get all the eventoTipos where Evento is {@code null}.
     *  @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<EventoTipoDTO> findAllWhereEventoIsNull() {
        LOG.debug("Request to get all eventoTipos where Evento is null");
        return StreamSupport.stream(eventoTipoRepository.findAll().spliterator(), false)
            .filter(eventoTipo -> eventoTipo.getEvento() == null)
            .map(eventoTipoMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EventoTipoDTO> findOne(Long id) {
        LOG.debug("Request to get EventoTipo : {}", id);
        return eventoTipoRepository.findById(id).map(eventoTipoMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete EventoTipo : {}", id);
        eventoTipoRepository.deleteById(id);
    }
}
