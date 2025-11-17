package com.example.demo.service.mapper;

import com.example.demo.domain.AlumnoProfile;
import com.example.demo.domain.User;
import com.example.demo.service.dto.AlumnoProfileDTO;
import com.example.demo.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link AlumnoProfile} and its DTO {@link AlumnoProfileDTO}.
 */
@Mapper(componentModel = "spring")
public interface AlumnoProfileMapper extends EntityMapper<AlumnoProfileDTO, AlumnoProfile> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    AlumnoProfileDTO toDto(AlumnoProfile s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
