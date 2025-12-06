package com.example.demo.service.mapper;

import com.example.demo.domain.EventoTipo;
import com.example.demo.service.dto.EventoTipoDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link EventoTipo} and its DTO {@link EventoTipoDTO}.
 */
@Mapper(componentModel = "spring")
public interface EventoTipoMapper extends EntityMapper<EventoTipoDTO, EventoTipo> {}
