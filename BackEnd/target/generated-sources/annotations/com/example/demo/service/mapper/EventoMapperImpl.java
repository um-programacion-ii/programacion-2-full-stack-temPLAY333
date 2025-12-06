package com.example.demo.service.mapper;

import com.example.demo.domain.Evento;
import com.example.demo.domain.EventoTipo;
import com.example.demo.domain.Integrante;
import com.example.demo.service.dto.EventoDTO;
import com.example.demo.service.dto.EventoTipoDTO;
import com.example.demo.service.dto.IntegranteDTO;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-05T21:27:56-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (OpenLogic)"
)
@Component
public class EventoMapperImpl implements EventoMapper {

    @Override
    public List<Evento> toEntity(List<EventoDTO> dtoList) {
        if ( dtoList == null ) {
            return null;
        }

        List<Evento> list = new ArrayList<Evento>( dtoList.size() );
        for ( EventoDTO eventoDTO : dtoList ) {
            list.add( toEntity( eventoDTO ) );
        }

        return list;
    }

    @Override
    public List<EventoDTO> toDto(List<Evento> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<EventoDTO> list = new ArrayList<EventoDTO>( entityList.size() );
        for ( Evento evento : entityList ) {
            list.add( toDto( evento ) );
        }

        return list;
    }

    @Override
    public void partialUpdate(Evento entity, EventoDTO dto) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getId() != null ) {
            entity.setId( dto.getId() );
        }
        if ( dto.getTitulo() != null ) {
            entity.setTitulo( dto.getTitulo() );
        }
        if ( dto.getResumen() != null ) {
            entity.setResumen( dto.getResumen() );
        }
        if ( dto.getDescripcion() != null ) {
            entity.setDescripcion( dto.getDescripcion() );
        }
        if ( dto.getFecha() != null ) {
            entity.setFecha( dto.getFecha() );
        }
        if ( dto.getDireccion() != null ) {
            entity.setDireccion( dto.getDireccion() );
        }
        if ( dto.getImagen() != null ) {
            entity.setImagen( dto.getImagen() );
        }
        if ( dto.getFilaAsientos() != null ) {
            entity.setFilaAsientos( dto.getFilaAsientos() );
        }
        if ( dto.getColumnAsientos() != null ) {
            entity.setColumnAsientos( dto.getColumnAsientos() );
        }
        if ( dto.getPrecioEntrada() != null ) {
            entity.setPrecioEntrada( dto.getPrecioEntrada() );
        }
        if ( dto.getEventoTipo() != null ) {
            if ( entity.getEventoTipo() == null ) {
                entity.eventoTipo( new EventoTipo() );
            }
            eventoTipoDTOToEventoTipo( dto.getEventoTipo(), entity.getEventoTipo() );
        }
        if ( entity.getIntegrantes() != null ) {
            Set<Integrante> set = integranteDTOSetToIntegranteSet( dto.getIntegrantes() );
            if ( set != null ) {
                entity.getIntegrantes().clear();
                entity.getIntegrantes().addAll( set );
            }
        }
        else {
            Set<Integrante> set = integranteDTOSetToIntegranteSet( dto.getIntegrantes() );
            if ( set != null ) {
                entity.integrantes( set );
            }
        }
    }

    @Override
    public EventoDTO toDto(Evento s) {
        if ( s == null ) {
            return null;
        }

        EventoDTO eventoDTO = new EventoDTO();

        eventoDTO.setEventoTipo( toDtoEventoTipoNombre( s.getEventoTipo() ) );
        eventoDTO.setIntegrantes( toDtoIntegranteNombreSet( s.getIntegrantes() ) );
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
    public Evento toEntity(EventoDTO eventoDTO) {
        if ( eventoDTO == null ) {
            return null;
        }

        Evento evento = new Evento();

        evento.setId( eventoDTO.getId() );
        evento.setTitulo( eventoDTO.getTitulo() );
        evento.setResumen( eventoDTO.getResumen() );
        evento.setDescripcion( eventoDTO.getDescripcion() );
        evento.setFecha( eventoDTO.getFecha() );
        evento.setDireccion( eventoDTO.getDireccion() );
        evento.setImagen( eventoDTO.getImagen() );
        evento.setFilaAsientos( eventoDTO.getFilaAsientos() );
        evento.setColumnAsientos( eventoDTO.getColumnAsientos() );
        evento.setPrecioEntrada( eventoDTO.getPrecioEntrada() );
        evento.eventoTipo( eventoTipoDTOToEventoTipo1( eventoDTO.getEventoTipo() ) );
        evento.integrantes( integranteDTOSetToIntegranteSet( eventoDTO.getIntegrantes() ) );

        return evento;
    }

    @Override
    public EventoTipoDTO toDtoEventoTipoNombre(EventoTipo eventoTipo) {
        if ( eventoTipo == null ) {
            return null;
        }

        EventoTipoDTO eventoTipoDTO = new EventoTipoDTO();

        eventoTipoDTO.setId( eventoTipo.getId() );
        eventoTipoDTO.setNombre( eventoTipo.getNombre() );

        return eventoTipoDTO;
    }

    @Override
    public IntegranteDTO toDtoIntegranteNombre(Integrante integrante) {
        if ( integrante == null ) {
            return null;
        }

        IntegranteDTO integranteDTO = new IntegranteDTO();

        integranteDTO.setId( integrante.getId() );
        integranteDTO.setNombre( integrante.getNombre() );

        return integranteDTO;
    }

    protected void eventoTipoDTOToEventoTipo(EventoTipoDTO eventoTipoDTO, EventoTipo mappingTarget) {
        if ( eventoTipoDTO == null ) {
            return;
        }

        if ( eventoTipoDTO.getId() != null ) {
            mappingTarget.setId( eventoTipoDTO.getId() );
        }
        if ( eventoTipoDTO.getNombre() != null ) {
            mappingTarget.setNombre( eventoTipoDTO.getNombre() );
        }
        if ( eventoTipoDTO.getDescripcion() != null ) {
            mappingTarget.setDescripcion( eventoTipoDTO.getDescripcion() );
        }
    }

    protected Set<Evento> eventoDTOSetToEventoSet(Set<EventoDTO> set) {
        if ( set == null ) {
            return null;
        }

        Set<Evento> set1 = new LinkedHashSet<Evento>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( EventoDTO eventoDTO : set ) {
            set1.add( toEntity( eventoDTO ) );
        }

        return set1;
    }

    protected Integrante integranteDTOToIntegrante(IntegranteDTO integranteDTO) {
        if ( integranteDTO == null ) {
            return null;
        }

        Integrante integrante = new Integrante();

        integrante.setId( integranteDTO.getId() );
        integrante.setNombre( integranteDTO.getNombre() );
        integrante.setApellido( integranteDTO.getApellido() );
        integrante.setIdentificacion( integranteDTO.getIdentificacion() );
        integrante.eventos( eventoDTOSetToEventoSet( integranteDTO.getEventos() ) );

        return integrante;
    }

    protected Set<Integrante> integranteDTOSetToIntegranteSet(Set<IntegranteDTO> set) {
        if ( set == null ) {
            return null;
        }

        Set<Integrante> set1 = new LinkedHashSet<Integrante>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( IntegranteDTO integranteDTO : set ) {
            set1.add( integranteDTOToIntegrante( integranteDTO ) );
        }

        return set1;
    }

    protected EventoTipo eventoTipoDTOToEventoTipo1(EventoTipoDTO eventoTipoDTO) {
        if ( eventoTipoDTO == null ) {
            return null;
        }

        EventoTipo eventoTipo = new EventoTipo();

        eventoTipo.setId( eventoTipoDTO.getId() );
        eventoTipo.setNombre( eventoTipoDTO.getNombre() );
        eventoTipo.setDescripcion( eventoTipoDTO.getDescripcion() );

        return eventoTipo;
    }
}
