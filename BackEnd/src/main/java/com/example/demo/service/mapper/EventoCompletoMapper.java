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
 * Mapper para convertir Evento -> EventoDTO con todos los datos necesarios para el listado completo de eventos activos.
 *
 * - eventoTipo: incluye id, nombre y descripcion
 * - integrantes: incluye id, nombre, apellido e identificacion (evita recursión mapeando sólo estos campos)
 */
@Mapper(componentModel = "spring")
public interface EventoCompletoMapper {

    @Mapping(target = "eventoTipo", source = "eventoTipo", qualifiedByName = "eventoTipoNombreDescripcion")
    @Mapping(target = "integrantes", source = "integrantes", qualifiedByName = "integranteBasicoSet")
    EventoDTO toDto(Evento s);

    @Named("eventoTipoNombreDescripcion")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    @Mapping(target = "descripcion", source = "descripcion")
    EventoTipoDTO toDtoEventoTipoNombreDescripcion(EventoTipo eventoTipo);

    @Named("integranteBasico")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    @Mapping(target = "apellido", source = "apellido")
    @Mapping(target = "identificacion", source = "identificacion")
    IntegranteDTO toDtoIntegranteBasico(Integrante integrante);

    @Named("integranteBasicoSet")
    default Set<IntegranteDTO> toDtoIntegranteBasicoSet(Set<Integrante> integrantes) {
        return integrantes.stream().map(this::toDtoIntegranteBasico).collect(Collectors.toSet());
    }
}

