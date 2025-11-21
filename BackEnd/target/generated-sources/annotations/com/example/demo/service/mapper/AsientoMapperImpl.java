package com.example.demo.service.mapper;

import com.example.demo.domain.Asiento;
import com.example.demo.domain.Evento;
import com.example.demo.domain.EventoTipo;
import com.example.demo.domain.Integrante;
import com.example.demo.domain.User;
import com.example.demo.domain.Venta;
import com.example.demo.service.dto.AsientoDTO;
import com.example.demo.service.dto.EventoDTO;
import com.example.demo.service.dto.EventoTipoDTO;
import com.example.demo.service.dto.IntegranteDTO;
import com.example.demo.service.dto.UserDTO;
import com.example.demo.service.dto.VentaDTO;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-08T19:05:53-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (OpenLogic)"
)
@Component
public class AsientoMapperImpl implements AsientoMapper {

    @Override
    public Asiento toEntity(AsientoDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Asiento asiento = new Asiento();

        asiento.setId( dto.getId() );
        asiento.setFila( dto.getFila() );
        asiento.setColumna( dto.getColumna() );
        asiento.setPersona( dto.getPersona() );
        asiento.setEstado( dto.getEstado() );
        asiento.venta( ventaDTOToVenta( dto.getVenta() ) );

        return asiento;
    }

    @Override
    public List<Asiento> toEntity(List<AsientoDTO> dtoList) {
        if ( dtoList == null ) {
            return null;
        }

        List<Asiento> list = new ArrayList<Asiento>( dtoList.size() );
        for ( AsientoDTO asientoDTO : dtoList ) {
            list.add( toEntity( asientoDTO ) );
        }

        return list;
    }

    @Override
    public List<AsientoDTO> toDto(List<Asiento> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<AsientoDTO> list = new ArrayList<AsientoDTO>( entityList.size() );
        for ( Asiento asiento : entityList ) {
            list.add( toDto( asiento ) );
        }

        return list;
    }

    @Override
    public void partialUpdate(Asiento entity, AsientoDTO dto) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getId() != null ) {
            entity.setId( dto.getId() );
        }
        if ( dto.getFila() != null ) {
            entity.setFila( dto.getFila() );
        }
        if ( dto.getColumna() != null ) {
            entity.setColumna( dto.getColumna() );
        }
        if ( dto.getPersona() != null ) {
            entity.setPersona( dto.getPersona() );
        }
        if ( dto.getEstado() != null ) {
            entity.setEstado( dto.getEstado() );
        }
        if ( dto.getVenta() != null ) {
            if ( entity.getVenta() == null ) {
                entity.venta( new Venta() );
            }
            ventaDTOToVenta1( dto.getVenta(), entity.getVenta() );
        }
    }

    @Override
    public AsientoDTO toDto(Asiento s) {
        if ( s == null ) {
            return null;
        }

        AsientoDTO asientoDTO = new AsientoDTO();

        asientoDTO.setVenta( toDtoVentaId( s.getVenta() ) );
        asientoDTO.setId( s.getId() );
        asientoDTO.setFila( s.getFila() );
        asientoDTO.setColumna( s.getColumna() );
        asientoDTO.setPersona( s.getPersona() );
        asientoDTO.setEstado( s.getEstado() );

        return asientoDTO;
    }

    @Override
    public VentaDTO toDtoVentaId(Venta venta) {
        if ( venta == null ) {
            return null;
        }

        VentaDTO ventaDTO = new VentaDTO();

        ventaDTO.setId( venta.getId() );

        return ventaDTO;
    }

    protected User userDTOToUser(UserDTO userDTO) {
        if ( userDTO == null ) {
            return null;
        }

        User user = new User();

        user.setId( userDTO.getId() );
        user.setLogin( userDTO.getLogin() );

        return user;
    }

    protected EventoTipo eventoTipoDTOToEventoTipo(EventoTipoDTO eventoTipoDTO) {
        if ( eventoTipoDTO == null ) {
            return null;
        }

        EventoTipo eventoTipo = new EventoTipo();

        eventoTipo.setId( eventoTipoDTO.getId() );
        eventoTipo.setNombre( eventoTipoDTO.getNombre() );
        eventoTipo.setDescripcion( eventoTipoDTO.getDescripcion() );

        return eventoTipo;
    }

    protected Set<Evento> eventoDTOSetToEventoSet(Set<EventoDTO> set) {
        if ( set == null ) {
            return null;
        }

        Set<Evento> set1 = new LinkedHashSet<Evento>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( EventoDTO eventoDTO : set ) {
            set1.add( eventoDTOToEvento( eventoDTO ) );
        }

        return set1;
    }

    protected Integrante integranteDTOToIntegrante(IntegranteDTO integranteDTO) {
        if ( integranteDTO == null ) {
            return null;
        }

        Integrante integrante = new Integrante();

        integrante.setId( integranteDTO.getId() );
        integrante.setNombre( integranteDTO.getNombre() );
        integrante.setApellido( integranteDTO.getApellido() );
        integrante.setIdentificacion( integranteDTO.getIdentificacion() );
        integrante.eventos( eventoDTOSetToEventoSet( integranteDTO.getEventos() ) );

        return integrante;
    }

    protected Set<Integrante> integranteDTOSetToIntegranteSet(Set<IntegranteDTO> set) {
        if ( set == null ) {
            return null;
        }

        Set<Integrante> set1 = new LinkedHashSet<Integrante>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( IntegranteDTO integranteDTO : set ) {
            set1.add( integranteDTOToIntegrante( integranteDTO ) );
        }

        return set1;
    }

    protected Evento eventoDTOToEvento(EventoDTO eventoDTO) {
        if ( eventoDTO == null ) {
            return null;
        }

        Evento evento = new Evento();

        evento.setId( eventoDTO.getId() );
        evento.setTitulo( eventoDTO.getTitulo() );
        evento.setResumen( eventoDTO.getResumen() );
        evento.setDescripcion( eventoDTO.getDescripcion() );
        evento.setFecha( eventoDTO.getFecha() );
        evento.setDireccion( eventoDTO.getDireccion() );
        evento.setImagen( eventoDTO.getImagen() );
        evento.setFilaAsientos( eventoDTO.getFilaAsientos() );
        evento.setColumnAsientos( eventoDTO.getColumnAsientos() );
        evento.setPrecioEntrada( eventoDTO.getPrecioEntrada() );
        evento.eventoTipo( eventoTipoDTOToEventoTipo( eventoDTO.getEventoTipo() ) );
        evento.integrantes( integranteDTOSetToIntegranteSet( eventoDTO.getIntegrantes() ) );

        return evento;
    }

    protected Venta ventaDTOToVenta(VentaDTO ventaDTO) {
        if ( ventaDTO == null ) {
            return null;
        }

        Venta venta = new Venta();

        venta.setId( ventaDTO.getId() );
        venta.setVentaId( ventaDTO.getVentaId() );
        venta.setFechaVenta( ventaDTO.getFechaVenta() );
        venta.setResultado( ventaDTO.getResultado() );
        venta.setDescripcion( ventaDTO.getDescripcion() );
        venta.setPrecioVenta( ventaDTO.getPrecioVenta() );
        venta.usuario( userDTOToUser( ventaDTO.getUsuario() ) );
        venta.evento( eventoDTOToEvento( ventaDTO.getEvento() ) );

        return venta;
    }

    protected void userDTOToUser1(UserDTO userDTO, User mappingTarget) {
        if ( userDTO == null ) {
            return;
        }

        if ( userDTO.getId() != null ) {
            mappingTarget.setId( userDTO.getId() );
        }
        if ( userDTO.getLogin() != null ) {
            mappingTarget.setLogin( userDTO.getLogin() );
        }
    }

    protected void eventoTipoDTOToEventoTipo1(EventoTipoDTO eventoTipoDTO, EventoTipo mappingTarget) {
        if ( eventoTipoDTO == null ) {
            return;
        }

        if ( eventoTipoDTO.getId() != null ) {
            mappingTarget.setId( eventoTipoDTO.getId() );
        }
        if ( eventoTipoDTO.getNombre() != null ) {
            mappingTarget.setNombre( eventoTipoDTO.getNombre() );
        }
        if ( eventoTipoDTO.getDescripcion() != null ) {
            mappingTarget.setDescripcion( eventoTipoDTO.getDescripcion() );
        }
    }

    protected void eventoDTOToEvento1(EventoDTO eventoDTO, Evento mappingTarget) {
        if ( eventoDTO == null ) {
            return;
        }

        if ( eventoDTO.getId() != null ) {
            mappingTarget.setId( eventoDTO.getId() );
        }
        if ( eventoDTO.getTitulo() != null ) {
            mappingTarget.setTitulo( eventoDTO.getTitulo() );
        }
        if ( eventoDTO.getResumen() != null ) {
            mappingTarget.setResumen( eventoDTO.getResumen() );
        }
        if ( eventoDTO.getDescripcion() != null ) {
            mappingTarget.setDescripcion( eventoDTO.getDescripcion() );
        }
        if ( eventoDTO.getFecha() != null ) {
            mappingTarget.setFecha( eventoDTO.getFecha() );
        }
        if ( eventoDTO.getDireccion() != null ) {
            mappingTarget.setDireccion( eventoDTO.getDireccion() );
        }
        if ( eventoDTO.getImagen() != null ) {
            mappingTarget.setImagen( eventoDTO.getImagen() );
        }
        if ( eventoDTO.getFilaAsientos() != null ) {
            mappingTarget.setFilaAsientos( eventoDTO.getFilaAsientos() );
        }
        if ( eventoDTO.getColumnAsientos() != null ) {
            mappingTarget.setColumnAsientos( eventoDTO.getColumnAsientos() );
        }
        if ( eventoDTO.getPrecioEntrada() != null ) {
            mappingTarget.setPrecioEntrada( eventoDTO.getPrecioEntrada() );
        }
        if ( eventoDTO.getEventoTipo() != null ) {
            if ( mappingTarget.getEventoTipo() == null ) {
                mappingTarget.eventoTipo( new EventoTipo() );
            }
            eventoTipoDTOToEventoTipo1( eventoDTO.getEventoTipo(), mappingTarget.getEventoTipo() );
        }
        if ( mappingTarget.getIntegrantes() != null ) {
            Set<Integrante> set = integranteDTOSetToIntegranteSet( eventoDTO.getIntegrantes() );
            if ( set != null ) {
                mappingTarget.getIntegrantes().clear();
                mappingTarget.getIntegrantes().addAll( set );
            }
        }
        else {
            Set<Integrante> set = integranteDTOSetToIntegranteSet( eventoDTO.getIntegrantes() );
            if ( set != null ) {
                mappingTarget.integrantes( set );
            }
        }
    }

    protected void ventaDTOToVenta1(VentaDTO ventaDTO, Venta mappingTarget) {
        if ( ventaDTO == null ) {
            return;
        }

        if ( ventaDTO.getId() != null ) {
            mappingTarget.setId( ventaDTO.getId() );
        }
        if ( ventaDTO.getVentaId() != null ) {
            mappingTarget.setVentaId( ventaDTO.getVentaId() );
        }
        if ( ventaDTO.getFechaVenta() != null ) {
            mappingTarget.setFechaVenta( ventaDTO.getFechaVenta() );
        }
        if ( ventaDTO.getResultado() != null ) {
            mappingTarget.setResultado( ventaDTO.getResultado() );
        }
        if ( ventaDTO.getDescripcion() != null ) {
            mappingTarget.setDescripcion( ventaDTO.getDescripcion() );
        }
        if ( ventaDTO.getPrecioVenta() != null ) {
            mappingTarget.setPrecioVenta( ventaDTO.getPrecioVenta() );
        }
        if ( ventaDTO.getUsuario() != null ) {
            if ( mappingTarget.getUsuario() == null ) {
                mappingTarget.usuario( new User() );
            }
            userDTOToUser1( ventaDTO.getUsuario(), mappingTarget.getUsuario() );
        }
        if ( ventaDTO.getEvento() != null ) {
            if ( mappingTarget.getEvento() == null ) {
                mappingTarget.evento( new Evento() );
            }
            eventoDTOToEvento1( ventaDTO.getEvento(), mappingTarget.getEvento() );
        }
    }
}
