package com.example.demo.service.mapper;

import com.example.demo.domain.Asiento;
import com.example.demo.domain.enumeration.Estado;
import com.example.demo.service.dto.AsientoBloqueoEstadoDTO;
import org.mapstruct.*;

/**
 * Mapper para convertir Asiento -> AsientoBloqueoEstadoDTO en el contexto de bloqueo de asientos.
 * Convierte el enum Estado a la representación textual requerida (primera letra mayúscula).
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BloqueoAsientoMapper {

    @Mapping(target = "estado", source = "estado", qualifiedByName = "estadoTexto")
    @Mapping(target = "fila", source = "fila")
    @Mapping(target = "columna", source = "columna")
    AsientoBloqueoEstadoDTO toDto(Asiento asiento);

    @Named("estadoTexto")
    default String mapEstado(Estado estado) {
        if (estado == null) return null;
        // Ajustar nombres al formato: "Ocupado", "Bloqueado", "Libre"
        String lower = estado.name().toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}

