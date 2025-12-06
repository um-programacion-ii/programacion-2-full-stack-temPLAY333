package um.prog2.dto.notificacion;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * DTO para evento de venta completada recibido desde Kafka de Cátedra.
 */
public class VentaCompletadaEventoDTO implements Serializable {

    private Long ventaId;
    private Long eventoId;
    private String usuarioEmail;
    private Boolean resultado;
    private Instant fechaVenta;
    private BigDecimal precioVenta;
    private List<AsientoVentaEventoDTO> asientos;
    private String mensaje;

    public VentaCompletadaEventoDTO() {}

    public Long getVentaId() { return ventaId; }
    public void setVentaId(Long ventaId) { this.ventaId = ventaId; }

    public Long getEventoId() { return eventoId; }
    public void setEventoId(Long eventoId) { this.eventoId = eventoId; }

    public String getUsuarioEmail() { return usuarioEmail; }
    public void setUsuarioEmail(String usuarioEmail) { this.usuarioEmail = usuarioEmail; }

    public Boolean getResultado() { return resultado; }
    public void setResultado(Boolean resultado) { this.resultado = resultado; }

    public Instant getFechaVenta() { return fechaVenta; }
    public void setFechaVenta(Instant fechaVenta) { this.fechaVenta = fechaVenta; }

    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }

    public List<AsientoVentaEventoDTO> getAsientos() { return asientos; }
    public void setAsientos(List<AsientoVentaEventoDTO> asientos) { this.asientos = asientos; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public static class AsientoVentaEventoDTO implements Serializable {
        private Integer fila;
        private Integer columna;
        private String persona;

        public AsientoVentaEventoDTO() {}

        public Integer getFila() { return fila; }
        public void setFila(Integer fila) { this.fila = fila; }

        public Integer getColumna() { return columna; }
        public void setColumna(Integer columna) { this.columna = columna; }

        public String getPersona() { return persona; }
        public void setPersona(String persona) { this.persona = persona; }
    }
}

