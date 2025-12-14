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
     * Devuelve 5 eventos con datos basados en MockData del mobile pero con variaciones.
     * Datos modificados para diferenciar origen (Proxy mock vs Mobile mock).
     */
    public List<EventoResumenDTO> listarEventosResumidos() {
        log.debug("MockCatedraService: devolviendo eventos resumidos mock");
        return Arrays.asList(
            crearEventoResumen(1L, "Concierto Sinfónico de Rock Clásico", "Rock meets orchestra en una noche épica",
                "2025-12-26T21:00:00Z", new BigDecimal("2800.00"), "Música"),
            crearEventoResumen(2L, "Gran Final de Campeonato", "El partido que definirá al campeón absoluto",
                "2025-12-28T19:30:00Z", new BigDecimal("5200.00"), "Deportes"),
            crearEventoResumen(3L, "Teatro Clásico Moderno", "Clásicos reimaginados para hoy",
                "2026-01-08T20:30:00Z", new BigDecimal("3500.00"), "Teatro"),
            crearEventoResumen(4L, "Noche de Stand-Up", "Los mejores comediantes en vivo",
                "2026-01-12T22:00:00Z", new BigDecimal("2100.00"), "Comedia"),
            crearEventoResumen(5L, "Festival Internacional de Jazz", "3 días de jazz en vivo",
                "2026-01-18T20:00:00Z", new BigDecimal("4000.00"), "Música")
        );
    }

    /**
     * Devuelve eventos completos (mismo listado pero con estructura EventoDTO).
     * Usa los datos completos de cada evento con todos los campos requeridos.
     */
    public List<EventoDTO> listarEventosCompletos() {
        log.debug("MockCatedraService: devolviendo eventos completos mock");
        List<EventoDTO> completos = new ArrayList<>();

        // Obtener datos completos de cada evento (1 a 5)
        for (Long id = 1L; id <= 5L; id++) {
            EventoDetalleDTO detalle = obtenerEventoDetalle(id);
            if (detalle != null) {
                EventoDTO completo = new EventoDTO();
                completo.setId(detalle.getId());
                completo.setTitulo(detalle.getTitulo());
                completo.setResumen(detalle.getResumen());
                completo.setDescripcion(detalle.getDescripcion());
                completo.setFecha(detalle.getFecha());
                completo.setDireccion(detalle.getDireccion());
                completo.setImagen(detalle.getImagen());
                completo.setFilaAsientos(detalle.getFilaAsientos());
                completo.setColumnAsientos(detalle.getColumnAsientos());
                completo.setPrecioEntrada(detalle.getPrecioEntrada());

                // Convertir EventoTipoBasicDTO a EventoTipoDTO
                EventoTipoDTO tipo = new EventoTipoDTO();
                tipo.setNombre(detalle.getEventoTipo().getNombre());
                tipo.setDescripcion(detalle.getEventoTipo().getDescripcion());
                completo.setEventoTipo(tipo);

                // Convertir integrantes si es necesario (EventoDTO usa Set<IntegranteDTO>)
                // Por ahora lo dejamos vacío ya que EventoDTO.integrantes es un Set

                completos.add(completo);
            }
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
                detalle.setTitulo("Concierto Sinfónico de Rock Clásico");
                detalle.setResumen("Rock meets orchestra en una noche épica");
                detalle.setDescripcion("Una fusión extraordinaria donde la Orquesta Filarmónica Nacional interpreta los clásicos inmortales de Led Zeppelin, Pink Floyd y Queen. Eternal Echoes tributo oficial se une para una experiencia sonora única que combina la potencia del rock con la elegancia de la música clásica.");
                detalle.setFecha(Instant.parse("2025-12-26T21:00:00Z"));
                detalle.setDireccion("Teatro Colón, Cerrito 1234, Buenos Aires");
                detalle.setImagen("https://images.unsplash.com/photo-1514320291840-2e0a9bf2a9ae?w=800&q=80");
                detalle.setFilaAsientos(18);
                detalle.setColumnAsientos(25);
                detalle.setPrecioEntrada(new BigDecimal("2800.00"));
                break;
            case 2:
                detalle.setTitulo("Gran Final de Campeonato");
                detalle.setResumen("El partido que definirá al campeón absoluto");
                detalle.setDescripcion("La final más esperada del año. Dos equipos legendarios se enfrentan en un duelo épico que quedará en la historia. Emoción, adrenalina y gloria esperan en cada jugada de este encuentro definitivo.");
                detalle.setFecha(Instant.parse("2025-12-28T19:30:00Z"));
                detalle.setDireccion("Estadio Monumental, Av. Figueroa Alcorta 7597, Buenos Aires");
                detalle.setImagen("https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=800&q=80");
                detalle.setFilaAsientos(20);
                detalle.setColumnAsientos(30);
                detalle.setPrecioEntrada(new BigDecimal("5200.00"));
                break;
            case 3:
                detalle.setTitulo("Teatro Clásico Moderno");
                detalle.setResumen("Clásicos reimaginados para hoy");
                detalle.setDescripcion("Una reinterpretación vanguardista de Hamlet con elementos multimedia y actuación contemporánea. El Teatro Nacional presenta esta obra maestra de Shakespeare en una versión que dialoga con nuestro presente, con un elenco de primer nivel.");
                detalle.setFecha(Instant.parse("2026-01-08T20:30:00Z"));
                detalle.setDireccion("Teatro San Martín, Av. Corrientes 1530, Buenos Aires");
                detalle.setImagen("https://images.unsplash.com/photo-1503095396549-807759245b35?w=800&q=80");
                detalle.setFilaAsientos(12);
                detalle.setColumnAsientos(15);
                detalle.setPrecioEntrada(new BigDecimal("3500.00"));
                break;
            case 4:
                detalle.setTitulo("Noche de Stand-Up");
                detalle.setResumen("Los mejores comediantes en vivo");
                detalle.setDescripcion("Una velada inolvidable con los comediantes más reconocidos del circuito nacional. Humor inteligente, observaciones agudas y carcajadas garantizadas en una noche que recordarás por mucho tiempo.");
                detalle.setFecha(Instant.parse("2026-01-12T22:00:00Z"));
                detalle.setDireccion("Teatro Broadway, Av. Corrientes 1155, Buenos Aires");
                detalle.setImagen("https://images.unsplash.com/photo-1585699324551-f6c309eedeca?w=800&q=80");
                detalle.setFilaAsientos(10);
                detalle.setColumnAsientos(12);
                detalle.setPrecioEntrada(new BigDecimal("2100.00"));
                break;
            case 5:
                detalle.setTitulo("Festival Internacional de Jazz");
                detalle.setResumen("3 días de jazz en vivo");
                detalle.setDescripcion("El festival de jazz más importante de la región. Tres noches consecutivas con artistas internacionales de primer nivel. Desde bebop hasta jazz fusion, una celebración completa del género en todas sus expresiones.");
                detalle.setFecha(Instant.parse("2026-01-18T20:00:00Z"));
                detalle.setDireccion("Centro Cultural Kirchner, Sarmiento 151, Buenos Aires");
                detalle.setImagen("https://images.unsplash.com/photo-1511192336575-5a79af67a629?w=800&q=80");
                detalle.setFilaAsientos(16);
                detalle.setColumnAsientos(22);
                detalle.setPrecioEntrada(new BigDecimal("4000.00"));
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
            case 1: return "Música";
            case 2: return "Deportes";
            case 3: return "Teatro";
            case 4: return "Comedia";
            case 5: return "Música";
            default: return "Evento";
        }
    }

    private List<IntegranteBasicDTO> crearIntegrantesMock(Long eventoId) {
        List<IntegranteBasicDTO> integrantes = new ArrayList<>();

        switch (eventoId.intValue()) {
            case 1: // Rock Sinfónico
                integrantes.add(crearIntegrante("Robert", "Plant", "Vocalista Principal"));
                integrantes.add(crearIntegrante("Jimmy", "Page", "Guitarrista Líder"));
                integrantes.add(crearIntegrante("John", "Bonham", "Baterista"));
                break;
            case 2: // Deportes
                integrantes.add(crearIntegrante("Diego", "Martínez", "Capitán Equipo A"));
                integrantes.add(crearIntegrante("Lucas", "Fernández", "Capitán Equipo B"));
                break;
            case 3: // Teatro
                integrantes.add(crearIntegrante("Elena", "Rossi", "Protagonista"));
                integrantes.add(crearIntegrante("Martín", "Soto", "Actor Principal"));
                integrantes.add(crearIntegrante("Ana", "Belén", "Directora"));
                break;
            case 4: // Comedia
                integrantes.add(crearIntegrante("Carlos", "López", "Comediante Estelar"));
                integrantes.add(crearIntegrante("Laura", "Méndez", "Anfitriona"));
                break;
            case 5: // Jazz
                integrantes.add(crearIntegrante("Miles", "Davis Jr", "Trompetista"));
                integrantes.add(crearIntegrante("John", "Coltrane II", "Saxofonista"));
                integrantes.add(crearIntegrante("Herbie", "Hancock III", "Pianista"));
                break;
        }

        return integrantes;
    }

    private IntegranteBasicDTO crearIntegrante(String nombre, String apellido, String rol) {
        IntegranteBasicDTO integrante = new IntegranteBasicDTO();
        integrante.setNombre(nombre);
        integrante.setApellido(apellido);
        integrante.setIdentificacion(rol);
        return integrante;
    }

    private BigDecimal calcularMontoTotal(Long eventoId, int cantidadAsientos) {
        EventoDetalleDTO detalle = obtenerEventoDetalle(eventoId);
        if (detalle == null) return BigDecimal.ZERO;
        return detalle.getPrecioEntrada().multiply(new BigDecimal(cantidadAsientos));
    }
}

