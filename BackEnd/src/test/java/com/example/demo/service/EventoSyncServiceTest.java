package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.demo.domain.Evento;
import com.example.demo.repository.EventoRepository;
import com.example.demo.service.dto.EventoDTO;
import com.example.demo.service.mapper.EventoMapper;
import com.example.demo.service.mapper.EventoResumenMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class EventoSyncServiceTest {

    private EventoRepository eventoRepository;
    private EventoMapper eventoMapper;
    private EventoResumenMapper eventoResumenMapper;
    private EventoExternalClient eventoExternalClient;

    private EventoSyncService eventoSyncService;

    @BeforeEach
    void setUp() {
        eventoRepository = Mockito.mock(EventoRepository.class);
        eventoMapper = Mockito.mock(EventoMapper.class);
        eventoResumenMapper = Mockito.mock(EventoResumenMapper.class);
        eventoExternalClient = Mockito.mock(EventoExternalClient.class);
        eventoSyncService = new EventoSyncService(eventoRepository, eventoMapper, eventoResumenMapper, eventoExternalClient);
    }

    @Test
    void sincronizarEventosCompletos_creaEventosNuevos() {
        EventoDTO dto = new EventoDTO();
        dto.setId(1L);
        dto.setTitulo("Titulo");
        dto.setResumen("Resumen");
        dto.setDescripcion("Desc");
        dto.setFecha(Instant.now());
        dto.setDireccion("Dir");
        dto.setImagen("Img");
        dto.setFilaAsientos(10);
        dto.setColumnAsientos(20);
        dto.setPrecioEntrada(BigDecimal.TEN);

        Evento entidadMapeada = new Evento();
        entidadMapeada.setId(1L);
        entidadMapeada.setTitulo("Titulo");

        when(eventoExternalClient.listarEventosCompletos()).thenReturn(List.of(dto));
        when(eventoRepository.findById(1L)).thenReturn(Optional.empty());
        when(eventoMapper.toEntity(dto)).thenReturn(entidadMapeada);
        when(eventoMapper.toDto(any(Evento.class))).thenReturn(dto);
        when(eventoRepository.save(any(Evento.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(eventoRepository.findAll()).thenReturn(List.of(entidadMapeada));

        eventoSyncService.sincronizarEventosCompletos();

        List<EventoDTO> lista = eventoSyncService.obtenerEventosCompletos();
        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).getTitulo()).isEqualTo("Titulo");
    }
}
