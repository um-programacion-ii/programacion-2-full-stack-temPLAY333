package com.example.demo.service.mapper;

import com.example.demo.domain.Asiento;
import com.example.demo.domain.Evento;
import com.example.demo.domain.User;
import com.example.demo.domain.Venta;
import com.example.demo.service.dto.AsientoDTO;
import com.example.demo.service.dto.VentaDTO;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static org.assertj.core.api.Assertions.assertThat;

class VentaMapperTest {

    private final VentaMapper ventaMapper = Mappers.getMapper(VentaMapper.class);
    private final AsientoMapper asientoMapper = Mappers.getMapper(AsientoMapper.class);

    private void injectAsientoMapper() {
        try {
            // MapStruct-generated implementation (componentModel=spring) expects asientoMapper to be injected
            Field f = ventaMapper.getClass().getDeclaredField("asientoMapper");
            f.setAccessible(true);
            f.set(ventaMapper, asientoMapper);
        } catch (NoSuchFieldException nsfe) {
            // If implementation uses a different field name or no field, ignore
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void toDto_shouldMapAsientos() {
        injectAsientoMapper();

        Venta venta = new Venta();
        venta.setId(1L);
        venta.setVentaId(100L);
        venta.setFechaVenta(Instant.parse("2025-11-01T10:00:00Z"));
        venta.setResultado(true);
        venta.setDescripcion("desc");
        venta.setPrecioVenta(BigDecimal.valueOf(123.45));

        User user = new User();
        user.setId(1L);
        user.setLogin("admin");
        venta.setUsuario(user);

        Evento evento = new Evento();
        evento.setId(2L);
        venta.setEvento(evento);

        Asiento a1 = new Asiento();
        a1.setId(10L);
        a1.setFila(1);
        a1.setColumna(2);
        a1.setPersona("Juan");
        venta.addAsiento(a1);

        VentaDTO dto = ventaMapper.toDto(venta);

        assertThat(dto).isNotNull();
        assertThat(dto.getAsientos()).isNotEmpty();
        AsientoDTO adto = dto.getAsientos().iterator().next();
        assertThat(adto.getId()).isEqualTo(10L);
        assertThat(adto.getPersona()).isEqualTo("Juan");
    }
}
