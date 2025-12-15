package um.prog2.dto.notificacion;

import java.io.Serializable;
import java.time.Instant;

public class BackendNotificacionDTO implements Serializable {
    private Instant timestamp = Instant.now();
    private String topic;
    private Integer partition;
    private Long offset;
    private String key;
    private String payload; // JSON crudo del mensaje de Kafka

    public BackendNotificacionDTO() {}

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public Integer getPartition() { return partition; }
    public void setPartition(Integer partition) { this.partition = partition; }
    public Long getOffset() { return offset; }
    public void setOffset(Long offset) { this.offset = offset; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
}
