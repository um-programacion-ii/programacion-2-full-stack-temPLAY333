package com.example.demo.service.mapper;

import com.example.demo.domain.Evento;
import com.example.demo.domain.EventoTipo;
import com.example.demo.service.dto.EventoResumenDTO;
import com.example.demo.service.dto.EventoTipoDTO;
import org.mapstruct.*;

/**
 * Mapper para convertir Evento -> EventoResumenDTO con datos mínimos para listado.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventoResumenMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "titulo", source = "titulo")
    @Mapping(target = "resumen", source = "resumen")
    @Mapping(target = "descripcion", source = "descripcion")
    @Mapping(target = "fecha", source = "fecha")
    @Mapping(target = "precioEntrada", source = "precioEntrada")
    @Mapping(target = "eventoTipo", source = "eventoTipo", qualifiedByName = "eventoTipoNombreDescripcion")
    EventoResumenDTO toDto(Evento evento);

    @Named("eventoTipoNombreDescripcion")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    @Mapping(target = "descripcion", source = "descripcion")
    EventoTipoDTO toDtoEventoTipoNombreDescripcion(EventoTipo eventoTipo);
}

