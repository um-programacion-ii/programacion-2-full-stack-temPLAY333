package com.example.demo.service.mapper;

import com.example.demo.domain.Venta;
import com.example.demo.service.dto.VentaResumenDTO;
import org.mapstruct.*;

/**
 * Mapper para convertir Venta -> VentaResumenDTO.
 * Asume que la entidad Venta tiene relación con Evento y que la cantidad de asientos se obtiene externamente.
 */
@Mapper(componentModel = "spring")
public interface VentaResumenMapper {

    @Mapping(target = "eventoId", source = "venta.evento.id")
    @Mapping(target = "ventaId", source = "venta.ventaId")
    @Mapping(target = "fechaVenta", source = "venta.fechaVenta")
    @Mapping(target = "resultado", source = "venta.resultado")
    @Mapping(target = "descripcion", source = "venta.descripcion")
    @Mapping(target = "precioVenta", source = "venta.precioVenta")
    @Mapping(target = "cantidadAsientos", source = "cantidadAsientos")
    VentaResumenDTO toDto(Venta venta, Integer cantidadAsientos);
}

