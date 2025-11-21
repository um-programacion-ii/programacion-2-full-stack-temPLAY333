package com.example.demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Integrante.
 */
@Entity
@Table(name = "integrante")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Integrante implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 100)
    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @NotNull
    @Size(max = 100)
    @Column(name = "apellido", length = 100, nullable = false)
    private String apellido;

    @NotNull
    @Size(max = 50)
    @Column(name = "identificacion", length = 50, nullable = false)
    private String identificacion;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "integrantes")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "eventoTipo", "integrantes" }, allowSetters = true)
    private Set<Evento> eventos = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Integrante id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return this.nombre;
    }

    public Integrante nombre(String nombre) {
        this.setNombre(nombre);
        return this;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return this.apellido;
    }

    public Integrante apellido(String apellido) {
        this.setApellido(apellido);
        return this;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getIdentificacion() {
        return this.identificacion;
    }

    public Integrante identificacion(String identificacion) {
        this.setIdentificacion(identificacion);
        return this;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public Set<Evento> getEventos() {
        return this.eventos;
    }

    public void setEventos(Set<Evento> eventos) {
        if (this.eventos != null) {
            this.eventos.forEach(i -> i.removeIntegrantes(this));
        }
        if (eventos != null) {
            eventos.forEach(i -> i.addIntegrantes(this));
        }
        this.eventos = eventos;
    }

    public Integrante eventos(Set<Evento> eventos) {
        this.setEventos(eventos);
        return this;
    }

    public Integrante addEventos(Evento evento) {
        this.eventos.add(evento);
        evento.getIntegrantes().add(this);
        return this;
    }

    public Integrante removeEventos(Evento evento) {
        this.eventos.remove(evento);
        evento.getIntegrantes().remove(this);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Integrante)) {
            return false;
        }
        return getId() != null && getId().equals(((Integrante) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Integrante{" +
            "id=" + getId() +
            ", nombre='" + getNombre() + "'" +
            ", apellido='" + getApellido() + "'" +
            ", identificacion='" + getIdentificacion() + "'" +
            "}";
    }
}
