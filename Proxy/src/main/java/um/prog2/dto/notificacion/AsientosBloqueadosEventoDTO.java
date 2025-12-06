package um.prog2.dto.notificacion;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * DTO para evento de bloqueo de asientos recibido desde Kafka de Cátedra.
 */
public class AsientosBloqueadosEventoDTO implements Serializable {

    private Long eventoId;
    private Boolean resultado;
    private List<AsientoBloqueadoDTO> asientos;
    private Instant bloqueadoHasta;
    private String mensaje;

    public AsientosBloqueadosEventoDTO() {}

    public Long getEventoId() { return eventoId; }
    public void setEventoId(Long eventoId) { this.eventoId = eventoId; }

    public Boolean getResultado() { return resultado; }
    public void setResultado(Boolean resultado) { this.resultado = resultado; }

    public List<AsientoBloqueadoDTO> getAsientos() { return asientos; }
    public void setAsientos(List<AsientoBloqueadoDTO> asientos) { this.asientos = asientos; }

    public Instant getBloqueadoHasta() { return bloqueadoHasta; }
    public void setBloqueadoHasta(Instant bloqueadoHasta) { this.bloqueadoHasta = bloqueadoHasta; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public static class AsientoBloqueadoDTO implements Serializable {
        private Integer fila;
        private Integer columna;
        private String estado;

        public AsientoBloqueadoDTO() {}

        public Integer getFila() { return fila; }
        public void setFila(Integer fila) { this.fila = fila; }

        public Integer getColumna() { return columna; }
        public void setColumna(Integer columna) { this.columna = columna; }

        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
    }
}

