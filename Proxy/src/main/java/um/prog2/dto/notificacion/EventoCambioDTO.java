package um.prog2.dto.notificacion;

import java.io.Serializable;

public class EventoCambioDTO implements Serializable {
    private String tipo; // EVENTO_CREADO, EVENTO_MODIFICADO, ASIENTO_BLOQUEADO, ASIENTO_VENDIDO
    private Long eventoId;
    private Integer fila; // opcional según tipo
    private Integer columna; // opcional según tipo
    private String descripcion; // mensaje adicional

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public Long getEventoId() { return eventoId; }
    public void setEventoId(Long eventoId) { this.eventoId = eventoId; }
    public Integer getFila() { return fila; }
    public void setFila(Integer fila) { this.fila = fila; }
    public Integer getColumna() { return columna; }
    public void setColumna(Integer columna) { this.columna = columna; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}

