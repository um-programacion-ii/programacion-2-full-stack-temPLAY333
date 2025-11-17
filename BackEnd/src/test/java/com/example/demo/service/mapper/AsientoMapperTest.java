package com.example.demo.service.mapper;

import static com.example.demo.domain.AsientoAsserts.*;
import static com.example.demo.domain.AsientoTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AsientoMapperTest {

    private AsientoMapper asientoMapper;

    @BeforeEach
    void setUp() {
        asientoMapper = new AsientoMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getAsientoSample1();
        var actual = asientoMapper.toEntity(asientoMapper.toDto(expected));
        assertAsientoAllPropertiesEquals(expected, actual);
    }
}
