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
    @Mapping(target = "imagen", source = "imagen")
    @Mapping(target = "eventoTipo", source = "eventoTipo", qualifiedByName = "eventoTipoNombreDescripcion", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    EventoResumenDTO toDto(Evento evento);

    @Named("eventoTipoNombreDescripcion")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    @Mapping(target = "descripcion", source = "descripcion")
    default EventoTipoDTO toDtoEventoTipoNombreDescripcion(EventoTipo eventoTipo) {
        if (eventoTipo == null) {
            return null;
        }
        try {
            EventoTipoDTO dto = new EventoTipoDTO();
            dto.setId(eventoTipo.getId());
            dto.setNombre(eventoTipo.getNombre());
            dto.setDescripcion(eventoTipo.getDescripcion());
            return dto;
        } catch (org.hibernate.LazyInitializationException e) {
            // Si el EventoTipo es un proxy lazy que no se puede inicializar, retornar null
            return null;
        }
    }
}
