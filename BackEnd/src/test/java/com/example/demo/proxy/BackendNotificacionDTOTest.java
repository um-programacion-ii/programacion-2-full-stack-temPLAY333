package com.example.demo.proxy;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class BackendNotificacionDTOTest {

    @Test
    void shouldSetDefaultTimestampInNoArgsConstructor() {
        BackendNotificacionDTO dto = new BackendNotificacionDTO();
        assertThat(dto.getTimestamp()).isNotNull();
    }

    @Test
    void shouldPopulateFieldsInAllArgsConstructor() {
        BackendNotificacionDTO dto = new BackendNotificacionDTO("k", 1L, 2, "payload", "TOPIC");
        assertThat(dto.getKey()).isEqualTo("k");
        assertThat(dto.getOffset()).isEqualTo(1L);
        assertThat(dto.getPartition()).isEqualTo(2);
        assertThat(dto.getPayload()).isEqualTo("payload");
        assertThat(dto.getTopic()).isEqualTo("TOPIC");
        assertThat(dto.getTimestamp()).isNotNull();
    }

    @Test
    void shouldAllowTimestampMutation() {
        BackendNotificacionDTO dto = new BackendNotificacionDTO();
        Instant now = Instant.now();
        dto.setTimestamp(now);
        assertThat(dto.getTimestamp()).isEqualTo(now);
    }
}
