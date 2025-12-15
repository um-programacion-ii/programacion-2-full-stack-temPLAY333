package um.prog2.dto.evento.consulta;

import um.prog2.dto.evento.shared.EventoTipoBasicDTO;
import um.prog2.dto.evento.shared.IntegranteBasicDTO;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * DTO de detalle para el endpoint GET /api/endpoints/v1/evento/{id}
 * JSON ejemplo según especificación del usuario.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EventoDetalleDTO implements Serializable {

    @NotNull
    private Long id;

    @NotNull
    @Size(max = 200)
    private String titulo;

    @NotNull
    @Size(max = 500)
    private String resumen;

    private String descripcion;

    @NotNull
    private Instant fecha;

    @NotNull
    @Size(max = 300)
    private String direccion;

    @Size(max = 1000)
    private String imagen;

    @NotNull @Min(1)
    private Integer filaAsientos;

    @NotNull @Min(1)
    private Integer columnAsientos;

    @NotNull
    private BigDecimal precioEntrada;

    @NotNull
    private EventoTipoBasicDTO eventoTipo;

    @NotNull
    private List<IntegranteBasicDTO> integrantes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getResumen() { return resumen; }
    public void setResumen(String resumen) { this.resumen = resumen; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Instant getFecha() { return fecha; }
    public void setFecha(Instant fecha) { this.fecha = fecha; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
    public Integer getFilaAsientos() { return filaAsientos; }
    public void setFilaAsientos(Integer filaAsientos) { this.filaAsientos = filaAsientos; }
    public Integer getColumnAsientos() { return columnAsientos; }
    public void setColumnAsientos(Integer columnAsientos) { this.columnAsientos = columnAsientos; }
    public BigDecimal getPrecioEntrada() { return precioEntrada; }
    public void setPrecioEntrada(BigDecimal precioEntrada) { this.precioEntrada = precioEntrada; }
    public EventoTipoBasicDTO getEventoTipo() { return eventoTipo; }
    public void setEventoTipo(EventoTipoBasicDTO eventoTipo) { this.eventoTipo = eventoTipo; }
    public List<IntegranteBasicDTO> getIntegrantes() { return integrantes; }
    public void setIntegrantes(List<IntegranteBasicDTO> integrantes) { this.integrantes = integrantes; }

    @Override
    public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof EventoDetalleDTO)) return false; EventoDetalleDTO that = (EventoDetalleDTO) o; return Objects.equals(id, that.id); }
    @Override
    public int hashCode() { return Objects.hash(id); }
    @Override
    public String toString() { return "EventoDetalleDTO{" + "id=" + id + ", titulo='" + titulo + '\'' + '}'; }
}
