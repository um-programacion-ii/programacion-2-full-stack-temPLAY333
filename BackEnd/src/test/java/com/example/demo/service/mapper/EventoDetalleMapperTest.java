package com.example.demo.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.domain.Evento;
import com.example.demo.domain.EventoTipo;
import com.example.demo.domain.Integrante;
import com.example.demo.service.dto.EventoDetalleDTO;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventoDetalleMapperTest {

    private EventoDetalleMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new EventoDetalleMapperImpl();
    }

    @Test
    void shouldMapEventoDetalleConSubMappers() {
        EventoTipo tipo = new EventoTipo().id(10L);
        tipo.setNombre("Conferencia");
        tipo.setDescripcion("Conferencia");

        Evento evento = new Evento()
            .id(1L)
            .titulo("Conferencia Nerd")
            .resumen("Esta es una conferencia de Nerds")
            .descripcion("Detallada")
            .fecha(Instant.parse("2025-11-10T11:00:00Z"))
            .direccion("Aula magna")
            .imagen("http://img")
            .filaAsientos(10)
            .columnAsientos(20)
            .precioEntrada(new BigDecimal("2500.00"))
            .eventoTipo(tipo);

        Set<Integrante> integrantes = new HashSet<>();
        integrantes.add(new Integrante().nombre("María").apellido("Corvalán").identificacion("Dra."));
        integrantes.add(new Integrante().nombre("Ricardo").apellido("Tapia").identificacion("Profesor"));
        evento.setIntegrantes(integrantes);

        EventoDetalleDTO dto = mapper.toDto(evento);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTitulo()).isEqualTo("Conferencia Nerd");
        assertThat(dto.getEventoTipo().getNombre()).isEqualTo("Conferencia");
        assertThat(dto.getEventoTipo().getDescripcion()).isEqualTo("Conferencia");
        assertThat(dto.getIntegrantes()).hasSize(2);
        assertThat(dto.getIntegrantes()).anySatisfy(i -> assertThat(i.getNombre()).isEqualTo("María"));
    }
}

