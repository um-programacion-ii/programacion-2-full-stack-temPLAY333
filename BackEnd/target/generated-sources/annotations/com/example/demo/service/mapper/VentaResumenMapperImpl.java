package com.example.demo.service.mapper;

import com.example.demo.domain.Evento;
import com.example.demo.domain.Venta;
import com.example.demo.service.dto.VentaResumenDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-08T19:05:53-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (OpenLogic)"
)
@Component
public class VentaResumenMapperImpl implements VentaResumenMapper {

    @Override
    public VentaResumenDTO toDto(Venta venta, Integer cantidadAsientos) {
        if ( venta == null && cantidadAsientos == null ) {
            return null;
        }

        VentaResumenDTO ventaResumenDTO = new VentaResumenDTO();

        if ( venta != null ) {
            ventaResumenDTO.setEventoId( ventaEventoId( venta ) );
            ventaResumenDTO.setVentaId( venta.getVentaId() );
            ventaResumenDTO.setFechaVenta( venta.getFechaVenta() );
            ventaResumenDTO.setResultado( venta.getResultado() );
            ventaResumenDTO.setDescripcion( venta.getDescripcion() );
            ventaResumenDTO.setPrecioVenta( venta.getPrecioVenta() );
        }
        ventaResumenDTO.setCantidadAsientos( cantidadAsientos );

        return ventaResumenDTO;
    }

    private Long ventaEventoId(Venta venta) {
        Evento evento = venta.getEvento();
        if ( evento == null ) {
            return null;
        }
        return evento.getId();
    }
}
