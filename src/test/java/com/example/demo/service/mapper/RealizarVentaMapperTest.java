package com.example.demo.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.domain.Asiento;
import com.example.demo.domain.Evento;
import com.example.demo.domain.Venta;
import com.example.demo.domain.enumeration.Estado;
import com.example.demo.service.dto.RealizarVentaResponseDTO;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RealizarVentaMapperTest {

    private RealizarVentaMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RealizarVentaMapperImpl();
    }

    @Test
    void shouldMapSuccessfulSaleResponse() {
        // given
        Evento evento = new Evento().id(1L);
        Venta venta = new Venta()
            .ventaId(1506L)
            .fechaVenta(Instant.parse("2025-08-24T23:18:41.974720Z"))
            .resultado(true)
            .descripcion("Venta realizada con exito")
            .precioVenta(new BigDecimal("1400.1"))
            .evento(evento);
        Set<Asiento> asientos = new HashSet<>();
        asientos.add(new Asiento().fila(2).columna(3).persona("Fernando Galvez").estado(Estado.OCUPADO));
        asientos.add(new Asiento().fila(2).columna(4).persona("Carlos Perez").estado(Estado.OCUPADO));

        // when
        RealizarVentaResponseDTO dto = mapper.toDto(venta, asientos);

        // then
        assertThat(dto.getEventoId()).isEqualTo(1L);
        assertThat(dto.getVentaId()).isEqualTo(1506L);
        assertThat(dto.getFechaVenta()).isEqualTo(Instant.parse("2025-08-24T23:18:41.974720Z"));
        assertThat(dto.getResultado()).isTrue();
        assertThat(dto.getDescripcion()).isEqualTo("Venta realizada con exito");
        assertThat(dto.getPrecioVenta()).isEqualByComparingTo(new BigDecimal("1400.1"));
        assertThat(dto.getAsientos()).hasSize(2);
        assertThat(dto.getAsientos())
            .anySatisfy(a -> {
                assertThat(a.getFila()).isEqualTo(2);
                assertThat(a.getColumna()).isEqualTo(3);
                assertThat(a.getPersona()).isEqualTo("Fernando Galvez");
                assertThat(a.getEstado()).isEqualTo("Ocupado");
            })
            .anySatisfy(a -> {
                assertThat(a.getFila()).isEqualTo(2);
                assertThat(a.getColumna()).isEqualTo(4);
                assertThat(a.getPersona()).isEqualTo("Carlos Perez");
                assertThat(a.getEstado()).isEqualTo("Ocupado");
            });
    }

    @Test
    void shouldMapFailedSaleResponseWithFreeSeats() {
        // Caso de venta rechazada: estados Libre y resultado false
        Evento evento = new Evento().id(1L);
        Venta venta = new Venta()
            .ventaId(1507L)
            .fechaVenta(Instant.parse("2025-08-24T23:23:05.622646Z"))
            .resultado(false)
            .descripcion("Venta rechazada. Alguno de los asientos no se encontraban bloqueados para la venta.")
            .precioVenta(new BigDecimal("1400.1"))
            .evento(evento);
        Set<Asiento> asientos = new HashSet<>();
        asientos.add(new Asiento().fila(2).columna(3).persona("Persona Libre 1").estado(Estado.LIBRE));
        asientos.add(new Asiento().fila(2).columna(4).persona("Persona Libre 2").estado(Estado.LIBRE));

        // when
        RealizarVentaResponseDTO dto = mapper.toDto(venta, asientos);

        // then
        assertThat(dto.getEventoId()).isEqualTo(1L);
        assertThat(dto.getVentaId()).isEqualTo(1507L);
        assertThat(dto.getFechaVenta()).isEqualTo(Instant.parse("2025-08-24T23:23:05.622646Z"));
        assertThat(dto.getResultado()).isFalse();
        assertThat(dto.getDescripcion()).startsWith("Venta rechazada.");
        assertThat(dto.getPrecioVenta()).isEqualByComparingTo(new BigDecimal("1400.1"));
        assertThat(dto.getAsientos()).hasSize(2);
        assertThat(dto.getAsientos())
            .allSatisfy(a -> assertThat(a.getEstado()).isEqualTo("Libre"));
    }
}
