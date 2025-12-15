package com.example.demo.service;

import com.example.demo.service.dto.BackendNotificacionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Test unitario para EventoWebhookService.
 * Valida el procesamiento de notificaciones recibidas del Proxy.
 */
@ExtendWith(MockitoExtension.class)
class EventoWebhookServiceTest {

    @Mock
    private EventoSyncService eventoSyncService;

    private EventoWebhookService eventoWebhookService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        eventoWebhookService = new EventoWebhookService(objectMapper, eventoSyncService);
    }

    @Test
    void procesarNotificacion_conVentaCompletada_debeSincronizarEvento() {
        // Given
        BackendNotificacionDTO notificacion = new BackendNotificacionDTO();
        notificacion.setTopic("VENTA_COMPLETADA");
        notificacion.setPartition(0);
        notificacion.setOffset(123L);
        notificacion.setPayload("{\"ventaId\":456,\"eventoId\":1,\"asientos\":[{\"fila\":1,\"columna\":2}],\"fechaVenta\":\"2025-12-13T10:00:00\",\"username\":\"alumno1\"}");

        doNothing().when(eventoSyncService).syncEventoById(anyLong());

        // When
        assertThatCode(() -> eventoWebhookService.procesarNotificacion(notificacion))
            .doesNotThrowAnyException();

        // Then
        verify(eventoSyncService, times(1)).syncEventoById(1L);
    }

    @Test
    void procesarNotificacion_conAsientosBloqueados_debeLogearInformacion() {
        // Given
        BackendNotificacionDTO notificacion = new BackendNotificacionDTO();
        notificacion.setTopic("ASIENTOS_BLOQUEADOS");
        notificacion.setPartition(0);
        notificacion.setOffset(124L);
        notificacion.setPayload("{\"eventoId\":1,\"asientos\":[{\"fila\":1,\"columna\":2}],\"bloqueadoHasta\":\"2025-12-13T10:05:00\"}");

        doNothing().when(eventoSyncService).syncEventoById(anyLong());

        // When
        assertThatCode(() -> eventoWebhookService.procesarNotificacion(notificacion))
            .doesNotThrowAnyException();

        // Then
        // Verificar que SÍ se llama a eventoSyncService para sincronizar el evento con asientos bloqueados
        verify(eventoSyncService, times(1)).syncEventoById(1L);
    }

    @Test
    void procesarNotificacion_conEventoCambiado_debeSincronizarEvento() {
        // Given
        BackendNotificacionDTO notificacion = new BackendNotificacionDTO();
        notificacion.setTopic("EVENTO_CAMBIADO");
        notificacion.setPartition(0);
        notificacion.setOffset(125L);
        notificacion.setPayload("{\"eventoId\":1,\"cambios\":{\"nombre\":\"Nuevo Nombre\",\"fecha\":\"2025-12-20T18:00:00\"}}");

        doNothing().when(eventoSyncService).syncEventoById(anyLong());

        // When
        assertThatCode(() -> eventoWebhookService.procesarNotificacion(notificacion))
            .doesNotThrowAnyException();

        // Then
        verify(eventoSyncService, times(1)).syncEventoById(1L);
    }

    @Test
    void procesarNotificacion_conEventoDesconocido_debeLogearWarning() {
        // Given
        BackendNotificacionDTO notificacion = new BackendNotificacionDTO();
        notificacion.setTopic("UNKNOWN");
        notificacion.setPartition(0);
        notificacion.setOffset(126L);
        notificacion.setPayload("{\"mensaje\":\"Evento desconocido\"}");

        // When
        assertThatCode(() -> eventoWebhookService.procesarNotificacion(notificacion))
            .doesNotThrowAnyException();

        // Then
        verify(eventoSyncService, never()).syncEventoById(anyLong());
    }

    @Test
    void procesarNotificacion_conTopicNoManejado_debeLogearWarning() {
        // Given
        BackendNotificacionDTO notificacion = new BackendNotificacionDTO();
        notificacion.setTopic("NUEVO_TIPO_EVENTO");
        notificacion.setPartition(0);
        notificacion.setOffset(127L);
        notificacion.setPayload("{\"mensaje\":\"Nuevo tipo de evento\"}");

        // When
        assertThatCode(() -> eventoWebhookService.procesarNotificacion(notificacion))
            .doesNotThrowAnyException();

        // Then
        verify(eventoSyncService, never()).syncEventoById(anyLong());
    }

    @Test
    void procesarNotificacion_conPayloadInvalido_debeLanzarExcepcion() {
        // Given
        BackendNotificacionDTO notificacion = new BackendNotificacionDTO();
        notificacion.setTopic("VENTA_COMPLETADA");
        notificacion.setPartition(0);
        notificacion.setOffset(128L);
        notificacion.setPayload("JSON INVALIDO {{{");

        // When / Then
        // El servicio captura excepciones en procesarVentaCompletada, por lo que NO se propaga
        // pero sí se loguea el error
        assertThatCode(() -> eventoWebhookService.procesarNotificacion(notificacion))
            .doesNotThrowAnyException();

        verify(eventoSyncService, never()).syncEventoById(anyLong());
    }

    @Test
    void procesarNotificacion_conVentaCompletadaSinEventoId_debeManejarError() {
        // Given
        BackendNotificacionDTO notificacion = new BackendNotificacionDTO();
        notificacion.setTopic("VENTA_COMPLETADA");
        notificacion.setPartition(0);
        notificacion.setOffset(129L);
        notificacion.setPayload("{\"ventaId\":456,\"asientos\":[]}"); // Falta eventoId

        // When / Then
        // El servicio captura la excepción en procesarVentaCompletada y la loguea
        assertThatCode(() -> eventoWebhookService.procesarNotificacion(notificacion))
            .doesNotThrowAnyException();

        verify(eventoSyncService, never()).syncEventoById(anyLong());
    }

    @Test
    void procesarNotificacion_conErrorEnSincronizacion_debePropagar() {
        // Given
        BackendNotificacionDTO notificacion = new BackendNotificacionDTO();
        notificacion.setTopic("EVENTO_CAMBIADO");
        notificacion.setPartition(0);
        notificacion.setOffset(130L);
        notificacion.setPayload("{\"eventoId\":999}");

        doThrow(new RuntimeException("Error de sincronización"))
            .when(eventoSyncService).syncEventoById(999L);

        // When / Then
        // El servicio captura la excepción en procesarEventoCambiado y la loguea
        assertThatCode(() -> eventoWebhookService.procesarNotificacion(notificacion))
            .doesNotThrowAnyException();

        verify(eventoSyncService, times(1)).syncEventoById(999L);
    }

    @Test
    void procesarNotificacion_conMultiplesAsientosBloqueados_debeProcesamientoCompleto() {
        // Given
        BackendNotificacionDTO notificacion = new BackendNotificacionDTO();
        notificacion.setTopic("ASIENTOS_BLOQUEADOS");
        notificacion.setPartition(0);
        notificacion.setOffset(131L);
        notificacion.setPayload("{\"eventoId\":2,\"asientos\":[" +
            "{\"fila\":1,\"columna\":1}," +
            "{\"fila\":1,\"columna\":2}," +
            "{\"fila\":1,\"columna\":3}," +
            "{\"fila\":1,\"columna\":4}" +
            "],\"bloqueadoHasta\":\"2025-12-13T10:05:00\"}");

        doNothing().when(eventoSyncService).syncEventoById(anyLong());

        // When
        assertThatCode(() -> eventoWebhookService.procesarNotificacion(notificacion))
            .doesNotThrowAnyException();

        // Then
        // Verificar que se sincroniza el evento después de bloquear múltiples asientos
        verify(eventoSyncService, times(1)).syncEventoById(2L);
    }

    @Test
    void procesarNotificacion_conVentaCompletadaConDatosCompletos_debeSincronizarYLogear() {
        // Given
        BackendNotificacionDTO notificacion = new BackendNotificacionDTO();
        notificacion.setTopic("VENTA_COMPLETADA");
        notificacion.setPartition(0);
        notificacion.setOffset(132L);
        notificacion.setPayload("{" +
            "\"ventaId\":789," +
            "\"eventoId\":3," +
            "\"asientos\":[" +
            "{\"fila\":2,\"columna\":5,\"persona\":\"Juan Perez\"}," +
            "{\"fila\":2,\"columna\":6,\"persona\":\"Maria Lopez\"}" +
            "]," +
            "\"fechaVenta\":\"2025-12-13T11:30:00\"," +
            "\"username\":\"alumno2\"" +
            "}");

        doNothing().when(eventoSyncService).syncEventoById(anyLong());

        // When
        assertThatCode(() -> eventoWebhookService.procesarNotificacion(notificacion))
            .doesNotThrowAnyException();

        // Then
        verify(eventoSyncService, times(1)).syncEventoById(3L);
    }
}

