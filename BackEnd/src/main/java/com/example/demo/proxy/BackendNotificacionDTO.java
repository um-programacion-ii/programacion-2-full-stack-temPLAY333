package com.example.demo.proxy;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO genérico para notificar eventos al Backend desde el proxy.
 *
 * Se utiliza como cuerpo del webhook que el proxy envía al Backend
 * cada vez que sucede alguno de estos casos, entre otros:
 * - Mensaje recibido en Kafka (cambio de evento, asientos, venta, etc.).
 * - Operaciones relevantes realizadas vía proxy (bloqueo de asientos, venta realizada, login, ...).
 */
public class BackendNotificacionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Clave lógica del mensaje (por ejemplo, id de evento o de venta), si existe.
     */
    private String key;

    /**
     * Offset de Kafka (si aplica).
     */
    private Long offset;

    /**
     * Partición de Kafka (si aplica).
     */
    private Integer partition;

    /**
     * Payload JSON completo que describe el evento original.
     */
    private String payload;

    /**
     * Tipo lógico de la notificación, por ejemplo:
     * VENTA_COMPLETADA, ASIENTOS_BLOQUEADOS, EVENTO_CAMBIADO, BLOQUEO_ASIENTOS, VENTA_REALIZADA, LOGIN_EXITOSO, etc.
     */
    private String topic;

    /**
     * Momento en que el proxy generó la notificación.
     */
    private Instant timestamp;

    // ---------------------------------------------------------------------
    // Constructores
    // ---------------------------------------------------------------------

    public BackendNotificacionDTO() {
        this.timestamp = Instant.now();
    }

    public BackendNotificacionDTO(String key, Long offset, Integer partition, String payload, String topic) {
        this.key = key;
        this.offset = offset;
        this.partition = partition;
        this.payload = payload;
        this.topic = topic;
        this.timestamp = Instant.now();
    }

    // ---------------------------------------------------------------------
    // Getters y setters
    // ---------------------------------------------------------------------

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public Integer getPartition() {
        return partition;
    }

    public void setPartition(Integer partition) {
        this.partition = partition;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    // ---------------------------------------------------------------------
    // Métodos utilitarios
    // ---------------------------------------------------------------------

    @Override
    public String toString() {
        return "BackendNotificacionDTO{" +
            "key='" + key + '\'' +
            ", offset=" + offset +
            ", partition=" + partition +
            ", payload='" + payload + '\'' +
            ", topic='" + topic + '\'' +
            ", timestamp=" + timestamp +
            '}';
    }
}
