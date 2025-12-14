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
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AsientoMapper.class, EventoMapper.class})
public interface VentaMapper extends EntityMapper<VentaDTO, Venta> {
    @Mapping(target = "usuario", source = "usuario", qualifiedByName = "userLogin")
    // Map evento using EventoMapper to include event fields (titulo, resumen, imagen, etc.)
    @Mapping(target = "evento", source = "evento")
    @Mapping(target = "asientos", source = "asientos")
    VentaDTO toDto(Venta s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    @Named("eventoId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    EventoDTO toDtoEventoId(Evento evento);
}
