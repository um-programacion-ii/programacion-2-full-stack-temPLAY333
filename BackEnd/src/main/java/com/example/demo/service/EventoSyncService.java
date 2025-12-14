package com.example.demo.service;

import com.example.demo.domain.Evento;
import com.example.demo.domain.EventoTipo;
import com.example.demo.domain.Integrante;
import com.example.demo.repository.EventoRepository;
import com.example.demo.repository.EventoTipoRepository;
import com.example.demo.repository.IntegranteRepository;
import com.example.demo.service.dto.EventoDTO;
import com.example.demo.service.dto.EventoDetalleDTO;
import com.example.demo.service.dto.EventoResumenDTO;
import com.example.demo.service.dto.EventoTipoDTO;
import com.example.demo.service.dto.IntegranteBasicDTO;
import com.example.demo.service.dto.IntegranteDTO;
import com.example.demo.service.mapper.EventoMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * Servicio para sincronizar eventos desde la Cátedra hacia la base de datos local.
 * Se ejecuta automáticamente cada hora.
 */
@Service
@Transactional
public class EventoSyncService {

    private static final Logger log = LoggerFactory.getLogger(EventoSyncService.class);

    private final RestTemplate restTemplate;
    private final EventoRepository eventoRepository;
    private final EventoTipoRepository eventoTipoRepository;
    private final IntegranteRepository integranteRepository;
    private final EventoMapper eventoMapper;
    private final EntityManager entityManager;

    // Evita procesar el mismo evento en paralelo
    private final ConcurrentMap<Long, Object> processingLocks = new ConcurrentHashMap<>();

    @Value("${app.proxy.base-url:http://localhost:8080}")
    private String proxyBaseUrl;

    public EventoSyncService(
        EventoRepository eventoRepository,
        EventoTipoRepository eventoTipoRepository,
        IntegranteRepository integranteRepository,
        EventoMapper eventoMapper,
        EntityManager entityManager
    ) {
        this.restTemplate = new RestTemplate();
        this.eventoRepository = eventoRepository;
        this.eventoTipoRepository = eventoTipoRepository;
        this.integranteRepository = integranteRepository;
        this.eventoMapper = eventoMapper;
        this.entityManager = entityManager;
    }

    /**
     * Sincroniza todos los eventos desde la Cátedra.
     * Se ejecuta cada hora: cron "0 0 * * * *" = segundo 0, minuto 0, cada hora
     * Solo si app.sync.enabled=true
     */
    @Scheduled(cron = "0 0 * * * *")
    @ConditionalOnProperty(name = "app.sync.enabled", havingValue = "true", matchIfMissing = true)
    public void syncAllEvents() {
        log.info("Iniciando sincronización automática de eventos desde Cátedra");
        try {
            syncEventsFromCatedra();
            log.info("Sincronización de eventos completada exitosamente");
        } catch (Exception e) {
            log.error("Error al sincronizar eventos desde Cátedra", e);
        }
    }

    /**
     * Sincroniza eventos manualmente (útil para testing o triggers manuales).
     */
    public void syncEventsFromCatedra() {
        log.debug("Obteniendo lista completa de eventos desde el Proxy");

        // Obtener eventos desde el Proxy
        String url = proxyBaseUrl + "/api/eventos";
        EventoDTO[] eventosArray = restTemplate.getForObject(url, EventoDTO[].class);
        List<EventoDTO> eventosFromCatedra = eventosArray != null ? List.of(eventosArray) : List.of();

        if (eventosFromCatedra.isEmpty()) {
            log.warn("No se obtuvieron eventos desde el Proxy");
            return;
        }

        log.debug("Se obtuvieron {} eventos desde el Proxy", eventosFromCatedra.size());

        // Procesar cada evento
        for (EventoDTO eventoDTO : eventosFromCatedra) {
            try {
                processAndSaveEvento(eventoDTO);
            } catch (Exception e) {
                log.error("Error al procesar evento con ID {}", eventoDTO.getId(), e);
            }
        }
    }

    /**
     * Sincroniza un evento específico desde el Proxy.
     */
    public void syncEventoById(Long eventoId) {
        log.debug("Sincronizando evento específico con ID {} desde el Proxy", eventoId);

        try {
            String url = proxyBaseUrl + "/api/eventos/" + eventoId;
            EventoDetalleDTO eventoDetalle = restTemplate.getForObject(url, EventoDetalleDTO.class);

            if (eventoDetalle == null) {
                log.warn("No se encontró evento con ID {} en el Proxy", eventoId);
                return;
            }

            processAndSaveEventoDetalle(eventoDetalle);
            log.info("Evento {} sincronizado exitosamente", eventoId);
        } catch (Exception e) {
            log.error("Error al sincronizar evento con ID {}", eventoId, e);
        }
    }

    private void processAndSaveEvento(EventoDTO eventoDTO) {
        Long externalId = eventoDTO.getId();
        Object lock = new Object();
        Object existing = processingLocks.putIfAbsent(externalId, lock);
        if (existing != null) {
            log.warn("Evento {} ya está siendo procesado por otra tarea, se omite ejecución concurrente", externalId);
            return;
        }
        try {
            if (eventoDTO.getId() == null) {
                log.error("EventoDTO sin ID, no se puede procesar: {}", eventoDTO);
                throw new IllegalArgumentException("El evento debe tener un ID");
            }

            // Buscar por externalId
            Evento evento;
            Optional<Evento> found = eventoRepository.findOneByExternalId(externalId);
            boolean exists = found.isPresent();
            if (exists) {
                evento = found.get();
            } else {
                evento = new Evento();
                evento.setExternalId(externalId);
            }

            // Mapear datos básicos
            // NOTA: No tocar la PK `id` (GeneratedValue). Usamos externalId para correlación.
            // evento.setId(eventoDTO.getId()); // no asignamos PK manualmente
            evento.setTitulo(eventoDTO.getTitulo());
            evento.setResumen(eventoDTO.getResumen());
            evento.setDescripcion(eventoDTO.getDescripcion());
            evento.setFecha(eventoDTO.getFecha());
            evento.setDireccion(eventoDTO.getDireccion());
            evento.setImagen(eventoDTO.getImagen());
            evento.setFilaAsientos(eventoDTO.getFilaAsientos());
            evento.setColumnAsientos(eventoDTO.getColumnAsientos());
            evento.setPrecioEntrada(eventoDTO.getPrecioEntrada());

            // Procesar EventoTipo
            if (eventoDTO.getEventoTipo() != null && eventoDTO.getEventoTipo().getNombre() != null) {
                EventoTipo eventoTipo;

                // Si tiene ID, intentar buscar por ID primero
                if (eventoDTO.getEventoTipo().getId() != null) {
                    eventoTipo = eventoTipoRepository
                        .findById(eventoDTO.getEventoTipo().getId())
                        .orElseGet(() -> crearNuevoEventoTipo(eventoDTO.getEventoTipo()));
                } else {
                    // Si no tiene ID, buscar por nombre
                    eventoTipo = eventoTipoRepository
                        .findAll()
                        .stream()
                        .filter(et -> et.getNombre().equals(eventoDTO.getEventoTipo().getNombre()))
                        .findFirst()
                        .orElseGet(() -> crearNuevoEventoTipo(eventoDTO.getEventoTipo()));
                }
                evento.setEventoTipo(eventoTipo);
            } else {
                log.warn("Evento {} no tiene tipo de evento asignado o nombre de tipo es nulo", eventoDTO.getId());
            }

            // Procesar integrantes
            if (eventoDTO.getIntegrantes() != null && !eventoDTO.getIntegrantes().isEmpty()) {
                Set<Integrante> integrantes = new HashSet<>();
                for (IntegranteDTO integranteDTO : eventoDTO.getIntegrantes()) {
                    if (integranteDTO.getId() == null) {
                        log.warn("Integrante sin ID en evento {}, se omitirá", eventoDTO.getId());
                        continue;
                    }

                    Integrante integrante = integranteRepository
                        .findById(integranteDTO.getId())
                        .orElseGet(() -> {
                            Integrante nuevoIntegrante = new Integrante();
                            nuevoIntegrante.setId(integranteDTO.getId());
                            nuevoIntegrante.setNombre(integranteDTO.getNombre());
                            nuevoIntegrante.setApellido(integranteDTO.getApellido());
                            nuevoIntegrante.setIdentificacion(integranteDTO.getIdentificacion());
                            return integranteRepository.save(nuevoIntegrante);
                        });
                    integrantes.add(integrante);
                }
                evento.setIntegrantes(integrantes);
            }

            // Guardar en BD local
            try {
                if (exists) {
                    eventoRepository.save(evento);
                } else {
                    // persist explicitamente para insertar un nuevo registro; externalId ya seteado
                    entityManager.persist(evento);
                }
                log.debug("Evento {} guardado/actualizado en BD local", evento.getId());
            } catch (Exception e) {
                log.error("Error guardando evento {}: {}", eventoId, e.getMessage(), e);
                // rethrow to be caught by outer caller and avoid silent data inconsistency
                throw e;
            } finally {
                // asegurar liberar lock
                processingLocks.remove(eventoId);
            }
        } catch (RuntimeException ex) {
            // asegurar liberar lock en caso de excepcion previa a la finally interno
            processingLocks.remove(eventoId);
            throw ex;
        }
    }

    private EventoTipo crearNuevoEventoTipo(EventoTipoDTO eventoTipoDTO) {
        EventoTipo nuevoTipo = new EventoTipo();
        if (eventoTipoDTO.getId() != null) {
            nuevoTipo.setId(eventoTipoDTO.getId());
        }
        nuevoTipo.setNombre(eventoTipoDTO.getNombre());
        nuevoTipo.setDescripcion(eventoTipoDTO.getDescripcion());
        return eventoTipoRepository.save(nuevoTipo);
    }

    private void processAndSaveEventoDetalle(EventoDetalleDTO eventoDetalleDTO) {
        // Validar que el evento tenga ID
        if (eventoDetalleDTO.getId() == null) {
            log.error("EventoDetalleDTO sin ID, no se puede procesar: {}", eventoDetalleDTO);
            throw new IllegalArgumentException("El evento debe tener un ID");
        }

        // Similar a processAndSaveEvento pero con más detalle
        Evento evento;
        Long id = eventoDetalleDTO.getId();
        boolean exists = eventoRepository.existsById(id);
        if (exists) {
            evento = eventoRepository.findById(id).orElse(new Evento());
        } else {
            evento = new Evento();
            evento.setId(id);
        }

        evento.setId(eventoDetalleDTO.getId());
        evento.setTitulo(eventoDetalleDTO.getTitulo());
        evento.setResumen(eventoDetalleDTO.getResumen());
        evento.setDescripcion(eventoDetalleDTO.getDescripcion());
        evento.setFecha(eventoDetalleDTO.getFecha());
        evento.setDireccion(eventoDetalleDTO.getDireccion());
        evento.setImagen(eventoDetalleDTO.getImagen());
        evento.setFilaAsientos(eventoDetalleDTO.getFilaAsientos());
        evento.setColumnAsientos(eventoDetalleDTO.getColumnAsientos());
        evento.setPrecioEntrada(eventoDetalleDTO.getPrecioEntrada());

        // EventoTipo - buscar por nombre ya que BasicDTO no tiene ID
        if (eventoDetalleDTO.getEventoTipo() != null && eventoDetalleDTO.getEventoTipo().getNombre() != null) {
            // Buscar por nombre o crear nuevo
            EventoTipo eventoTipo = eventoTipoRepository
                .findAll()
                .stream()
                .filter(et -> et.getNombre().equals(eventoDetalleDTO.getEventoTipo().getNombre()))
                .findFirst()
                .orElseGet(() -> {
                    EventoTipo nuevoTipo = new EventoTipo();
                    nuevoTipo.setNombre(eventoDetalleDTO.getEventoTipo().getNombre());
                    nuevoTipo.setDescripcion(eventoDetalleDTO.getEventoTipo().getDescripcion());
                    return eventoTipoRepository.save(nuevoTipo);
                });
            evento.setEventoTipo(eventoTipo);
        } else {
            log.warn("Evento detallado {} no tiene tipo de evento asignado o nombre es nulo", eventoDetalleDTO.getId());
        }

        // Integrantes - BasicDTO no tiene ID, buscar por nombre/apellido/identificación
        if (eventoDetalleDTO.getIntegrantes() != null && !eventoDetalleDTO.getIntegrantes().isEmpty()) {
            Set<Integrante> integrantes = new HashSet<>();
            for (IntegranteBasicDTO integranteBasicDTO : eventoDetalleDTO.getIntegrantes()) {
                // Validar que tenga los datos mínimos necesarios
                if (integranteBasicDTO.getNombre() == null ||
                    integranteBasicDTO.getApellido() == null ||
                    integranteBasicDTO.getIdentificacion() == null) {
                    log.warn("Integrante con datos incompletos en evento {}, se omitirá", eventoDetalleDTO.getId());
                    continue;
                }

                // Buscar por nombre y apellido o crear nuevo
                Integrante integrante = integranteRepository
                    .findAll()
                    .stream()
                    .filter(i ->
                        i.getNombre().equals(integranteBasicDTO.getNombre()) &&
                        i.getApellido().equals(integranteBasicDTO.getApellido()) &&
                        i.getIdentificacion().equals(integranteBasicDTO.getIdentificacion())
                    )
                    .findFirst()
                    .orElseGet(() -> {
                        Integrante nuevoIntegrante = new Integrante();
                        nuevoIntegrante.setNombre(integranteBasicDTO.getNombre());
                        nuevoIntegrante.setApellido(integranteBasicDTO.getApellido());
                        nuevoIntegrante.setIdentificacion(integranteBasicDTO.getIdentificacion());
                        return integranteRepository.save(nuevoIntegrante);
                    });
                integrantes.add(integrante);
            }
            evento.setIntegrantes(integrantes);
        }

        if (exists) {
            eventoRepository.save(evento);
        } else {
            entityManager.persist(evento);
        }
        log.debug("Evento detallado {} guardado/actualizado en BD local", evento.getId());
    }

    /**
     * Obtiene todos los eventos resumidos desde la BD local.
     * No consulta la Cátedra, solo lee la BD sincronizada.
     */
    public List<EventoResumenDTO> obtenerEventosResumidos() {
        log.debug("Obteniendo eventos resumidos desde BD local");

        List<Evento> eventos = eventoRepository.findAll();

        return eventos
            .stream()
            .map(evento -> {
                EventoResumenDTO dto = new EventoResumenDTO();
                dto.setId(evento.getId());
                dto.setTitulo(evento.getTitulo());
                dto.setResumen(evento.getResumen());
                dto.setDescripcion(evento.getDescripcion());
                dto.setFecha(evento.getFecha());
                dto.setPrecioEntrada(evento.getPrecioEntrada());
                if (evento.getEventoTipo() != null) {
                    EventoTipoDTO eventoTipoDTO = new EventoTipoDTO();
                    eventoTipoDTO.setId(evento.getEventoTipo().getId());
                    eventoTipoDTO.setNombre(evento.getEventoTipo().getNombre());
                    dto.setEventoTipo(eventoTipoDTO);
                }
                return dto;
            })
            .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los eventos completos desde la BD local.
     * No consulta la Cátedra, solo lee la BD sincronizada.
     */
    public List<EventoDTO> obtenerEventosCompletos() {
        log.debug("Obteniendo eventos completos desde BD local");

        List<Evento> eventos = eventoRepository.findAll();

        return eventos.stream().map(eventoMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Obtiene un evento específico por ID desde la BD local.
     * No consulta la Cátedra, solo lee la BD sincronizada.
     */
    public Optional<EventoDTO> obtenerEventoPorId(Long id) {
        log.debug("Obteniendo evento {} desde BD local", id);

        return eventoRepository.findById(id).map(eventoMapper::toDto);
    }
}

