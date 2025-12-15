package com.example.demo.service.mapper;

import com.example.demo.service.dto.LoginRequestDTO;
import com.example.demo.service.dto.LoginResponseDTO;
import com.example.demo.web.rest.vm.LoginVM;
import org.mapstruct.*;

/**
 * Mapper para convertir entre LoginRequestDTO y el VM existente (si se mantiene) y producir LoginResponseDTO.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LoginMapper {

    @Mapping(target = "username", source = "username")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "rememberMe", source = "rememberMe")
    LoginVM toLoginVM(LoginRequestDTO dto);

    @InheritInverseConfiguration
    LoginRequestDTO toLoginRequestDTO(LoginVM vm);

    default LoginResponseDTO toLoginResponseDTO(String jwtToken) {
        return new LoginResponseDTO(jwtToken);
    }
}

