package com.example.demo.service.mapper;

import com.example.demo.domain.Asiento;
import com.example.demo.domain.Venta;
import com.example.demo.domain.enumeration.Estado;
import com.example.demo.service.dto.AsientoVentaEstadoDTO;
import com.example.demo.service.dto.VentaDetalleDTO;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper para convertir Venta + Asientos -> VentaDetalleDTO.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VentaDetalleMapper {

    @Mapping(target = "eventoId", source = "venta.evento.id")
    @Mapping(target = "ventaId", source = "venta.ventaId")
    @Mapping(target = "fechaVenta", source = "venta.fechaVenta")
    @Mapping(target = "resultado", source = "venta.resultado")
    @Mapping(target = "descripcion", source = "venta.descripcion")
    @Mapping(target = "precioVenta", source = "venta.precioVenta")
    @Mapping(target = "asientos", source = "asientos", qualifiedByName = "asientosVenta")
    VentaDetalleDTO toDto(Venta venta, Set<Asiento> asientos);

    @Named("asientoVenta")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "fila", source = "fila")
    @Mapping(target = "columna", source = "columna")
    @Mapping(target = "persona", source = "persona")
    @Mapping(target = "estado", source = "estado", qualifiedByName = "estadoCapitalizado")
    AsientoVentaEstadoDTO toDtoAsiento(Asiento asiento);

    @Named("asientosVenta")
    default List<AsientoVentaEstadoDTO> toDtoAsientos(Set<Asiento> asientos) {
        if (asientos == null) return java.util.Collections.emptyList();
        return asientos.stream().map(this::toDtoAsiento).collect(Collectors.toList());
    }

    @Named("estadoCapitalizado")
    default String estadoCapitalizado(Estado estado) {
        if (estado == null) return null;
        String lower = estado.name().toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}

