package com.example.demo.service.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * DTO para recibir notificaciones del Proxy.
 * El Proxy envía un webhook genérico con este formato para todos los tipos de eventos.
 *
 * Campos:
 * - timestamp: Momento en que el Proxy procesa el evento
 * - topic: Tipo de evento (ej: VENTA_COMPLETADA, ASIENTOS_BLOQUEADOS, EVENTO_CAMBIADO)
 * - partition: Partición de Kafka (null si viene de HTTP)
 * - offset: Offset de Kafka (null si viene de HTTP)
 * - key: Key del mensaje Kafka (null si viene de HTTP)
 * - payload: JSON crudo como string - el Backend debe parsearlo según el tipo
 *
 * Ejemplo:
 * {
 *   "timestamp": "2025-12-14T15:30:00Z",
 *   "topic": "VENTA_COMPLETADA",
 *   "partition": 0,
 *   "offset": 12345,
 *   "key": "evento-1",
 *   "payload": "{\"ventaId\":123,...}"
 * }
 */
public class BackendNotificacionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Instant timestamp;
    private String topic;
    private Integer partition;
    private Long offset;
    private String key;
    private String payload;

    public BackendNotificacionDTO() {}

    public BackendNotificacionDTO(Instant timestamp, String topic, String payload) {
        this.timestamp = timestamp;
        this.topic = topic;
        this.payload = payload;
    }

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BackendNotificacionDTO)) return false;
        BackendNotificacionDTO that = (BackendNotificacionDTO) o;
        return Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(topic, that.topic) &&
               Objects.equals(partition, that.partition) &&
               Objects.equals(offset, that.offset) &&
               Objects.equals(key, that.key) &&
               Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, topic, partition, offset, key, payload);
    }

    @Override
    public String toString() {
        return "BackendNotificacionDTO{" +
            "timestamp=" + timestamp +
            ", topic='" + topic + '\'' +
            ", partition=" + partition +
            ", offset=" + offset +
            ", key='" + key + '\'' +
            ", payload='" + (payload != null ? payload.substring(0, Math.min(100, payload.length())) + "..." : null) + '\'' +
            '}';
    }
}

