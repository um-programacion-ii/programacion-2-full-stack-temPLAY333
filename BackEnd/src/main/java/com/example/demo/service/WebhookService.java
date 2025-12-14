package com.example.demo.service;

import com.example.demo.domain.WebhookProcesado;
import com.example.demo.repository.WebhookProcesadoRepository;
import com.example.demo.service.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para procesar webhooks del Proxy.
 * Maneja idempotencia y discrimina eventos por tipo.
 */
@Service
@Transactional
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    private final WebhookProcesadoRepository webhookProcesadoRepository;
    private final ObjectMapper objectMapper;
    private final EventoSyncService eventoSyncService;

    public WebhookService(
        WebhookProcesadoRepository webhookProcesadoRepository,
        ObjectMapper objectMapper,
        EventoSyncService eventoSyncService
    ) {
        this.webhookProcesadoRepository = webhookProcesadoRepository;
        this.objectMapper = objectMapper;
        this.eventoSyncService = eventoSyncService;
    }

    /**
     * Verifica si un webhook ya fue procesado (idempotencia).
     */
    public boolean yaFueProcesado(String idempotencyKey) {
        return webhookProcesadoRepository.existsByIdempotencyKey(idempotencyKey);
    }

    /**
     * Marca un webhook como procesado.
     */
    public void marcarProcesado(String idempotencyKey, String topic, Integer partition, Long offset) {
        if (!webhookProcesadoRepository.existsByIdempotencyKey(idempotencyKey)) {
            WebhookProcesado registro = new WebhookProcesado(idempotencyKey, topic, partition, offset);
            webhookProcesadoRepository.save(registro);
            log.debug("Webhook marcado como procesado: {}", idempotencyKey);
        }
    }

    /**
     * Procesa venta completada.
     */
    public void procesarVentaCompletada(RealizarVentaResponseDTO venta) {
        log.info("Procesando VENTA_COMPLETADA: ventaId={}, eventoId={}",
            venta.getVentaId(), venta.getEventoId());

        // Aquí iría la lógica para persistir la venta en BD local
        // Por ahora solo logeamos
        log.debug("Venta procesada exitosamente");
    }

    /**
     * Procesa asientos bloqueados.
     */
    public void procesarAsientosBloqueados(BloquearAsientosResponseDTO bloqueo) {
        log.info("Procesando ASIENTOS_BLOQUEADOS: eventoId={}, resultado={}",
            bloqueo.getEventoId(), bloqueo.getResultado());

        // Aquí iría la lógica para actualizar estado local si es necesario
        log.debug("Bloqueo procesado exitosamente");
    }

    /**
     * Procesa evento cambiado.
     */
    public void procesarEventoCambiado(Long eventoId) {
        log.info("Procesando EVENTO_CAMBIADO: eventoId={}", eventoId);

        try {
            // Sincronizar evento actualizado desde el Proxy
            eventoSyncService.syncEventoById(eventoId);
            log.debug("Evento sincronizado exitosamente: {}", eventoId);
        } catch (Exception e) {
            log.error("Error al sincronizar evento {}", eventoId, e);
        }
    }

    /**
     * Limpieza periódica de registros antiguos (> 7 días).
     * Se ejecuta diariamente a las 3 AM.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void limpiarRegistrosAntiguos() {
        Instant cutoffDate = Instant.now().minus(7, ChronoUnit.DAYS);
        int deleted = webhookProcesadoRepository.deleteOldRecords(cutoffDate);
        if (deleted > 0) {
            log.info("Limpiados {} registros de webhooks antiguos", deleted);
        }
    }
}

