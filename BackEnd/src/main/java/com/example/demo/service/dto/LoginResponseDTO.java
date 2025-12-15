package com.example.demo.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Objects;

/**
 * DTO de respuesta de login con el token JWT bajo la propiedad id_token.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class LoginResponseDTO implements Serializable {

    @NotBlank
    @JsonProperty("id_token")
    private String idToken;

    public LoginResponseDTO() {}

    public LoginResponseDTO(String idToken) { this.idToken = idToken; }

    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LoginResponseDTO)) return false;
        LoginResponseDTO that = (LoginResponseDTO) o;
        return Objects.equals(idToken, that.idToken);
    }

    @Override
    public int hashCode() { return Objects.hash(idToken); }

    @Override
    public String toString() { return "LoginResponseDTO{" + "idToken='" + idToken + '\'' + '}'; }
}

