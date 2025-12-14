package com.example.demo.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;

/**
 * Entidad para registrar webhooks procesados y evitar duplicados (idempotencia).
 * Se usa partition + offset de Kafka como clave única.
 */
@Entity
@Table(name = "webhook_procesado")
public class WebhookProcesado implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", length = 100, unique = true, nullable = false)
    private String idempotencyKey;

    @Column(name = "topic", length = 50, nullable = false)
    private String topic;

    @Column(name = "partition_num")
    private Integer partitionNum;

    @Column(name = "offset_num")
    private Long offsetNum;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    public WebhookProcesado() {
        this.processedAt = Instant.now();
    }

    public WebhookProcesado(String idempotencyKey, String topic, Integer partitionNum, Long offsetNum) {
        this.idempotencyKey = idempotencyKey;
        this.topic = topic;
        this.partitionNum = partitionNum;
        this.offsetNum = offsetNum;
        this.processedAt = Instant.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Integer getPartitionNum() {
        return partitionNum;
    }

    public void setPartitionNum(Integer partitionNum) {
        this.partitionNum = partitionNum;
    }

    public Long getOffsetNum() {
        return offsetNum;
    }

    public void setOffsetNum(Long offsetNum) {
        this.offsetNum = offsetNum;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    @Override
    public String toString() {
        return "WebhookProcesado{" +
            "id=" + id +
            ", idempotencyKey='" + idempotencyKey + '\'' +
            ", topic='" + topic + '\'' +
            ", partitionNum=" + partitionNum +
            ", offsetNum=" + offsetNum +
            ", processedAt=" + processedAt +
            '}';
    }
}

