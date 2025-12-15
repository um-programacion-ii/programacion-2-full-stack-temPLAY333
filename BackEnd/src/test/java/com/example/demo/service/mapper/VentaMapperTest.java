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

    private final VentaMapper ventaMapper;
    private final AsientoMapper asientoMapper;
    private final EventoMapper eventoMapper;

    public VentaMapperTest() {
        try {
            // Create mappers in dependency order
            this.asientoMapper = Mappers.getMapper(AsientoMapper.class);
            this.eventoMapper = Mappers.getMapper(EventoMapper.class);

            // Create VentaMapper and inject dependencies manually
            this.ventaMapper = new com.example.demo.service.mapper.VentaMapperImpl();
            // Inject dependencies using reflection
            try {
                Field asientoField = ventaMapper.getClass().getDeclaredField("asientoMapper");
                asientoField.setAccessible(true);
                asientoField.set(ventaMapper, asientoMapper);

                Field eventoField = ventaMapper.getClass().getDeclaredField("eventoMapper");
                eventoField.setAccessible(true);
                eventoField.set(ventaMapper, eventoMapper);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Failed to inject dependencies into VentaMapper", e);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize mappers", e);
        }
    }

    @Test
    void toDto_shouldMapAsientos() {

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
