package com.example.demo.service.mapper;

import com.example.demo.domain.AlumnoProfile;
import com.example.demo.domain.User;
import com.example.demo.service.dto.AgregarUsuarioRequestDTO;
import org.mapstruct.*;

/**
 * Mapper para el endpoint de alta de usuario: transforma el request en entidades de dominio.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AgregarUsuarioMapper {

    /**
     * Mapea campos de usuario. No setea el password (debe ser encriptado por el servicio).
     */
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "login", source = "username")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", source = "email")
    User toUser(AgregarUsuarioRequestDTO dto);

    /**
     * Mapea datos del perfil de alumno. La asociación a User se realiza luego.
     */
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "nombreAlumno", source = "nombreAlumno")
    @Mapping(target = "descripcionProyecto", source = "descripcionProyecto")
    AlumnoProfile toAlumnoProfile(AgregarUsuarioRequestDTO dto);

    /**
     * Vincula el User al AlumnoProfile construido previamente.
     */
    @Mapping(target = "user", source = "user")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nombreAlumno", ignore = true)
    @Mapping(target = "descripcionProyecto", ignore = true)
    void linkUser(@MappingTarget AlumnoProfile profile, User user);
}

