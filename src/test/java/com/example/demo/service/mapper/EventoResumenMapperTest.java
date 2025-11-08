package com.example.demo.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.domain.Evento;
import com.example.demo.domain.EventoTipo;
import com.example.demo.service.dto.EventoResumenDTO;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventoResumenMapperTest {

    private EventoResumenMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new EventoResumenMapperImpl();
    }

    @Test
    void shouldMapEventoResumen() {
        EventoTipo tipo = new EventoTipo().id(10L);
        tipo.setNombre("Conferencia");
        tipo.setDescripcion("Conferencia");
        Evento evento = new Evento()
            .id(2L)
            .titulo("Otra Conferencia Nerd")
            .resumen("Resumen breve")
            .descripcion("Descripcion completa")
            .fecha(Instant.parse("2025-12-12T14:00:00Z"))
            .direccion("Aula magna")
            .imagen("http://img2")
            .filaAsientos(12)
            .columnAsientos(18)
            .precioEntrada(new BigDecimal("4500.00"))
            .eventoTipo(tipo);

        EventoResumenDTO dto = mapper.toDto(evento);

        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getTitulo()).isEqualTo("Otra Conferencia Nerd");
        assertThat(dto.getEventoTipo().getNombre()).isEqualTo("Conferencia");
        assertThat(dto.getEventoTipo().getDescripcion()).isEqualTo("Conferencia");
        assertThat(dto.getPrecioEntrada()).isEqualByComparingTo(new BigDecimal("4500.00"));
    }
}

