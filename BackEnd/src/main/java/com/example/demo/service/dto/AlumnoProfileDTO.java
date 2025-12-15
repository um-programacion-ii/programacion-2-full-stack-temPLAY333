package com.example.demo.service.dto;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.example.demo.domain.AlumnoProfile} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AlumnoProfileDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 200)
    private String nombreAlumno;

    @Lob
    private String descripcionProyecto;

    @NotNull
    private UserDTO user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AlumnoProfileDTO)) {
            return false;
        }

        AlumnoProfileDTO alumnoProfileDTO = (AlumnoProfileDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, alumnoProfileDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AlumnoProfileDTO{" +
            "id=" + getId() +
            ", nombreAlumno='" + getNombreAlumno() + "'" +
            ", descripcionProyecto='" + getDescripcionProyecto() + "'" +
            ", user=" + getUser() +
            "}";
    }
}
