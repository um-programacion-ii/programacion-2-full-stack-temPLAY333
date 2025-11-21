package com.example.demo.service.mapper;

import com.example.demo.domain.AlumnoProfile;
import com.example.demo.domain.User;
import com.example.demo.service.dto.AlumnoProfileDTO;
import com.example.demo.service.dto.UserDTO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-08T19:05:53-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (OpenLogic)"
)
@Component
public class AlumnoProfileMapperImpl implements AlumnoProfileMapper {

    @Override
    public AlumnoProfile toEntity(AlumnoProfileDTO dto) {
        if ( dto == null ) {
            return null;
        }

        AlumnoProfile alumnoProfile = new AlumnoProfile();

        alumnoProfile.setId( dto.getId() );
        alumnoProfile.setNombreAlumno( dto.getNombreAlumno() );
        alumnoProfile.setDescripcionProyecto( dto.getDescripcionProyecto() );
        alumnoProfile.user( userDTOToUser( dto.getUser() ) );

        return alumnoProfile;
    }

    @Override
    public List<AlumnoProfile> toEntity(List<AlumnoProfileDTO> dtoList) {
        if ( dtoList == null ) {
            return null;
        }

        List<AlumnoProfile> list = new ArrayList<AlumnoProfile>( dtoList.size() );
        for ( AlumnoProfileDTO alumnoProfileDTO : dtoList ) {
            list.add( toEntity( alumnoProfileDTO ) );
        }

        return list;
    }

    @Override
    public List<AlumnoProfileDTO> toDto(List<AlumnoProfile> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<AlumnoProfileDTO> list = new ArrayList<AlumnoProfileDTO>( entityList.size() );
        for ( AlumnoProfile alumnoProfile : entityList ) {
            list.add( toDto( alumnoProfile ) );
        }

        return list;
    }

    @Override
    public void partialUpdate(AlumnoProfile entity, AlumnoProfileDTO dto) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getId() != null ) {
            entity.setId( dto.getId() );
        }
        if ( dto.getNombreAlumno() != null ) {
            entity.setNombreAlumno( dto.getNombreAlumno() );
        }
        if ( dto.getDescripcionProyecto() != null ) {
            entity.setDescripcionProyecto( dto.getDescripcionProyecto() );
        }
        if ( dto.getUser() != null ) {
            if ( entity.getUser() == null ) {
                entity.user( new User() );
            }
            userDTOToUser1( dto.getUser(), entity.getUser() );
        }
    }

    @Override
    public AlumnoProfileDTO toDto(AlumnoProfile s) {
        if ( s == null ) {
            return null;
        }

        AlumnoProfileDTO alumnoProfileDTO = new AlumnoProfileDTO();

        alumnoProfileDTO.setUser( toDtoUserLogin( s.getUser() ) );
        alumnoProfileDTO.setId( s.getId() );
        alumnoProfileDTO.setNombreAlumno( s.getNombreAlumno() );
        alumnoProfileDTO.setDescripcionProyecto( s.getDescripcionProyecto() );

        return alumnoProfileDTO;
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

    protected User userDTOToUser(UserDTO userDTO) {
        if ( userDTO == null ) {
            return null;
        }

        User user = new User();

        user.setId( userDTO.getId() );
        user.setLogin( userDTO.getLogin() );

        return user;
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
}
