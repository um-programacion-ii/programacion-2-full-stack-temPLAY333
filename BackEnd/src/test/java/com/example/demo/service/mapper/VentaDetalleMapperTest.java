package com.example.demo.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.domain.Asiento;
import com.example.demo.domain.Evento;
import com.example.demo.domain.Venta;
import com.example.demo.domain.enumeration.Estado;
import com.example.demo.service.dto.VentaDetalleDTO;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VentaDetalleMapperTest {

    private VentaDetalleMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new VentaDetalleMapperImpl();
    }

    @Test
    void shouldMapSuccessfulSaleWithAsientos() {
        // given
        Evento evento = new Evento().id(1L);
        Venta venta = new Venta()
            .ventaId(1504L)
            .fechaVenta(Instant.parse("2025-08-23T22:51:15.101553Z"))
            .resultado(true)
            .descripcion("Venta realizada con exito")
            .precioVenta(new BigDecimal("1200.1"))
            .evento(evento);

        Set<Asiento> asientos = new HashSet<>();
        asientos.add(new Asiento().fila(2).columna(1).persona("Fernando Villarreal").estado(Estado.OCUPADO));
        asientos.add(new Asiento().fila(2).columna(2).persona("Carlos Perez").estado(Estado.OCUPADO));

        // when
        VentaDetalleDTO dto = mapper.toDto(venta, asientos);

        // then
        assertThat(dto.getEventoId()).isEqualTo(1L);
        assertThat(dto.getVentaId()).isEqualTo(1504L);
        assertThat(dto.getFechaVenta()).isEqualTo(Instant.parse("2025-08-23T22:51:15.101553Z"));
        assertThat(dto.getResultado()).isTrue();
        assertThat(dto.getDescripcion()).isEqualTo("Venta realizada con exito");
        assertThat(dto.getPrecioVenta()).isEqualByComparingTo(new BigDecimal("1200.1"));
        assertThat(dto.getAsientos()).hasSize(2);
        assertThat(dto.getAsientos())
            .anySatisfy(a -> {
                assertThat(a.getFila()).isEqualTo(2);
                assertThat(a.getColumna()).isEqualTo(1);
                assertThat(a.getPersona()).isEqualTo("Fernando Villarreal");
                assertThat(a.getEstado()).isEqualTo("Ocupado");
            })
            .anySatisfy(a -> {
                assertThat(a.getFila()).isEqualTo(2);
                assertThat(a.getColumna()).isEqualTo(2);
                assertThat(a.getPersona()).isEqualTo("Carlos Perez");
                assertThat(a.getEstado()).isEqualTo("Ocupado");
            });
    }

    @Test
    void shouldMapFailedSaleWithEmptyAsientos() {
        // given
        Evento evento = new Evento().id(1L);
        Venta venta = new Venta()
            .ventaId(1503L)
            .fechaVenta(Instant.parse("2025-08-23T22:51:02.574851Z"))
            .resultado(false)
            .descripcion("Venta rechazada. Alguno de los asientos no se encontraban bloqueados para la venta.")
            .precioVenta(new BigDecimal("1200.1"))
            .evento(evento);

        Set<Asiento> asientos = java.util.Collections.emptySet();

        // when
        VentaDetalleDTO dto = mapper.toDto(venta, asientos);

        // then
        assertThat(dto.getEventoId()).isEqualTo(1L);
        assertThat(dto.getVentaId()).isEqualTo(1503L);
        assertThat(dto.getFechaVenta()).isEqualTo(Instant.parse("2025-08-23T22:51:02.574851Z"));
        assertThat(dto.getResultado()).isFalse();
        assertThat(dto.getDescripcion()).startsWith("Venta rechazada.");
        assertThat(dto.getPrecioVenta()).isEqualByComparingTo(new BigDecimal("1200.1"));
        assertThat(dto.getAsientos()).isEmpty();
    }
}

