package com.example.demo.service.mapper;

import com.example.demo.domain.Evento;
import com.example.demo.domain.EventoTipo;
import com.example.demo.service.dto.EventoResumenDTO;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static org.assertj.core.api.Assertions.assertThat;

class EventoResumenMapperTest {

    private final EventoResumenMapper mapper;

    public EventoResumenMapperTest() {
        try {
            this.mapper = Mappers.getMapper(EventoResumenMapper.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize EventoResumenMapper", e);
        }
    }

    @Test
    void toDto_shouldIncludeImagen() {
        Evento e = new Evento();
        e.setId(5L);
        e.setTitulo("T");
        e.setResumen("R");
        e.setFecha(Instant.parse("2025-11-10T11:00:00Z"));
        e.setPrecioEntrada(BigDecimal.valueOf(1000));
        e.setImagen("https://example.com/img.jpg");

        EventoTipo et = new EventoTipo();
        et.setId(1L);
        et.setNombre("Concierto");
        e.setEventoTipo(et);

        EventoResumenDTO dto = mapper.toDto(e);
        assertThat(dto).isNotNull();
        assertThat(dto.getImagen()).isEqualTo("https://example.com/img.jpg");
    }
}

