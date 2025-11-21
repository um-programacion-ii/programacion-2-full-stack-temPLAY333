package com.example.demo.service.mapper;

import com.example.demo.service.dto.LoginRequestDTO;
import com.example.demo.web.rest.vm.LoginVM;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-08T19:05:53-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (OpenLogic)"
)
@Component
public class LoginMapperImpl implements LoginMapper {

    @Override
    public LoginVM toLoginVM(LoginRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        LoginVM loginVM = new LoginVM();

        loginVM.setUsername( dto.getUsername() );
        loginVM.setPassword( dto.getPassword() );
        loginVM.setRememberMe( dto.isRememberMe() );

        return loginVM;
    }

    @Override
    public LoginRequestDTO toLoginRequestDTO(LoginVM vm) {
        if ( vm == null ) {
            return null;
        }

        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();

        loginRequestDTO.setUsername( vm.getUsername() );
        loginRequestDTO.setPassword( vm.getPassword() );
        loginRequestDTO.setRememberMe( vm.isRememberMe() );

        return loginRequestDTO;
    }
}
