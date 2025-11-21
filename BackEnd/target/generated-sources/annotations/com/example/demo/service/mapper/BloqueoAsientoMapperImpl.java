package com.example.demo.service.mapper;

import com.example.demo.domain.Asiento;
import com.example.demo.service.dto.AsientoBloqueoEstadoDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-08T19:05:53-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (OpenLogic)"
)
@Component
public class BloqueoAsientoMapperImpl implements BloqueoAsientoMapper {

    @Override
    public AsientoBloqueoEstadoDTO toDto(Asiento asiento) {
        if ( asiento == null ) {
            return null;
        }

        AsientoBloqueoEstadoDTO asientoBloqueoEstadoDTO = new AsientoBloqueoEstadoDTO();

        asientoBloqueoEstadoDTO.setEstado( mapEstado( asiento.getEstado() ) );
        asientoBloqueoEstadoDTO.setFila( asiento.getFila() );
        asientoBloqueoEstadoDTO.setColumna( asiento.getColumna() );

        return asientoBloqueoEstadoDTO;
    }
}
