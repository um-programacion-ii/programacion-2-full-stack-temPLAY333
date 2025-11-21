package com.example.demo.service.mapper;

import com.example.demo.domain.Evento;
import com.example.demo.domain.EventoTipo;
import com.example.demo.service.dto.EventoResumenDTO;
import com.example.demo.service.dto.EventoTipoDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-08T19:05:53-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (OpenLogic)"
)
@Component
public class EventoResumenMapperImpl implements EventoResumenMapper {

    @Override
    public EventoResumenDTO toDto(Evento evento) {
        if ( evento == null ) {
            return null;
        }

        EventoResumenDTO eventoResumenDTO = new EventoResumenDTO();

        eventoResumenDTO.setId( evento.getId() );
        eventoResumenDTO.setTitulo( evento.getTitulo() );
        eventoResumenDTO.setResumen( evento.getResumen() );
        eventoResumenDTO.setDescripcion( evento.getDescripcion() );
        eventoResumenDTO.setFecha( evento.getFecha() );
        eventoResumenDTO.setPrecioEntrada( evento.getPrecioEntrada() );
        eventoResumenDTO.setEventoTipo( toDtoEventoTipoNombreDescripcion( evento.getEventoTipo() ) );

        return eventoResumenDTO;
    }

    @Override
    public EventoTipoDTO toDtoEventoTipoNombreDescripcion(EventoTipo eventoTipo) {
        if ( eventoTipo == null ) {
            return null;
        }

        EventoTipoDTO eventoTipoDTO = new EventoTipoDTO();

        eventoTipoDTO.setId( eventoTipo.getId() );
        eventoTipoDTO.setNombre( eventoTipo.getNombre() );
        eventoTipoDTO.setDescripcion( eventoTipo.getDescripcion() );

        return eventoTipoDTO;
    }
}
