package com.example.demo.service.mapper;

import com.example.demo.domain.EventoTipo;
import com.example.demo.service.dto.EventoTipoDTO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-08T19:05:53-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (OpenLogic)"
)
@Component
public class EventoTipoMapperImpl implements EventoTipoMapper {

    @Override
    public EventoTipo toEntity(EventoTipoDTO dto) {
        if ( dto == null ) {
            return null;
        }

        EventoTipo eventoTipo = new EventoTipo();

        eventoTipo.setId( dto.getId() );
        eventoTipo.setNombre( dto.getNombre() );
        eventoTipo.setDescripcion( dto.getDescripcion() );

        return eventoTipo;
    }

    @Override
    public EventoTipoDTO toDto(EventoTipo entity) {
        if ( entity == null ) {
            return null;
        }

        EventoTipoDTO eventoTipoDTO = new EventoTipoDTO();

        eventoTipoDTO.setId( entity.getId() );
        eventoTipoDTO.setNombre( entity.getNombre() );
        eventoTipoDTO.setDescripcion( entity.getDescripcion() );

        return eventoTipoDTO;
    }

    @Override
    public List<EventoTipo> toEntity(List<EventoTipoDTO> dtoList) {
        if ( dtoList == null ) {
            return null;
        }

        List<EventoTipo> list = new ArrayList<EventoTipo>( dtoList.size() );
        for ( EventoTipoDTO eventoTipoDTO : dtoList ) {
            list.add( toEntity( eventoTipoDTO ) );
        }

        return list;
    }

    @Override
    public List<EventoTipoDTO> toDto(List<EventoTipo> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<EventoTipoDTO> list = new ArrayList<EventoTipoDTO>( entityList.size() );
        for ( EventoTipo eventoTipo : entityList ) {
            list.add( toDto( eventoTipo ) );
        }

        return list;
    }

    @Override
    public void partialUpdate(EventoTipo entity, EventoTipoDTO dto) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getId() != null ) {
            entity.setId( dto.getId() );
        }
        if ( dto.getNombre() != null ) {
            entity.setNombre( dto.getNombre() );
        }
        if ( dto.getDescripcion() != null ) {
            entity.setDescripcion( dto.getDescripcion() );
        }
    }
}
