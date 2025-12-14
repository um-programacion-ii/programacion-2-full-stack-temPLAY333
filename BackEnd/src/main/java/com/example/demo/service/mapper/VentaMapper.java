package com.example.demo.service.mapper;

import com.example.demo.domain.Evento;
import com.example.demo.domain.User;
import com.example.demo.domain.Venta;
import com.example.demo.service.dto.EventoDTO;
import com.example.demo.service.dto.UserDTO;
import com.example.demo.service.dto.VentaDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Venta} and its DTO {@link VentaDTO}.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VentaMapper extends EntityMapper<VentaDTO, Venta> {
    @Mapping(target = "usuario", source = "usuario", qualifiedByName = "userLogin")
    @Mapping(target = "evento", source = "evento", qualifiedByName = "eventoId")
    @Mapping(target = "asientos", source = "asientos")
    VentaDTO toDto(Venta s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    @Named("eventoId")
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
    EventoDTO toDtoEventoId(Evento evento);
}
