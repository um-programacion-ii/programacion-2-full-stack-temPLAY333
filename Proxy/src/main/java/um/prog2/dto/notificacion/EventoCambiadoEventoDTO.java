package um.prog2.dto.notificacion;

import java.io.Serializable;

/**
 * DTO para evento de cambio en evento recibido desde Kafka de Cátedra.
 */
public class EventoCambiadoEventoDTO implements Serializable {

    private Long eventoId;
    private String tipoCambio; // CREADO, MODIFICADO, CANCELADO
    private String mensaje;

    public EventoCambiadoEventoDTO() {}

    public Long getEventoId() { return eventoId; }
    public void setEventoId(Long eventoId) { this.eventoId = eventoId; }

    public String getTipoCambio() { return tipoCambio; }
    public void setTipoCambio(String tipoCambio) { this.tipoCambio = tipoCambio; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}

