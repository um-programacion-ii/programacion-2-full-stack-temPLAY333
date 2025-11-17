package com.example.demo.service.mapper;

import com.example.demo.domain.Evento;
import com.example.demo.domain.EventoTipo;
import com.example.demo.domain.Integrante;
import com.example.demo.service.dto.EventoDetalleDTO;
import com.example.demo.service.dto.EventoTipoBasicDTO;
import com.example.demo.service.dto.IntegranteBasicDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-08T19:05:53-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (OpenLogic)"
)
@Component
public class EventoDetalleMapperImpl implements EventoDetalleMapper {

    @Override
    public EventoDetalleDTO toDto(Evento evento) {
        if ( evento == null ) {
            return null;
        }

        EventoDetalleDTO eventoDetalleDTO = new EventoDetalleDTO();

        eventoDetalleDTO.setId( evento.getId() );
        eventoDetalleDTO.setTitulo( evento.getTitulo() );
        eventoDetalleDTO.setResumen( evento.getResumen() );
        eventoDetalleDTO.setDescripcion( evento.getDescripcion() );
        eventoDetalleDTO.setFecha( evento.getFecha() );
        eventoDetalleDTO.setDireccion( evento.getDireccion() );
        eventoDetalleDTO.setImagen( evento.getImagen() );
        eventoDetalleDTO.setFilaAsientos( evento.getFilaAsientos() );
        eventoDetalleDTO.setColumnAsientos( evento.getColumnAsientos() );
        eventoDetalleDTO.setPrecioEntrada( evento.getPrecioEntrada() );
        eventoDetalleDTO.setEventoTipo( toDtoEventoTipoBasic( evento.getEventoTipo() ) );
        eventoDetalleDTO.setIntegrantes( toDtoIntegranteBasicList( evento.getIntegrantes() ) );

        return eventoDetalleDTO;
    }

    @Override
    public EventoTipoBasicDTO toDtoEventoTipoBasic(EventoTipo eventoTipo) {
        if ( eventoTipo == null ) {
            return null;
        }

        EventoTipoBasicDTO eventoTipoBasicDTO = new EventoTipoBasicDTO();

        eventoTipoBasicDTO.setNombre( eventoTipo.getNombre() );
        eventoTipoBasicDTO.setDescripcion( eventoTipo.getDescripcion() );

        return eventoTipoBasicDTO;
    }

    @Override
    public IntegranteBasicDTO toDtoIntegranteBasic(Integrante integrante) {
        if ( integrante == null ) {
            return null;
        }

        IntegranteBasicDTO integranteBasicDTO = new IntegranteBasicDTO();

        integranteBasicDTO.setNombre( integrante.getNombre() );
        integranteBasicDTO.setApellido( integrante.getApellido() );
        integranteBasicDTO.setIdentificacion( integrante.getIdentificacion() );

        return integranteBasicDTO;
    }
}
