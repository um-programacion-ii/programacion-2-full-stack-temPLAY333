package com.example.demo.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.domain.Evento;
import com.example.demo.domain.Venta;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VentaResumenMapperTest {

    private VentaResumenMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new VentaResumenMapperImpl();
    }

    @Test
    void shouldMapVentaResumen() {
        Evento evento = new Evento().id(5L);
        Venta venta = new Venta()
            .ventaId(2001L)
            .fechaVenta(Instant.parse("2025-08-23T22:51:02.574851Z"))
            .resultado(false)
            .descripcion("Venta rechazada. Alguno de los asientos no se encontraban bloqueados para la venta.")
            .precioVenta(new BigDecimal("1200.10"))
            .evento(evento);

        var dto = mapper.toDto(venta, 0);

        assertThat(dto.getEventoId()).isEqualTo(5L);
        assertThat(dto.getVentaId()).isEqualTo(2001L);
        assertThat(dto.getFechaVenta()).isEqualTo(Instant.parse("2025-08-23T22:51:02.574851Z"));
        assertThat(dto.getResultado()).isFalse();
        assertThat(dto.getDescripcion()).startsWith("Venta rechazada");
        assertThat(dto.getPrecioVenta()).isEqualByComparingTo(new BigDecimal("1200.10"));
        assertThat(dto.getCantidadAsientos()).isEqualTo(0);
    }
}

