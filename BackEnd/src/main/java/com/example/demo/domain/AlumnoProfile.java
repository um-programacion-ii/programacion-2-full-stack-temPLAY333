package com.example.demo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A AlumnoProfile.
 */
@Entity
@Table(name = "alumno_profile")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AlumnoProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 200)
    @Column(name = "nombre_alumno", length = 200, nullable = false)
    private String nombreAlumno;

    @Lob
    @Column(name = "descripcion_proyecto", nullable = false)
    private String descripcionProyecto;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @NotNull
    @JoinColumn(unique = true)
    private User user;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AlumnoProfile id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreAlumno() {
        return this.nombreAlumno;
    }

    public AlumnoProfile nombreAlumno(String nombreAlumno) {
        this.setNombreAlumno(nombreAlumno);
        return this;
    }

    public void setNombreAlumno(String nombreAlumno) {
        this.nombreAlumno = nombreAlumno;
    }

    public String getDescripcionProyecto() {
        return this.descripcionProyecto;
    }

    public AlumnoProfile descripcionProyecto(String descripcionProyecto) {
        this.setDescripcionProyecto(descripcionProyecto);
        return this;
    }

    public void setDescripcionProyecto(String descripcionProyecto) {
        this.descripcionProyecto = descripcionProyecto;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public AlumnoProfile user(User user) {
        this.setUser(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AlumnoProfile)) {
            return false;
        }
        return getId() != null && getId().equals(((AlumnoProfile) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AlumnoProfile{" +
            "id=" + getId() +
            ", nombreAlumno='" + getNombreAlumno() + "'" +
            ", descripcionProyecto='" + getDescripcionProyecto() + "'" +
            "}";
    }
}
