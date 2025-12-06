package com.example.demo.service.mapper;

import com.example.demo.domain.Evento;
import com.example.demo.domain.EventoTipo;
import com.example.demo.domain.Integrante;
import com.example.demo.service.dto.EventoDTO;
import com.example.demo.service.dto.EventoTipoDTO;
import com.example.demo.service.dto.IntegranteDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-05T21:27:56-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (OpenLogic)"
)
@Component
public class EventoCompletoMapperImpl implements EventoCompletoMapper {

    @Override
    public EventoDTO toDto(Evento s) {
        if ( s == null ) {
            return null;
        }

        EventoDTO eventoDTO = new EventoDTO();

        eventoDTO.setEventoTipo( toDtoEventoTipoNombreDescripcion( s.getEventoTipo() ) );
        eventoDTO.setIntegrantes( toDtoIntegranteBasicoSet( s.getIntegrantes() ) );
        eventoDTO.setId( s.getId() );
        eventoDTO.setTitulo( s.getTitulo() );
        eventoDTO.setResumen( s.getResumen() );
        eventoDTO.setDescripcion( s.getDescripcion() );
        eventoDTO.setFecha( s.getFecha() );
        eventoDTO.setDireccion( s.getDireccion() );
        eventoDTO.setImagen( s.getImagen() );
        eventoDTO.setFilaAsientos( s.getFilaAsientos() );
        eventoDTO.setColumnAsientos( s.getColumnAsientos() );
        eventoDTO.setPrecioEntrada( s.getPrecioEntrada() );

        return eventoDTO;
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

    @Override
    public IntegranteDTO toDtoIntegranteBasico(Integrante integrante) {
        if ( integrante == null ) {
            return null;
        }

        IntegranteDTO integranteDTO = new IntegranteDTO();

        integranteDTO.setId( integrante.getId() );
        integranteDTO.setNombre( integrante.getNombre() );
        integranteDTO.setApellido( integrante.getApellido() );
        integranteDTO.setIdentificacion( integrante.getIdentificacion() );

        return integranteDTO;
    }
}
