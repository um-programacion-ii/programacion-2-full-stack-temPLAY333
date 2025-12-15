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
 * A EventoTipo.
 */
@Entity
@Table(name = "evento_tipo")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EventoTipo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 100)
    @Column(name = "nombre", length = 100, nullable = false, unique = true)
    private String nombre;

    @Size(max = 500)
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @OneToMany(mappedBy = "eventoTipo", fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "eventoTipo", "integrantes" }, allowSetters = true)
    private Set<Evento> eventos = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public EventoTipo id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return this.nombre;
    }

    public EventoTipo nombre(String nombre) {
        this.setNombre(nombre);
        return this;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public EventoTipo descripcion(String descripcion) {
        this.setDescripcion(descripcion);
        return this;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Set<Evento> getEventos() {
        return this.eventos;
    }

    public void setEventos(Set<Evento> eventos) {
        this.eventos = eventos;
    }

    public EventoTipo eventos(Set<Evento> eventos) {
        this.setEventos(eventos);
        return this;
    }

    public EventoTipo addEvento(Evento evento) {
        this.eventos.add(evento);
        evento.setEventoTipo(this);
        return this;
    }

    public EventoTipo removeEvento(Evento evento) {
        this.eventos.remove(evento);
        evento.setEventoTipo(null);
        return this;
    }

    // Compatibility helpers for legacy 1:1 API (tests/code expect getEvento/setEvento/evento)
    // These adapt to the OneToMany model by returning/setting the first event in the collection.
    public Evento getEvento() {
        return this.eventos.stream().findFirst().orElse(null);
    }

    public void setEvento(Evento evento) {
        // Clear previous single-event mapping to emulate old 1:1 behaviour
        if (this.eventos != null) {
            this.eventos.forEach(e -> e.setEventoTipo(null));
            this.eventos.clear();
        }
        if (evento != null) {
            this.addEvento(evento);
        }
    }

    public EventoTipo evento(Evento evento) {
        this.setEvento(evento);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventoTipo)) {
            return false;
        }
        return getId() != null && getId().equals(((EventoTipo) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EventoTipo{" +
            "id=" + getId() +
            ", nombre='" + getNombre() + "'" +
            ", descripcion='" + getDescripcion() + "'" +
            "}";
    }
}
