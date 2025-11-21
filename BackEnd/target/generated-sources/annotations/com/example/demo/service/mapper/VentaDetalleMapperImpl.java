package com.example.demo.service.mapper;

import com.example.demo.domain.Asiento;
import com.example.demo.domain.Evento;
import com.example.demo.domain.Venta;
import com.example.demo.service.dto.AsientoVentaEstadoDTO;
import com.example.demo.service.dto.VentaDetalleDTO;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-08T19:05:53-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (OpenLogic)"
)
@Component
public class VentaDetalleMapperImpl implements VentaDetalleMapper {

    @Override
    public VentaDetalleDTO toDto(Venta venta, Set<Asiento> asientos) {
        if ( venta == null && asientos == null ) {
            return null;
        }

        VentaDetalleDTO ventaDetalleDTO = new VentaDetalleDTO();

        if ( venta != null ) {
            ventaDetalleDTO.setEventoId( ventaEventoId( venta ) );
            ventaDetalleDTO.setVentaId( venta.getVentaId() );
            ventaDetalleDTO.setFechaVenta( venta.getFechaVenta() );
            ventaDetalleDTO.setResultado( venta.getResultado() );
            ventaDetalleDTO.setDescripcion( venta.getDescripcion() );
            ventaDetalleDTO.setPrecioVenta( venta.getPrecioVenta() );
        }
        ventaDetalleDTO.setAsientos( toDtoAsientos( asientos ) );

        return ventaDetalleDTO;
    }

    @Override
    public AsientoVentaEstadoDTO toDtoAsiento(Asiento asiento) {
        if ( asiento == null ) {
            return null;
        }

        AsientoVentaEstadoDTO asientoVentaEstadoDTO = new AsientoVentaEstadoDTO();

        asientoVentaEstadoDTO.setFila( asiento.getFila() );
        asientoVentaEstadoDTO.setColumna( asiento.getColumna() );
        asientoVentaEstadoDTO.setPersona( asiento.getPersona() );
        asientoVentaEstadoDTO.setEstado( estadoCapitalizado( asiento.getEstado() ) );

        return asientoVentaEstadoDTO;
    }

    private Long ventaEventoId(Venta venta) {
        Evento evento = venta.getEvento();
        if ( evento == null ) {
            return null;
        }
        return evento.getId();
    }
}
