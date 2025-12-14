package com.example.demo.repository;

import com.example.demo.domain.WebhookProcesado;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository para gestionar webhooks procesados (idempotencia).
 */
@Repository
public interface WebhookProcesadoRepository extends JpaRepository<WebhookProcesado, Long> {

    /**
     * Verifica si un webhook con esta clave ya fue procesado.
     */
    boolean existsByIdempotencyKey(String idempotencyKey);

    /**
     * Limpia registros antiguos (> 7 días) para evitar crecimiento infinito.
     */
    @Modifying
    @Query("DELETE FROM WebhookProcesado w WHERE w.processedAt < :cutoffDate")
    int deleteOldRecords(@Param("cutoffDate") Instant cutoffDate);
}

