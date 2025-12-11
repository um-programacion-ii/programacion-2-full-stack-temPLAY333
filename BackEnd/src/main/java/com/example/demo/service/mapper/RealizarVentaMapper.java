package com.example.demo.service.mapper;

import com.example.demo.domain.Asiento;
import com.example.demo.domain.Venta;
import com.example.demo.domain.enumeration.Estado;
import com.example.demo.service.dto.AsientoVentaEstadoDTO;
import com.example.demo.service.dto.RealizarVentaResponseDTO;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper para construir la respuesta de realizar venta.
 * Asume que la lógica de negocio determina el resultado y estados de los asientos.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RealizarVentaMapper {

    @Mapping(target = "eventoId", source = "venta.evento.id")
    @Mapping(target = "ventaId", source = "venta.ventaId")
    @Mapping(target = "fechaVenta", source = "venta.fechaVenta")
    @Mapping(target = "resultado", source = "venta.resultado")
    @Mapping(target = "descripcion", source = "venta.descripcion")
    @Mapping(target = "precioVenta", source = "venta.precioVenta")
    @Mapping(target = "asientos", source = "asientos", qualifiedByName = "asientoVentaEstadoList")
    RealizarVentaResponseDTO toDto(Venta venta, Set<Asiento> asientos);

    @Named("asientoVentaEstado")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "fila", source = "fila")
    @Mapping(target = "columna", source = "columna")
    @Mapping(target = "persona", source = "persona")
    @Mapping(target = "estado", source = "estado", qualifiedByName = "estadoTextoVenta")
    AsientoVentaEstadoDTO toDtoAsientoVentaEstado(Asiento asiento);

    @Named("asientoVentaEstadoList")
    default List<AsientoVentaEstadoDTO> toDtoAsientoVentaEstadoList(Set<Asiento> asientos) {
        return asientos.stream().map(this::toDtoAsientoVentaEstado).collect(Collectors.toList());
    }

    @Named("estadoTextoVenta")
    default String mapEstadoVenta(Estado estado) {
        if (estado == null) return null;
        String lower = estado.name().toLowerCase();
        // Capitalizar primera letra (OCUPADO -> Ocupado, LIBRE -> Libre, BLOQUEADO -> Bloqueado)
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}

