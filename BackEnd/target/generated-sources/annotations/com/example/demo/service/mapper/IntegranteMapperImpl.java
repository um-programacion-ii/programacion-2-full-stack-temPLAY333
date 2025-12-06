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
public class IntegranteMapperImpl implements IntegranteMapper {

    @Override
    public List<Integrante> toEntity(List<IntegranteDTO> dtoList) {
        if ( dtoList == null ) {
            return null;
        }

        List<Integrante> list = new ArrayList<Integrante>( dtoList.size() );
        for ( IntegranteDTO integranteDTO : dtoList ) {
            list.add( toEntity( integranteDTO ) );
        }

        return list;
    }

    @Override
    public List<IntegranteDTO> toDto(List<Integrante> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<IntegranteDTO> list = new ArrayList<IntegranteDTO>( entityList.size() );
        for ( Integrante integrante : entityList ) {
            list.add( toDto( integrante ) );
        }

        return list;
    }

    @Override
    public void partialUpdate(Integrante entity, IntegranteDTO dto) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getId() != null ) {
            entity.setId( dto.getId() );
        }
        if ( dto.getNombre() != null ) {
            entity.setNombre( dto.getNombre() );
        }
        if ( dto.getApellido() != null ) {
            entity.setApellido( dto.getApellido() );
        }
        if ( dto.getIdentificacion() != null ) {
            entity.setIdentificacion( dto.getIdentificacion() );
        }
        if ( entity.getEventos() != null ) {
            Set<Evento> set = eventoDTOSetToEventoSet( dto.getEventos() );
            if ( set != null ) {
                entity.getEventos().clear();
                entity.getEventos().addAll( set );
            }
        }
        else {
            Set<Evento> set = eventoDTOSetToEventoSet( dto.getEventos() );
            if ( set != null ) {
                entity.eventos( set );
            }
        }
    }

    @Override
    public IntegranteDTO toDto(Integrante s) {
        if ( s == null ) {
            return null;
        }

        IntegranteDTO integranteDTO = new IntegranteDTO();

        integranteDTO.setEventos( toDtoEventoIdSet( s.getEventos() ) );
        integranteDTO.setId( s.getId() );
        integranteDTO.setNombre( s.getNombre() );
        integranteDTO.setApellido( s.getApellido() );
        integranteDTO.setIdentificacion( s.getIdentificacion() );

        return integranteDTO;
    }

    @Override
    public Integrante toEntity(IntegranteDTO integranteDTO) {
        if ( integranteDTO == null ) {
            return null;
        }

        Integrante integrante = new Integrante();

        integrante.setId( integranteDTO.getId() );
        integrante.setNombre( integranteDTO.getNombre() );
        integrante.setApellido( integranteDTO.getApellido() );
        integrante.setIdentificacion( integranteDTO.getIdentificacion() );

        return integrante;
    }

    @Override
    public EventoDTO toDtoEventoId(Evento evento) {
        if ( evento == null ) {
            return null;
        }

        EventoDTO eventoDTO = new EventoDTO();

        eventoDTO.setId( evento.getId() );

        return eventoDTO;
    }

    protected EventoTipo eventoTipoDTOToEventoTipo(EventoTipoDTO eventoTipoDTO) {
        if ( eventoTipoDTO == null ) {
            return null;
        }

        EventoTipo eventoTipo = new EventoTipo();

        eventoTipo.setId( eventoTipoDTO.getId() );
        eventoTipo.setNombre( eventoTipoDTO.getNombre() );
        eventoTipo.setDescripcion( eventoTipoDTO.getDescripcion() );

        return eventoTipo;
    }

    protected Set<Integrante> integranteDTOSetToIntegranteSet(Set<IntegranteDTO> set) {
        if ( set == null ) {
            return null;
        }

        Set<Integrante> set1 = new LinkedHashSet<Integrante>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( IntegranteDTO integranteDTO : set ) {
            set1.add( toEntity( integranteDTO ) );
        }

        return set1;
    }

    protected Evento eventoDTOToEvento(EventoDTO eventoDTO) {
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
        evento.eventoTipo( eventoTipoDTOToEventoTipo( eventoDTO.getEventoTipo() ) );
        evento.integrantes( integranteDTOSetToIntegranteSet( eventoDTO.getIntegrantes() ) );

        return evento;
    }

    protected Set<Evento> eventoDTOSetToEventoSet(Set<EventoDTO> set) {
        if ( set == null ) {
            return null;
        }

        Set<Evento> set1 = new LinkedHashSet<Evento>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( EventoDTO eventoDTO : set ) {
            set1.add( eventoDTOToEvento( eventoDTO ) );
        }

        return set1;
    }
}
