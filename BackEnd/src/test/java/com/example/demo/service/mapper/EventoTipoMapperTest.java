package com.example.demo.service.mapper;

import static com.example.demo.domain.EventoTipoAsserts.*;
import static com.example.demo.domain.EventoTipoTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class EventoTipoMapperTest {

    private EventoTipoMapper eventoTipoMapper;

    @BeforeEach
    void setUp() {
        eventoTipoMapper = Mappers.getMapper(EventoTipoMapper.class);
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getEventoTipoSample1();
        var actual = eventoTipoMapper.toEntity(eventoTipoMapper.toDto(expected));
        assertEventoTipoAllPropertiesEquals(expected, actual);
    }
}
