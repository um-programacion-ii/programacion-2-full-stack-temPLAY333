package com.example.demo.service.mapper;

import static com.example.demo.domain.AlumnoProfileAsserts.*;
import static com.example.demo.domain.AlumnoProfileTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AlumnoProfileMapperTest {

    private AlumnoProfileMapper alumnoProfileMapper;

    @BeforeEach
    void setUp() {
        alumnoProfileMapper = new AlumnoProfileMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getAlumnoProfileSample1();
        var actual = alumnoProfileMapper.toEntity(alumnoProfileMapper.toDto(expected));
        assertAlumnoProfileAllPropertiesEquals(expected, actual);
    }
}
