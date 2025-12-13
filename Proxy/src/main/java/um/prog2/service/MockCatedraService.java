package um.prog2.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import um.prog2.dto.evento.bloqueo.AsientoEstadoDTO;
import um.prog2.dto.evento.bloqueo.BloquearAsientosResponseDTO;
import um.prog2.dto.evento.consulta.EventoDetalleDTO;
import um.prog2.dto.evento.consulta.EventoDTO;
import um.prog2.dto.evento.consulta.EventoResumenDTO;
import um.prog2.dto.evento.shared.EventoTipoBasicDTO;
import um.prog2.dto.evento.shared.EventoTipoDTO;
import um.prog2.dto.evento.shared.IntegranteBasicDTO;
import um.prog2.dto.notificacion.BackendNotificacionDTO;
import um.prog2.dto.venta.RealizarVentaResponseDTO;
import um.prog2.dto.consultaventas.VentaResumenDTO;
import um.prog2.dto.consultaventas.VentaDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Servicio Mock que simula las respuestas de la Cátedra.
 * Activo SOLO con profile "mock".
 * Simula:
 * - Respuestas HTTP síncronas (eventos, ventas, etc.)
 * - Respuestas asíncronas vía notificación al Backend (simula Kafka)
 *
 * NO mockea Kafka ni Redis reales, solo simula que el Proxy
 * "se conecta" y devuelve datos correctos.
 */
@Service
@Profile("mock")
public class MockCatedraService {

    private static final Logger log = LoggerFactory.getLogger(MockCatedraService.class);

    @Autowired(required = false)
    private NotificadorBackendService notificadorBackendService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Estado en memoria: asientos bloqueados/vendidos por evento
    private final Map<Long, List<AsientoEstadoDTO>> asientosEstadoMap = new HashMap<>();

    // Ventas generadas
    private final List<VentaDTO> ventasGeneradas = new ArrayList<>();
    private Long nextVentaId = 1L;

    // ==================== EVENTOS ====================

    /**
     * Devuelve 5 eventos con datos inventados y diferentes tamaños de sala.
     */
    public List<EventoResumenDTO> listarEventosResumidos() {
        log.debug("MockCatedraService: devolviendo eventos resumidos mock");
        return Arrays.asList(
            crearEventoResumen(1L, "Conferencia Tech 2025", "La conferencia más nerd del año",
                "2025-12-20T10:00:00Z", new BigDecimal("2500.00"), "Conferencia"),
            crearEventoResumen(2L, "Concierto Rock Nacional", "Las mejores bandas del país",
                "2025-12-22T20:00:00Z", new BigDecimal("4500.00"), "Concierto"),
            crearEventoResumen(3L, "Obra de Teatro Clásica", "Shakespeare en la ciudad",
                "2025-12-25T19:00:00Z", new BigDecimal("3000.00"), "Teatro"),
            crearEventoResumen(4L, "Stand-Up Comedy Night", "Risas garantizadas",
                "2025-12-28T21:00:00Z", new BigDecimal("1800.00"), "Comedia"),
            crearEventoResumen(5L, "Festival de Cine Indie", "Lo mejor del cine independiente",
                "2025-12-30T18:00:00Z", new BigDecimal("2000.00"), "Cine")
        );
    }

    /**
     * Devuelve eventos completos (mismo listado pero con estructura EventoDTO).
     */
    public List<EventoDTO> listarEventosCompletos() {
        log.debug("MockCatedraService: devolviendo eventos completos mock");
        List<EventoResumenDTO> resumidos = listarEventosResumidos();
        List<EventoDTO> completos = new ArrayList<>();
        for (EventoResumenDTO resumido : resumidos) {
            EventoDTO completo = new EventoDTO();
            completo.setId(resumido.getId());
            completo.setTitulo(resumido.getTitulo());
            completo.setResumen(resumido.getResumen());
            completo.setDescripcion(resumido.getDescripcion());
            completo.setFecha(resumido.getFecha());
            completo.setPrecioEntrada(resumido.getPrecioEntrada());
            completo.setEventoTipo(resumido.getEventoTipo());
            completos.add(completo);
        }
        return completos;
    }

    /**
     * Devuelve detalle de un evento específico.
     * Tamaños de sala variables por evento.
     */
    public EventoDetalleDTO obtenerEventoDetalle(Long id) {
        log.debug("MockCatedraService: devolviendo detalle de evento {} mock", id);

        EventoDetalleDTO detalle = new EventoDetalleDTO();
        detalle.setId(id);

        switch (id.intValue()) {
            case 1:
                detalle.setTitulo("Conferencia Tech 2025");
                detalle.setResumen("La conferencia más nerd del año");
                detalle.setDescripcion("Una conferencia épica con los mejores speakers del mundo tech. Charlas, talleres y networking.");
                detalle.setFecha(Instant.parse("2025-12-20T10:00:00Z"));
                detalle.setDireccion("Centro de Convenciones, Av. Libertador 1000");
                detalle.setImagen("https://picsum.photos/seed/tech2025/800/600");
                detalle.setFilaAsientos(10);
                detalle.setColumnAsientos(8);
                detalle.setPrecioEntrada(new BigDecimal("2500.00"));
                break;
            case 2:
                detalle.setTitulo("Concierto Rock Nacional");
                detalle.setResumen("Las mejores bandas del país");
                detalle.setDescripcion("Un concierto inolvidable con los grupos más importantes del rock nacional. 4 horas de música en vivo.");
                detalle.setFecha(Instant.parse("2025-12-22T20:00:00Z"));
                detalle.setDireccion("Estadio Municipal, Ruta 40 Km 12");
                detalle.setImagen("https://picsum.photos/seed/rock2025/800/600");
                detalle.setFilaAsientos(15);
                detalle.setColumnAsientos(12);
                detalle.setPrecioEntrada(new BigDecimal("4500.00"));
                break;
            case 3:
                detalle.setTitulo("Obra de Teatro Clásica");
                detalle.setResumen("Shakespeare en la ciudad");
                detalle.setDescripcion("Romeo y Julieta interpretado por el elenco nacional. Una noche de teatro inolvidable.");
                detalle.setFecha(Instant.parse("2025-12-25T19:00:00Z"));
                detalle.setDireccion("Teatro Municipal, Calle San Martín 456");
                detalle.setImagen("https://picsum.photos/seed/teatro2025/800/600");
                detalle.setFilaAsientos(8);
                detalle.setColumnAsientos(10);
                detalle.setPrecioEntrada(new BigDecimal("3000.00"));
                break;
            case 4:
                detalle.setTitulo("Stand-Up Comedy Night");
                detalle.setResumen("Risas garantizadas");
                detalle.setDescripcion("Los mejores comediantes del país en una noche llena de humor y sorpresas.");
                detalle.setFecha(Instant.parse("2025-12-28T21:00:00Z"));
                detalle.setDireccion("Café Cultural, Paseo de la Plaza 123");
                detalle.setImagen("https://picsum.photos/seed/comedy2025/800/600");
                detalle.setFilaAsientos(6);
                detalle.setColumnAsientos(6);
                detalle.setPrecioEntrada(new BigDecimal("1800.00"));
                break;
            case 5:
                detalle.setTitulo("Festival de Cine Indie");
                detalle.setResumen("Lo mejor del cine independiente");
                detalle.setDescripcion("Maratón de películas independientes de todo el mundo. 3 días de proyecciones.");
                detalle.setFecha(Instant.parse("2025-12-30T18:00:00Z"));
                detalle.setDireccion("Cine Arte, Boulevard Los Andes 789");
                detalle.setImagen("https://picsum.photos/seed/cine2025/800/600");
                detalle.setFilaAsientos(12);
                detalle.setColumnAsientos(10);
                detalle.setPrecioEntrada(new BigDecimal("2000.00"));
                break;
            default:
                log.warn("MockCatedraService: evento {} no encontrado", id);
                return null;
        }

        // EventoTipo
        EventoTipoBasicDTO tipo = new EventoTipoBasicDTO();
        tipo.setNombre(obtenerTipoPorId(id));
        tipo.setDescripcion(obtenerTipoPorId(id));
        detalle.setEventoTipo(tipo);

        // Integrantes mock
        detalle.setIntegrantes(crearIntegrantesMock(id));

        return detalle;
    }

    // ==================== ASIENTOS ====================

    /**
     * Devuelve estado de asientos desde el mapa en memoria (simula Redis).
     */
    public List<AsientoEstadoDTO> obtenerEstadoAsientos(Long eventoId) {
        log.debug("MockCatedraService: devolviendo estado de asientos para evento {} mock", eventoId);
        return asientosEstadoMap.getOrDefault(eventoId, Collections.emptyList());
    }

    /**
     * Bloquea asientos y simula respuesta asíncrona vía webhook.
     */
    public BloquearAsientosResponseDTO bloquearAsientos(Long eventoId, List<um.prog2.dto.evento.bloqueo.AsientoPosicionDTO> asientos) {
        log.debug("MockCatedraService: bloqueando {} asientos para evento {} mock", asientos.size(), eventoId);

        // Simular respuesta síncrona
        BloquearAsientosResponseDTO response = new BloquearAsientosResponseDTO();
        response.setResultado(true);
        response.setDescripcion("Asientos bloqueados exitosamente");
        response.setEventoId(eventoId);

        // Actualizar estado en memoria y construir lista de respuesta
        List<AsientoEstadoDTO> estadoActual = asientosEstadoMap.computeIfAbsent(eventoId, k -> new ArrayList<>());
        List<AsientoEstadoDTO> asientosRespuesta = new ArrayList<>();

        for (um.prog2.dto.evento.bloqueo.AsientoPosicionDTO asiento : asientos) {
            AsientoEstadoDTO estado = new AsientoEstadoDTO();
            estado.setFila(asiento.getFila());
            estado.setColumna(asiento.getColumna());
            estado.setEstado("Bloqueado");
            estadoActual.add(estado);
            asientosRespuesta.add(estado);
        }

        response.setAsientos(asientosRespuesta);

        // Simular webhook asíncrono (después de delay random)
        if (notificadorBackendService != null) {
            CompletableFuture.runAsync(() -> {
                try {
                    int delay = ThreadLocalRandom.current().nextInt(1000, 3000);
                    Thread.sleep(delay);

                    // Construir payload JSON
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("eventoId", eventoId);
                    payload.put("asientos", asientos);
                    payload.put("bloqueadoHasta", Instant.now().plus(15, ChronoUnit.MINUTES).toString());

                    String payloadJson = objectMapper.writeValueAsString(payload);

                    BackendNotificacionDTO notificacion = new BackendNotificacionDTO();
                    notificacion.setTimestamp(Instant.now());
                    notificacion.setTopic("ASIENTOS_BLOQUEADOS");
                    notificacion.setPartition(0);
                    notificacion.setOffset(ThreadLocalRandom.current().nextLong(1000, 9999));
                    notificacion.setKey("evento-" + eventoId);
                    notificacion.setPayload(payloadJson);

                    notificadorBackendService.notificarCambio(notificacion);
                    log.debug("MockCatedraService: webhook ASIENTOS_BLOQUEADOS enviado para evento {}", eventoId);

                } catch (Exception e) {
                    log.error("MockCatedraService: error enviando webhook de bloqueo", e);
                }
            });
        }

        return response;
    }

    // ==================== VENTAS ====================

    /**
     * Realiza venta y simula respuesta asíncrona vía webhook.
     */
    public RealizarVentaResponseDTO realizarVenta(Long eventoId, List<um.prog2.dto.venta.AsientoVentaDTO> asientos) {
        log.debug("MockCatedraService: realizando venta de {} asientos para evento {} mock", asientos.size(), eventoId);

        Long ventaId = nextVentaId++;
        BigDecimal precioTotal = calcularMontoTotal(eventoId, asientos.size());

        // Simular respuesta síncrona
        RealizarVentaResponseDTO response = new RealizarVentaResponseDTO();
        response.setEventoId(eventoId);
        response.setVentaId(ventaId);
        response.setFechaVenta(Instant.now());
        response.setResultado(true);
        response.setDescripcion("Venta realizada exitosamente");
        response.setPrecioVenta(precioTotal);

        // Actualizar estado en memoria (marcar como vendidos)
        List<AsientoEstadoDTO> estadoActual = asientosEstadoMap.computeIfAbsent(eventoId, k -> new ArrayList<>());
        List<um.prog2.dto.venta.AsientoVentaEstadoDTO> asientosRespuesta = new ArrayList<>();

        for (um.prog2.dto.venta.AsientoVentaDTO asiento : asientos) {
            // Remover bloqueos y agregar como vendido
            estadoActual.removeIf(a -> a.getFila().equals(asiento.getFila()) && a.getColumna().equals(asiento.getColumna()));
            AsientoEstadoDTO estado = new AsientoEstadoDTO();
            estado.setFila(asiento.getFila());
            estado.setColumna(asiento.getColumna());
            estado.setEstado("Vendido");
            estadoActual.add(estado);

            // Construir respuesta
            um.prog2.dto.venta.AsientoVentaEstadoDTO asientoResp = new um.prog2.dto.venta.AsientoVentaEstadoDTO();
            asientoResp.setFila(asiento.getFila());
            asientoResp.setColumna(asiento.getColumna());
            asientoResp.setPersona(asiento.getPersona());
            asientoResp.setEstado("Ocupado");
            asientosRespuesta.add(asientoResp);
        }

        response.setAsientos(asientosRespuesta);

        // Guardar venta
        VentaDTO venta = new VentaDTO();
        venta.setEventoId(eventoId);
        venta.setVentaId(ventaId);
        venta.setFechaVenta(response.getFechaVenta());
        venta.setAsientos(asientosRespuesta);
        venta.setResultado(true);
        venta.setDescripcion("Venta realizada con éxito");
        venta.setPrecioVenta(precioTotal);
        ventasGeneradas.add(venta);

        // Simular webhook asíncrono
        if (notificadorBackendService != null) {
            CompletableFuture.runAsync(() -> {
                try {
                    int delay = ThreadLocalRandom.current().nextInt(2000, 5000);
                    Thread.sleep(delay);

                    Map<String, Object> payload = new HashMap<>();
                    payload.put("ventaId", ventaId);
                    payload.put("eventoId", eventoId);
                    payload.put("asientos", asientos);
                    payload.put("fechaVenta", Instant.now().toString());
                    payload.put("montoTotal", venta.getPrecioVenta());

                    String payloadJson = objectMapper.writeValueAsString(payload);

                    BackendNotificacionDTO notificacion = new BackendNotificacionDTO();
                    notificacion.setTimestamp(Instant.now());
                    notificacion.setTopic("VENTA_COMPLETADA");
                    notificacion.setPartition(0);
                    notificacion.setOffset(ThreadLocalRandom.current().nextLong(1000, 9999));
                    notificacion.setKey("venta-" + ventaId);
                    notificacion.setPayload(payloadJson);

                    notificadorBackendService.notificarCambio(notificacion);
                    log.debug("MockCatedraService: webhook VENTA_COMPLETADA enviado para venta {}", ventaId);

                } catch (Exception e) {
                    log.error("MockCatedraService: error enviando webhook de venta", e);
                }
            });
        }

        return response;
    }

    /**
     * Lista ventas del usuario.
     */
    public List<VentaResumenDTO> listarVentas() {
        log.debug("MockCatedraService: listando ventas mock (total: {})", ventasGeneradas.size());
        List<VentaResumenDTO> resumenes = new ArrayList<>();
        for (VentaDTO venta : ventasGeneradas) {
            VentaResumenDTO resumen = new VentaResumenDTO();
            resumen.setEventoId(venta.getEventoId());
            resumen.setVentaId(venta.getVentaId());
            resumen.setFechaVenta(venta.getFechaVenta());
            resumen.setResultado(venta.getResultado());
            resumen.setDescripcion(venta.getDescripcion());
            resumen.setPrecioVenta(venta.getPrecioVenta());
            resumen.setCantidadAsientos(venta.getAsientos().size());
            resumenes.add(resumen);
        }
        return resumenes;
    }

    /**
     * Obtiene detalle de una venta.
     */
    public VentaDTO obtenerVenta(Long ventaId) {
        log.debug("MockCatedraService: obteniendo venta {} mock", ventaId);
        return ventasGeneradas.stream()
            .filter(v -> v.getVentaId().equals(ventaId))
            .findFirst()
            .orElse(null);
    }

    // ==================== HELPERS ====================

    private EventoResumenDTO crearEventoResumen(Long id, String titulo, String resumen, String fechaIso, BigDecimal precio, String tipoNombre) {
        EventoResumenDTO dto = new EventoResumenDTO();
        dto.setId(id);
        dto.setTitulo(titulo);
        dto.setResumen(resumen);
        dto.setDescripcion(resumen + " - Evento imperdible de " + tipoNombre.toLowerCase());
        dto.setFecha(Instant.parse(fechaIso));
        dto.setPrecioEntrada(precio);

        EventoTipoDTO tipo = new EventoTipoDTO();
        tipo.setNombre(tipoNombre);
        tipo.setDescripcion(tipoNombre);
        dto.setEventoTipo(tipo);

        return dto;
    }

    private String obtenerTipoPorId(Long id) {
        switch (id.intValue()) {
            case 1: return "Conferencia";
            case 2: return "Concierto";
            case 3: return "Teatro";
            case 4: return "Comedia";
            case 5: return "Cine";
            default: return "Evento";
        }
    }

    private List<IntegranteBasicDTO> crearIntegrantesMock(Long eventoId) {
        List<IntegranteBasicDTO> integrantes = new ArrayList<>();

        IntegranteBasicDTO int1 = new IntegranteBasicDTO();
        int1.setNombre("Juan");
        int1.setApellido("Pérez");
        int1.setIdentificacion("Speaker Principal");
        integrantes.add(int1);

        IntegranteBasicDTO int2 = new IntegranteBasicDTO();
        int2.setNombre("María");
        int2.setApellido("González");
        int2.setIdentificacion("Co-host");
        integrantes.add(int2);

        return integrantes;
    }

    private BigDecimal calcularMontoTotal(Long eventoId, int cantidadAsientos) {
        EventoDetalleDTO detalle = obtenerEventoDetalle(eventoId);
        if (detalle == null) return BigDecimal.ZERO;
        return detalle.getPrecioEntrada().multiply(new BigDecimal(cantidadAsientos));
    }
}

