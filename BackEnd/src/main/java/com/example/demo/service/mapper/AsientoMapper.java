package com.example.demo.service.mapper;

import com.example.demo.domain.Asiento;
import com.example.demo.domain.Venta;
import com.example.demo.service.dto.AsientoDTO;
import com.example.demo.service.dto.VentaDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Asiento} and its DTO {@link AsientoDTO}.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AsientoMapper extends EntityMapper<AsientoDTO, Asiento> {
    @Mapping(target = "venta", source = "venta", qualifiedByName = "ventaId")
    AsientoDTO toDto(Asiento s);

    @Named("ventaId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    VentaDTO toDtoVentaId(Venta venta);
}
