package com.example.demo.service.mapper;

import com.example.demo.domain.Evento;
import com.example.demo.domain.EventoTipo;
import com.example.demo.domain.Integrante;
import com.example.demo.service.dto.EventoDTO;
import com.example.demo.service.dto.EventoTipoDTO;
import com.example.demo.service.dto.IntegranteDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Evento} and its DTO {@link EventoDTO}.
 */
@Mapper(componentModel = "spring")
public interface EventoMapper extends EntityMapper<EventoDTO, Evento> {
    @Mapping(target = "eventoTipo", source = "eventoTipo", qualifiedByName = "eventoTipoNombre")
    @Mapping(target = "integrantes", source = "integrantes", qualifiedByName = "integranteNombreSet")
    EventoDTO toDto(Evento s);

    @Mapping(target = "removeIntegrantes", ignore = true)
    Evento toEntity(EventoDTO eventoDTO);

    @Named("eventoTipoNombre")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    EventoTipoDTO toDtoEventoTipoNombre(EventoTipo eventoTipo);

    @Named("integranteNombre")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    IntegranteDTO toDtoIntegranteNombre(Integrante integrante);

    @Named("integranteNombreSet")
    default Set<IntegranteDTO> toDtoIntegranteNombreSet(Set<Integrante> integrante) {
        return integrante.stream().map(this::toDtoIntegranteNombre).collect(Collectors.toSet());
    }
}
