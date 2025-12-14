package com.example.demo.service;

import com.example.demo.domain.Evento;
import com.example.demo.domain.EventoTipo;
import com.example.demo.domain.Integrante;
import com.example.demo.proxy.EventoProxyController;
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
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para sincronizar eventos desde la Cátedra hacia la base de datos local.
 * Se ejecuta automáticamente cada hora.
 */
@Service
@Transactional
public class EventoSyncService {

    private static final Logger log = LoggerFactory.getLogger(EventoSyncService.class);

    private final EventoProxyController eventoProxyController;
    private final EventoRepository eventoRepository;
    private final EventoTipoRepository eventoTipoRepository;
    private final IntegranteRepository integranteRepository;
    private final EventoMapper eventoMapper;

    public EventoSyncService(
        EventoProxyController eventoProxyController,
        EventoRepository eventoRepository,
        EventoTipoRepository eventoTipoRepository,
        IntegranteRepository integranteRepository,
        EventoMapper eventoMapper
    ) {
        this.eventoProxyController = eventoProxyController;
        this.eventoRepository = eventoRepository;
        this.eventoTipoRepository = eventoTipoRepository;
        this.integranteRepository = integranteRepository;
        this.eventoMapper = eventoMapper;
    }

    /**
     * Sincroniza todos los eventos desde la Cátedra.
     * Se ejecuta cada hora: cron "0 0 * * * *" = segundo 0, minuto 0, cada hora
     */
    @Scheduled(cron = "0 0 * * * *")
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
        log.debug("Obteniendo lista completa de eventos desde la Cátedra");

        // Obtener eventos desde el proxy (Cátedra)
        List<EventoDTO> eventosFromCatedra = eventoProxyController.listarEventos().getBody();

        if (eventosFromCatedra == null || eventosFromCatedra.isEmpty()) {
            log.warn("No se obtuvieron eventos desde la Cátedra");
            return;
        }

        log.debug("Se obtuvieron {} eventos desde la Cátedra", eventosFromCatedra.size());

        // Procesar cada evento de forma aislada
        for (EventoDTO eventoDTO : eventosFromCatedra) {
            try {
                processAndSaveEvento(eventoDTO);
            } catch (Exception e) {
                log.error("Error al procesar evento con ID {}: {}", eventoDTO.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Sincroniza un evento específico desde la Cátedra.
     */
    public void syncEventoById(Long eventoId) {
        log.debug("Sincronizando evento específico con ID {} desde la Cátedra", eventoId);

        try {
            EventoDetalleDTO eventoDetalle = eventoProxyController.obtenerEvento(eventoId).getBody();

            if (eventoDetalle == null) {
                log.warn("No se encontró evento con ID {} en la Cátedra", eventoId);
                return;
            }

            processAndSaveEventoDetalle(eventoDetalle);
            log.info("Evento {} sincronizado exitosamente", eventoId);
        } catch (Exception e) {
            log.error("Error al sincronizar evento con ID {}", eventoId, e);
        }
    }

    private void processAndSaveEvento(EventoDTO eventoDTO) {
        if (eventoDTO == null) {
            return;
        }
        // Mapear en objeto entidad
        Evento evento = eventoRepository.findById(eventoDTO.getId()).orElse(new Evento());
        evento.setId(eventoDTO.getId());
        evento.setTitulo(eventoDTO.getTitulo());
        evento.setResumen(eventoDTO.getResumen());
        evento.setDescripcion(eventoDTO.getDescripcion());
        evento.setFecha(eventoDTO.getFecha() != null ? eventoDTO.getFecha() : Instant.now());
        evento.setDireccion(eventoDTO.getDireccion());
        evento.setImagen(eventoDTO.getImagen());
        evento.setFilaAsientos(eventoDTO.getFilaAsientos());
        evento.setColumnAsientos(eventoDTO.getColumnAsientos());
        evento.setPrecioEntrada(eventoDTO.getPrecioEntrada());

        // EventoTipo: buscar por id si viene, sino por nombre, sino crear
        if (eventoDTO.getEventoTipo() != null) {
            EventoTipo tipo = null;
            if (eventoDTO.getEventoTipo().getId() != null) {
                tipo = eventoTipoRepository.findById(eventoDTO.getEventoTipo().getId()).orElse(null);
            }
            if (tipo == null && eventoDTO.getEventoTipo().getNombre() != null) {
                String nombre = eventoDTO.getEventoTipo().getNombre().trim();
                tipo = eventoTipoRepository.findAll()
                    .stream()
                    .filter(et -> et.getNombre().equalsIgnoreCase(nombre))
                    .findFirst()
                    .orElseGet(() -> {
                        EventoTipo nuevo = new EventoTipo();
                        nuevo.setNombre(nombre);
                        nuevo.setDescripcion(eventoDTO.getEventoTipo().getDescripcion());
                        return eventoTipoRepository.save(nuevo);
                    });
            }
            evento.setEventoTipo(tipo);
        }

        // Integrantes: backend crea ID siempre. Buscar por identificacion o por nombre/apellido, si no crear nuevo.
        Set<Integrante> integrantes = new HashSet<>();
        if (eventoDTO.getIntegrantes() != null) {
            int idx = 0;
            for (IntegranteDTO integranteDTO : eventoDTO.getIntegrantes()) {
                idx++;
                Integrante integrante = null;
                String identificacion = integranteDTO.getIdentificacion() != null ? integranteDTO.getIdentificacion().trim() : null;
                if (identificacion != null && !identificacion.isEmpty()) {
                    integrante = integranteRepository.findByIdentificacion(identificacion).orElse(null);
                }
                if (integrante == null) {
                    String nombre = integranteDTO.getNombre() != null ? integranteDTO.getNombre().trim() : "";
                    String apellido = integranteDTO.getApellido() != null ? integranteDTO.getApellido().trim() : "";
                    if (!nombre.isEmpty() || !apellido.isEmpty()) {
                        integrante = integranteRepository.findByNombreAndApellido(nombre, apellido).orElse(null);
                    }
                }
                if (integrante == null) {
                    // crear uno nuevo; si no hay identificacion, generar placeholder para cumplir not-null
                    String idGen = identificacion != null && !identificacion.isEmpty() ? identificacion : ("gen-" + eventoDTO.getId() + "-" + idx);
                    Integrante nuevo = new Integrante();
                    nuevo.setNombre(integranteDTO.getNombre() != null ? integranteDTO.getNombre() : "");
                    nuevo.setApellido(integranteDTO.getApellido() != null ? integranteDTO.getApellido() : "");
                    nuevo.setIdentificacion(idGen);
                    integrante = integranteRepository.save(nuevo);
                    log.debug("Integrante creado por backend id={} identificacion={}", integrante.getId(), integrante.getIdentificacion());
                }
                integrantes.add(integrante);
            }
        }
        evento.setIntegrantes(integrantes);

        // Persistir evento en su propia transacción para evitar rollback global
        try {
            saveEventoNewTransaction(evento);
            log.debug("Evento {} guardado/actualizado en BD local (REQUIRES_NEW)", evento.getId());
        } catch (Exception e) {
            log.error("Error guardando evento {}: {}", evento.getId(), e.getMessage(), e);
            throw e;
        }
    }

    private void processAndSaveEventoDetalle(EventoDetalleDTO eventoDetalleDTO) {
        if (eventoDetalleDTO == null) {
            return;
        }
        Evento evento = eventoRepository.findById(eventoDetalleDTO.getId()).orElse(new Evento());
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

        // EventoTipo
        if (eventoDetalleDTO.getEventoTipo() != null) {
            EventoTipo tipo = null;
            // EventoTipoBasicDTO doesn't have an ID; search by nombre
            String nombre = eventoDetalleDTO.getEventoTipo().getNombre().trim();
            tipo = eventoTipoRepository.findAll()
                .stream()
                .filter(et -> et.getNombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElseGet(() -> {
                    EventoTipo nuevo = new EventoTipo();
                    nuevo.setNombre(nombre);
                    nuevo.setDescripcion(eventoDetalleDTO.getEventoTipo().getDescripcion());
                    return eventoTipoRepository.save(nuevo);
                });
            evento.setEventoTipo(tipo);
        }

        // Integrantes
        Set<Integrante> integrantes = new HashSet<>();
        if (eventoDetalleDTO.getIntegrantes() != null) {
            int idx = 0;
            for (IntegranteBasicDTO integranteDTO : eventoDetalleDTO.getIntegrantes()) {
                idx++;
                Integrante integrante = null;
                String identificacion = integranteDTO.getIdentificacion() != null ? integranteDTO.getIdentificacion().trim() : null;
                if (identificacion != null && !identificacion.isEmpty()) {
                    integrante = integranteRepository.findByIdentificacion(identificacion).orElse(null);
                }
                if (integrante == null) {
                    String nombre = integranteDTO.getNombre() != null ? integranteDTO.getNombre().trim() : "";
                    String apellido = integranteDTO.getApellido() != null ? integranteDTO.getApellido().trim() : "";
                    if (!nombre.isEmpty() || !apellido.isEmpty()) {
                        integrante = integranteRepository.findByNombreAndApellido(nombre, apellido).orElse(null);
                    }
                }
                if (integrante == null) {
                    String idGen = identificacion != null && !identificacion.isEmpty() ? identificacion : ("gen-" + eventoDetalleDTO.getId() + "-" + idx);
                    Integrante nuevo = new Integrante();
                    nuevo.setNombre(integranteDTO.getNombre() != null ? integranteDTO.getNombre() : "");
                    nuevo.setApellido(integranteDTO.getApellido() != null ? integranteDTO.getApellido() : "");
                    nuevo.setIdentificacion(idGen);
                    integrante = integranteRepository.save(nuevo);
                    log.debug("Integrante creado por backend id={} identificacion={}", integrante.getId(), integrante.getIdentificacion());
                }
                integrantes.add(integrante);
            }
        }
        evento.setIntegrantes(integrantes);

        try {
            saveEventoNewTransaction(evento);
            log.debug("Evento detallado {} guardado/actualizado en BD local (REQUIRES_NEW)", evento.getId());
        } catch (Exception e) {
            log.error("Error guardando evento detallado {}: {}", evento.getId(), e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Evento saveEventoNewTransaction(Evento evento) {
        // Validar campos mínimos antes de persistir
        if (evento.getTitulo() == null || evento.getFecha() == null || evento.getDireccion() == null) {
            log.warn("Evento {} incompleto, no se persiste en REQUIRES_NEW", evento.getId());
            return evento;
        }
        Evento saved = eventoRepository.save(evento);
        log.debug("[REQUIRES_NEW] Evento {} guardado en BD", saved.getId());
        return saved;
    }

    // Public query methods for controllers / frontend
    public List<EventoResumenDTO> obtenerEventosResumidos() {
        return eventoRepository.findAll().stream().map(e -> {
            EventoResumenDTO dto = new EventoResumenDTO();
            dto.setId(e.getId());
            dto.setTitulo(e.getTitulo());
            dto.setResumen(e.getResumen());
            dto.setDescripcion(e.getDescripcion());
            dto.setFecha(e.getFecha());
            dto.setPrecioEntrada(e.getPrecioEntrada());
            if (e.getEventoTipo() != null) {
                EventoTipoDTO t = new EventoTipoDTO();
                t.setId(e.getEventoTipo().getId());
                t.setNombre(e.getEventoTipo().getNombre());
                dto.setEventoTipo(t);
            }
            return dto;
        }).collect(Collectors.toList());
    }

    public List<EventoDTO> obtenerEventosCompletos() {
        return eventoRepository.findAll().stream().map(eventoMapper::toDto).collect(Collectors.toList());
    }

    public Optional<EventoDTO> obtenerEventoPorId(Long id) {
        return eventoRepository.findById(id).map(eventoMapper::toDto);
    }
}
