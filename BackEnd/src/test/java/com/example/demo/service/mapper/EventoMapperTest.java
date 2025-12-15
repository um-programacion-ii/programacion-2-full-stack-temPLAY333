package com.example.demo.service.mapper;

import static com.example.demo.domain.EventoAsserts.*;
import static com.example.demo.domain.EventoTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class EventoMapperTest {

    private EventoMapper eventoMapper;

    @BeforeEach
    void setUp() {
        try {
            eventoMapper = Mappers.getMapper(EventoMapper.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize EventoMapper", e);
        }
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getEventoSample1();
        var actual = eventoMapper.toEntity(eventoMapper.toDto(expected));
        assertEventoAllPropertiesEquals(expected, actual);
    }
}
