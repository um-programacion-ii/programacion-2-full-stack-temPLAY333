package com.example.demo.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.domain.Asiento;
import com.example.demo.domain.enumeration.Estado;
import com.example.demo.service.dto.AsientoBloqueoEstadoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BloqueoAsientoMapperTest {

    private BloqueoAsientoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new BloqueoAsientoMapperImpl();
    }

    @Test
    void shouldMapBlockedSeatEstado() {
        Asiento asiento = new Asiento().fila(1).columna(2).estado(Estado.BLOQUEADO);

        AsientoBloqueoEstadoDTO dto = mapper.toDto(asiento);

        assertThat(dto.getFila()).isEqualTo(1);
        assertThat(dto.getColumna()).isEqualTo(2);
        assertThat(dto.getEstado()).isEqualTo("Bloqueado");
    }

    @Test
    void shouldMapOccupiedSeatEstado() {
        Asiento asiento = new Asiento().fila(3).columna(4).estado(Estado.OCUPADO);

        AsientoBloqueoEstadoDTO dto = mapper.toDto(asiento);

        assertThat(dto.getFila()).isEqualTo(3);
        assertThat(dto.getColumna()).isEqualTo(4);
        assertThat(dto.getEstado()).isEqualTo("Ocupado");
    }
}

