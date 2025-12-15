package com.example.demo.service.mapper;

import static com.example.demo.domain.AlumnoProfileAsserts.*;
import static com.example.demo.domain.AlumnoProfileTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class AlumnoProfileMapperTest {

    private AlumnoProfileMapper alumnoProfileMapper;

    @BeforeEach
    void setUp() {
        try {
            alumnoProfileMapper = Mappers.getMapper(AlumnoProfileMapper.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize AlumnoProfileMapper", e);
        }
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getAlumnoProfileSample1();
        var actual = alumnoProfileMapper.toEntity(alumnoProfileMapper.toDto(expected));
        assertAlumnoProfileAllPropertiesEquals(expected, actual);
    }
}
