package com.example.demo.service.mapper;

import com.example.demo.domain.Evento;
import com.example.demo.domain.Integrante;
import com.example.demo.service.dto.EventoDTO;
import com.example.demo.service.dto.IntegranteDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Integrante} and its DTO {@link IntegranteDTO}.
 */
@Mapper(componentModel = "spring")
public interface IntegranteMapper extends EntityMapper<IntegranteDTO, Integrante> {
    @Mapping(target = "eventos", source = "eventos", qualifiedByName = "eventoIdSet")
    IntegranteDTO toDto(Integrante s);

    @Mapping(target = "eventos", ignore = true)
    @Mapping(target = "removeEventos", ignore = true)
    Integrante toEntity(IntegranteDTO integranteDTO);

    @Named("eventoId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    EventoDTO toDtoEventoId(Evento evento);

    @Named("eventoIdSet")
    default Set<EventoDTO> toDtoEventoIdSet(Set<Evento> evento) {
        return evento.stream().map(this::toDtoEventoId).collect(Collectors.toSet());
    }
}
