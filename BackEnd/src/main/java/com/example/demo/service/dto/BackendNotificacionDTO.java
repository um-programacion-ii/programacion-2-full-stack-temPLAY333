package com.example.demo.service.dto;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO para recibir notificaciones del Proxy vía webhook.
 *
 * El Proxy envía este DTO cuando:
 * - Recibe eventos de Kafka de la Cátedra (ventas, bloqueos, cambios)
 * - (Opcional) Responde a operaciones HTTP síncronas
 */
public class BackendNotificacionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Momento en que el Proxy generó/recibió la notificación.
     */
    private Instant timestamp;

    /**
     * Tipo lógico del evento:
     * - VENTA_COMPLETADA
     * - ASIENTOS_BLOQUEADOS
     * - EVENTO_CAMBIADO
     * - UNKNOWN
     */
    private String topic;

    /**
     * Partición de Kafka (si aplica, null si viene de HTTP).
     */
    private Integer partition;

    /**
     * Offset de Kafka (si aplica, null si viene de HTTP).
     */
    private Long offset;

    /**
     * Clave del mensaje de Kafka (si aplica).
     */
    private String key;

    /**
     * Payload JSON crudo del evento como String.
     * Debe parsearse en el Backend según el tipo de evento.
     */
    private String payload;

    // Constructores

    public BackendNotificacionDTO() {
        this.timestamp = Instant.now();
    }

    public BackendNotificacionDTO(String topic, String payload) {
        this.timestamp = Instant.now();
        this.topic = topic;
        this.payload = payload;
    }

    // Getters y Setters

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Integer getPartition() {
        return partition;
    }

    public void setPartition(Integer partition) {
        this.partition = partition;
    }

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "BackendNotificacionDTO{" +
            "timestamp=" + timestamp +
            ", topic='" + topic + '\'' +
            ", partition=" + partition +
            ", offset=" + offset +
            ", key='" + key + '\'' +
            ", payload='" + payload + '\'' +
            '}';
    }
}

