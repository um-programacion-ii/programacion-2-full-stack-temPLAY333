package com.example.demo.service.mapper;

import com.example.demo.domain.Asiento;
import com.example.demo.domain.Evento;
import com.example.demo.domain.Venta;
import com.example.demo.service.dto.AsientoVentaEstadoDTO;
import com.example.demo.service.dto.RealizarVentaResponseDTO;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-05T21:27:55-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (OpenLogic)"
)
@Component
public class RealizarVentaMapperImpl implements RealizarVentaMapper {

    @Override
    public RealizarVentaResponseDTO toDto(Venta venta, Set<Asiento> asientos) {
        if ( venta == null && asientos == null ) {
            return null;
        }

        RealizarVentaResponseDTO realizarVentaResponseDTO = new RealizarVentaResponseDTO();

        if ( venta != null ) {
            realizarVentaResponseDTO.setEventoId( ventaEventoId( venta ) );
            realizarVentaResponseDTO.setVentaId( venta.getVentaId() );
            realizarVentaResponseDTO.setFechaVenta( venta.getFechaVenta() );
            realizarVentaResponseDTO.setResultado( venta.getResultado() );
            realizarVentaResponseDTO.setDescripcion( venta.getDescripcion() );
            realizarVentaResponseDTO.setPrecioVenta( venta.getPrecioVenta() );
        }
        realizarVentaResponseDTO.setAsientos( toDtoAsientoVentaEstadoList( asientos ) );

        return realizarVentaResponseDTO;
    }

    @Override
    public AsientoVentaEstadoDTO toDtoAsientoVentaEstado(Asiento asiento) {
        if ( asiento == null ) {
            return null;
        }

        AsientoVentaEstadoDTO asientoVentaEstadoDTO = new AsientoVentaEstadoDTO();

        asientoVentaEstadoDTO.setFila( asiento.getFila() );
        asientoVentaEstadoDTO.setColumna( asiento.getColumna() );
        asientoVentaEstadoDTO.setPersona( asiento.getPersona() );
        asientoVentaEstadoDTO.setEstado( mapEstadoVenta( asiento.getEstado() ) );

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
