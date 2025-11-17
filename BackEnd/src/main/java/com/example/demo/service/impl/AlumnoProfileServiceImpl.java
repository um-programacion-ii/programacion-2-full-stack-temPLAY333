package com.example.demo.service.impl;

import com.example.demo.domain.AlumnoProfile;
import com.example.demo.repository.AlumnoProfileRepository;
import com.example.demo.service.AlumnoProfileService;
import com.example.demo.service.dto.AlumnoProfileDTO;
import com.example.demo.service.mapper.AlumnoProfileMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.example.demo.domain.AlumnoProfile}.
 */
@Service
@Transactional
public class AlumnoProfileServiceImpl implements AlumnoProfileService {

    private static final Logger LOG = LoggerFactory.getLogger(AlumnoProfileServiceImpl.class);

    private final AlumnoProfileRepository alumnoProfileRepository;

    private final AlumnoProfileMapper alumnoProfileMapper;

    public AlumnoProfileServiceImpl(AlumnoProfileRepository alumnoProfileRepository, AlumnoProfileMapper alumnoProfileMapper) {
        this.alumnoProfileRepository = alumnoProfileRepository;
        this.alumnoProfileMapper = alumnoProfileMapper;
    }

    @Override
    public AlumnoProfileDTO save(AlumnoProfileDTO alumnoProfileDTO) {
        LOG.debug("Request to save AlumnoProfile : {}", alumnoProfileDTO);
        AlumnoProfile alumnoProfile = alumnoProfileMapper.toEntity(alumnoProfileDTO);
        alumnoProfile = alumnoProfileRepository.save(alumnoProfile);
        return alumnoProfileMapper.toDto(alumnoProfile);
    }

    @Override
    public AlumnoProfileDTO update(AlumnoProfileDTO alumnoProfileDTO) {
        LOG.debug("Request to update AlumnoProfile : {}", alumnoProfileDTO);
        AlumnoProfile alumnoProfile = alumnoProfileMapper.toEntity(alumnoProfileDTO);
        alumnoProfile = alumnoProfileRepository.save(alumnoProfile);
        return alumnoProfileMapper.toDto(alumnoProfile);
    }

    @Override
    public Optional<AlumnoProfileDTO> partialUpdate(AlumnoProfileDTO alumnoProfileDTO) {
        LOG.debug("Request to partially update AlumnoProfile : {}", alumnoProfileDTO);

        return alumnoProfileRepository
            .findById(alumnoProfileDTO.getId())
            .map(existingAlumnoProfile -> {
                alumnoProfileMapper.partialUpdate(existingAlumnoProfile, alumnoProfileDTO);

                return existingAlumnoProfile;
            })
            .map(alumnoProfileRepository::save)
            .map(alumnoProfileMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumnoProfileDTO> findAll() {
        LOG.debug("Request to get all AlumnoProfiles");
        return alumnoProfileRepository.findAll().stream().map(alumnoProfileMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    public Page<AlumnoProfileDTO> findAllWithEagerRelationships(Pageable pageable) {
        return alumnoProfileRepository.findAllWithEagerRelationships(pageable).map(alumnoProfileMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AlumnoProfileDTO> findOne(Long id) {
        LOG.debug("Request to get AlumnoProfile : {}", id);
        return alumnoProfileRepository.findOneWithEagerRelationships(id).map(alumnoProfileMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete AlumnoProfile : {}", id);
        alumnoProfileRepository.deleteById(id);
    }
}
