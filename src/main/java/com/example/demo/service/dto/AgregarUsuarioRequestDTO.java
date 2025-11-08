package com.example.demo.service.dto;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * DTO de solicitud para el endpoint POST /api/v1/agregar_usuario.
 *
 * JSON de entrada esperado:
 * {
 *   "username": "juan",
 *   "password": "juan123",
 *   "firstName": "Juan",
 *   "lastName": "Perez",
 *   "email": "juan@perez.com.ar",
 *   "nombreAlumno": "Juan Perez",
 *   "descripcionProyecto": "Proyecto de Juan Perez"
 * }
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AgregarUsuarioRequestDTO implements Serializable {

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Size(min = 4, max = 100)
    private String password;

    @NotBlank
    @Size(max = 50)
    private String firstName;

    @NotBlank
    @Size(max = 50)
    private String lastName;

    @NotBlank
    @Email
    @Size(min = 5, max = 254)
    private String email;

    @NotBlank
    @Size(max = 200)
    private String nombreAlumno;

    @Lob
    @NotBlank // agregado para reflejar que la columna es not null
    private String descripcionProyecto;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombreAlumno() {
        return nombreAlumno;
    }

    public void setNombreAlumno(String nombreAlumno) {
        this.nombreAlumno = nombreAlumno;
    }

    public String getDescripcionProyecto() {
        return descripcionProyecto;
    }

    public void setDescripcionProyecto(String descripcionProyecto) {
        this.descripcionProyecto = descripcionProyecto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AgregarUsuarioRequestDTO)) {
            return false;
        }
        AgregarUsuarioRequestDTO that = (AgregarUsuarioRequestDTO) o;
        return Objects.equals(username, that.username)
            && Objects.equals(email, that.email)
            && Objects.equals(firstName, that.firstName)
            && Objects.equals(lastName, that.lastName)
            && Objects.equals(nombreAlumno, that.nombreAlumno)
            && Objects.equals(descripcionProyecto, that.descripcionProyecto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, email, firstName, lastName, nombreAlumno, descripcionProyecto);
    }

    @Override
    public String toString() {
        return "AgregarUsuarioRequestDTO{" +
            "username='" + username + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", email='" + email + '\'' +
            ", nombreAlumno='" + nombreAlumno + '\'' +
            ", descripcionProyecto='" + descripcionProyecto + '\'' +
            '}';
    }
}
