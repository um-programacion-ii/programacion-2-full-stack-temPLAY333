package com.example.demo.service.impl;

import com.example.demo.domain.Integrante;
import com.example.demo.repository.IntegranteRepository;
import com.example.demo.service.IntegranteService;
import com.example.demo.service.dto.IntegranteDTO;
import com.example.demo.service.mapper.IntegranteMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.example.demo.domain.Integrante}.
 */
@Service
@Transactional
public class IntegranteServiceImpl implements IntegranteService {

    private static final Logger LOG = LoggerFactory.getLogger(IntegranteServiceImpl.class);

    private final IntegranteRepository integranteRepository;

    private final IntegranteMapper integranteMapper;

    public IntegranteServiceImpl(IntegranteRepository integranteRepository, IntegranteMapper integranteMapper) {
        this.integranteRepository = integranteRepository;
        this.integranteMapper = integranteMapper;
    }

    @Override
    public IntegranteDTO save(IntegranteDTO integranteDTO) {
        LOG.debug("Request to save Integrante : {}", integranteDTO);
        Integrante integrante = integranteMapper.toEntity(integranteDTO);
        integrante = integranteRepository.save(integrante);
        return integranteMapper.toDto(integrante);
    }

    @Override
    public IntegranteDTO update(IntegranteDTO integranteDTO) {
        LOG.debug("Request to update Integrante : {}", integranteDTO);
        Integrante integrante = integranteMapper.toEntity(integranteDTO);
        integrante = integranteRepository.save(integrante);
        return integranteMapper.toDto(integrante);
    }

    @Override
    public Optional<IntegranteDTO> partialUpdate(IntegranteDTO integranteDTO) {
        LOG.debug("Request to partially update Integrante : {}", integranteDTO);

        return integranteRepository
            .findById(integranteDTO.getId())
            .map(existingIntegrante -> {
                integranteMapper.partialUpdate(existingIntegrante, integranteDTO);

                return existingIntegrante;
            })
            .map(integranteRepository::save)
            .map(integranteMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IntegranteDTO> findAll() {
        LOG.debug("Request to get all Integrantes");
        return integranteRepository.findAll().stream().map(integranteMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IntegranteDTO> findOne(Long id) {
        LOG.debug("Request to get Integrante : {}", id);
        return integranteRepository.findById(id).map(integranteMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Integrante : {}", id);
        integranteRepository.deleteById(id);
    }
}
