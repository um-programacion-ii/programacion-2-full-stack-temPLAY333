package com.example.demo.service.mapper;

import com.example.demo.domain.Evento;
import com.example.demo.domain.EventoTipo;
import com.example.demo.domain.Integrante;
import com.example.demo.service.dto.*;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper para convertir Evento -> EventoDetalleDTO según el contrato del endpoint /api/endpoints/v1/evento/{id}.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventoDetalleMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "titulo", source = "titulo")
    @Mapping(target = "resumen", source = "resumen")
    @Mapping(target = "descripcion", source = "descripcion")
    @Mapping(target = "fecha", source = "fecha")
    @Mapping(target = "direccion", source = "direccion")
    @Mapping(target = "imagen", source = "imagen")
    @Mapping(target = "filaAsientos", source = "filaAsientos")
    @Mapping(target = "columnAsientos", source = "columnAsientos")
    @Mapping(target = "precioEntrada", source = "precioEntrada")
    @Mapping(target = "eventoTipo", source = "eventoTipo", qualifiedByName = "eventoTipoBasic")
    @Mapping(target = "integrantes", source = "integrantes", qualifiedByName = "integranteBasicList")
    EventoDetalleDTO toDto(Evento evento);

    @Named("eventoTipoBasic")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "nombre", source = "nombre")
    @Mapping(target = "descripcion", source = "descripcion")
    EventoTipoBasicDTO toDtoEventoTipoBasic(EventoTipo eventoTipo);

    @Named("integranteBasic")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "nombre", source = "nombre")
    @Mapping(target = "apellido", source = "apellido")
    @Mapping(target = "identificacion", source = "identificacion")
    IntegranteBasicDTO toDtoIntegranteBasic(Integrante integrante);

    @Named("integranteBasicList")
    default List<IntegranteBasicDTO> toDtoIntegranteBasicList(java.util.Set<Integrante> integrantes) {
        return integrantes.stream().map(this::toDtoIntegranteBasic).collect(Collectors.toList());
    }
}

