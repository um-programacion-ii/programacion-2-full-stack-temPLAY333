package com.example.demo.service.mapper;

import static com.example.demo.domain.IntegranteAsserts.*;
import static com.example.demo.domain.IntegranteTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class IntegranteMapperTest {

    private IntegranteMapper integranteMapper;

    @BeforeEach
    void setUp() {
        try {
            integranteMapper = Mappers.getMapper(IntegranteMapper.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize IntegranteMapper", e);
        }
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getIntegranteSample1();
        var actual = integranteMapper.toEntity(integranteMapper.toDto(expected));
        assertIntegranteAllPropertiesEquals(expected, actual);
    }
}
