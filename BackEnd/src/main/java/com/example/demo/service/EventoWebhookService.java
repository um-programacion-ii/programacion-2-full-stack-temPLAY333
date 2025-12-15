package com.example.demo.service;

import com.example.demo.service.dto.BackendNotificacionDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para procesar notificaciones recibidas del Proxy vía webhook.
 * Estas notificaciones vienen originalmente de Kafka de la Cátedra.
 */
@Service
@Transactional
public class EventoWebhookService {

    private static final Logger log = LoggerFactory.getLogger(EventoWebhookService.class);

    private final ObjectMapper objectMapper;
    private final EventoSyncService eventoSyncService;

    public EventoWebhookService(ObjectMapper objectMapper, EventoSyncService eventoSyncService) {
        this.objectMapper = objectMapper;
        this.eventoSyncService = eventoSyncService;
    }

    /**
     * Procesa una notificación recibida del Proxy.
     * El payload es un JSON string que debe parsearse según el tipo de evento.
     */
    public void procesarNotificacion(BackendNotificacionDTO notificacion) {
        log.debug("Procesando notificación - Topic: {}, Offset: {}, Partition: {}",
            notificacion.getTopic(), notificacion.getOffset(), notificacion.getPartition());

        try {
            String topic = notificacion.getTopic();
            String payload = notificacion.getPayload();

            switch (topic) {
                case "VENTA_COMPLETADA":
                    procesarVentaCompletada(payload);
                    break;

                case "ASIENTOS_BLOQUEADOS":
                    procesarAsientosBloqueados(payload);
                    break;

                case "EVENTO_CAMBIADO":
                    procesarEventoCambiado(payload);
                    break;

                case "UNKNOWN":
                    log.warn("Evento desconocido recibido: {}", payload);
                    break;

                default:
                    log.warn("Tipo de evento no manejado: {}", topic);
            }
        } catch (Exception e) {
            log.error("Error al procesar notificación", e);
            throw new RuntimeException("Error al procesar notificación del Proxy", e);
        }
    }

    /**
     * Procesa evento de venta completada.
     * Payload: {"ventaId":123,"eventoId":1,"asientos":[...],"fechaVenta":"...","username":"alumno1"}
     */
    private void procesarVentaCompletada(String payload) {
        try {
            log.info("Procesando VENTA_COMPLETADA: {}", payload);

            JsonNode json = objectMapper.readTree(payload);
            Long eventoId = json.get("eventoId").asLong();
            Long ventaId = json.get("ventaId").asLong();

            // Sincronizar el evento para actualizar asientos vendidos
            eventoSyncService.syncEventoById(eventoId);

            // TODO: Persistir venta en BD local
            // 1. Buscar/crear usuario por username
            // 2. Buscar evento por eventoId
            // 3. Crear entidad Venta con datos del JSON
            // 4. Crear entidades Asiento asociadas
            // 5. Guardar en BD

            // Por ahora solo registramos que se procesó
            log.info("Venta completada procesada - VentaId: {}, EventoId: {}", ventaId, eventoId);
            log.info("La persistencia completa de la venta se implementará según los requisitos del negocio");
        } catch (Exception e) {
            log.error("Error al procesar venta completada: {}", payload, e);
        }
    }

    /**
     * Procesa evento de asientos bloqueados.
     * Payload: {"eventoId":1,"asientos":[...],"bloqueadoHasta":"..."}
     */
    private void procesarAsientosBloqueados(String payload) {
        try {
            log.info("Procesando ASIENTOS_BLOQUEADOS: {}", payload);

            JsonNode json = objectMapper.readTree(payload);
            Long eventoId = json.get("eventoId").asLong();

            // Sincronizar el evento para actualizar asientos bloqueados
            eventoSyncService.syncEventoById(eventoId);

            // Aquí puedes agregar lógica adicional:
            // - Actualizar cache de disponibilidad
            // - Notificar a otros usuarios que esos asientos ya no están disponibles

            log.info("Asientos bloqueados procesados - EventoId: {}", eventoId);
        } catch (Exception e) {
            log.error("Error al procesar asientos bloqueados: {}", payload, e);
        }
    }

    /**
     * Procesa evento de cambio en un evento.
     * Payload: {"eventoId":1,"tipoCambio":"..."}
     */
    private void procesarEventoCambiado(String payload) {
        try {
            log.info("Procesando EVENTO_CAMBIADO: {}", payload);

            JsonNode json = objectMapper.readTree(payload);
            Long eventoId = json.get("eventoId").asLong();

            // Sincronizar el evento para obtener los datos actualizados
            eventoSyncService.syncEventoById(eventoId);

            // Aquí puedes agregar lógica adicional:
            // - Invalidar cache
            // - Notificar a usuarios que tienen este evento en favoritos
            // - Actualizar índices de búsqueda

            log.info("Evento cambiado procesado - EventoId: {}", eventoId);
        } catch (Exception e) {
            log.error("Error al procesar evento cambiado: {}", payload, e);
        }
    }
}

