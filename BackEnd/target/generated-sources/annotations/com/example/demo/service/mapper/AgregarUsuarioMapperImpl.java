package com.example.demo.service.mapper;

import com.example.demo.domain.AlumnoProfile;
import com.example.demo.domain.User;
import com.example.demo.service.dto.AgregarUsuarioRequestDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-05T21:27:56-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (OpenLogic)"
)
@Component
public class AgregarUsuarioMapperImpl implements AgregarUsuarioMapper {

    @Override
    public User toUser(AgregarUsuarioRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        User user = new User();

        user.setLogin( dto.getUsername() );
        user.setFirstName( dto.getFirstName() );
        user.setLastName( dto.getLastName() );
        user.setEmail( dto.getEmail() );

        return user;
    }

    @Override
    public AlumnoProfile toAlumnoProfile(AgregarUsuarioRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        AlumnoProfile alumnoProfile = new AlumnoProfile();

        alumnoProfile.setNombreAlumno( dto.getNombreAlumno() );
        alumnoProfile.setDescripcionProyecto( dto.getDescripcionProyecto() );

        return alumnoProfile;
    }

    @Override
    public void linkUser(AlumnoProfile profile, User user) {
        if ( user == null ) {
            return;
        }

        profile.user( user );
    }
}
