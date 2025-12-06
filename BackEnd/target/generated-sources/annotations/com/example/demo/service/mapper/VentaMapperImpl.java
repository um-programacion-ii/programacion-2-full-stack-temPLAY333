package com.example.demo.service.mapper;

import com.example.demo.domain.Evento;
import com.example.demo.domain.EventoTipo;
import com.example.demo.domain.Integrante;
import com.example.demo.domain.User;
import com.example.demo.domain.Venta;
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
    date = "2025-12-05T21:27:56-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (OpenLogic)"
)
@Component
public class VentaMapperImpl implements VentaMapper {

    @Override
    public Venta toEntity(VentaDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Venta venta = new Venta();

        venta.setId( dto.getId() );
        venta.setVentaId( dto.getVentaId() );
        venta.setFechaVenta( dto.getFechaVenta() );
        venta.setResultado( dto.getResultado() );
        venta.setDescripcion( dto.getDescripcion() );
        venta.setPrecioVenta( dto.getPrecioVenta() );
        venta.usuario( userDTOToUser( dto.getUsuario() ) );
        venta.evento( eventoDTOToEvento( dto.getEvento() ) );

        return venta;
    }

    @Override
    public List<Venta> toEntity(List<VentaDTO> dtoList) {
        if ( dtoList == null ) {
            return null;
        }

        List<Venta> list = new ArrayList<Venta>( dtoList.size() );
        for ( VentaDTO ventaDTO : dtoList ) {
            list.add( toEntity( ventaDTO ) );
        }

        return list;
    }

    @Override
    public List<VentaDTO> toDto(List<Venta> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<VentaDTO> list = new ArrayList<VentaDTO>( entityList.size() );
        for ( Venta venta : entityList ) {
            list.add( toDto( venta ) );
        }

        return list;
    }

    @Override
    public void partialUpdate(Venta entity, VentaDTO dto) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getId() != null ) {
            entity.setId( dto.getId() );
        }
        if ( dto.getVentaId() != null ) {
            entity.setVentaId( dto.getVentaId() );
        }
        if ( dto.getFechaVenta() != null ) {
            entity.setFechaVenta( dto.getFechaVenta() );
        }
        if ( dto.getResultado() != null ) {
            entity.setResultado( dto.getResultado() );
        }
        if ( dto.getDescripcion() != null ) {
            entity.setDescripcion( dto.getDescripcion() );
        }
        if ( dto.getPrecioVenta() != null ) {
            entity.setPrecioVenta( dto.getPrecioVenta() );
        }
        if ( dto.getUsuario() != null ) {
            if ( entity.getUsuario() == null ) {
                entity.usuario( new User() );
            }
            userDTOToUser1( dto.getUsuario(), entity.getUsuario() );
        }
        if ( dto.getEvento() != null ) {
            if ( entity.getEvento() == null ) {
                entity.evento( new Evento() );
            }
            eventoDTOToEvento1( dto.getEvento(), entity.getEvento() );
        }
    }

    @Override
    public VentaDTO toDto(Venta s) {
        if ( s == null ) {
            return null;
        }

        VentaDTO ventaDTO = new VentaDTO();

        ventaDTO.setUsuario( toDtoUserLogin( s.getUsuario() ) );
        ventaDTO.setEvento( toDtoEventoId( s.getEvento() ) );
        ventaDTO.setId( s.getId() );
        ventaDTO.setVentaId( s.getVentaId() );
        ventaDTO.setFechaVenta( s.getFechaVenta() );
        ventaDTO.setResultado( s.getResultado() );
        ventaDTO.setDescripcion( s.getDescripcion() );
        ventaDTO.setPrecioVenta( s.getPrecioVenta() );

        return ventaDTO;
    }

    @Override
    public UserDTO toDtoUserLogin(User user) {
        if ( user == null ) {
            return null;
        }

        UserDTO userDTO = new UserDTO();

        userDTO.setId( user.getId() );
        userDTO.setLogin( user.getLogin() );

        return userDTO;
    }

    @Override
    public EventoDTO toDtoEventoId(Evento evento) {
        if ( evento == null ) {
            return null;
        }

        EventoDTO eventoDTO = new EventoDTO();

        eventoDTO.setId( evento.getId() );

        return eventoDTO;
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
}
