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
        log.info("========================================");
        log.info("Iniciando sincronizacion de eventos desde la Catedra");
        log.info("========================================");

        // Obtener eventos desde el proxy (Cátedra)
        List<EventoDTO> eventosFromCatedra = null;
        try {
            var response = eventoProxyController.listarEventos();
            eventosFromCatedra = response.getBody();
            log.info("Respuesta del proxy: status={}, eventos recibidos={}",
                response.getStatusCode(), eventosFromCatedra != null ? eventosFromCatedra.size() : 0);
        } catch (Exception e) {
            log.error("ERROR al obtener eventos desde el proxy: {}", e.getMessage(), e);
            return;
        }

        if (eventosFromCatedra == null || eventosFromCatedra.isEmpty()) {
            log.warn("No se obtuvieron eventos desde la Cátedra (lista null o vacia)");
            return;
        }

        log.info("Se obtuvieron {} eventos desde la Cátedra", eventosFromCatedra.size());

        // Procesar cada evento de forma aislada
        int procesados = 0;
        int guardados = 0;
        int errores = 0;

        for (EventoDTO eventoDTO : eventosFromCatedra) {
            try {
                procesados++;
                log.info("Procesando evento {}/{}: ID={}, titulo={}",
                    procesados, eventosFromCatedra.size(), eventoDTO.getId(), eventoDTO.getTitulo());

                // Verificar si el evento ya existe antes de procesar
                boolean yaExistia = eventoRepository.existsById(eventoDTO.getId());

                processAndSaveEvento(eventoDTO);

                // Verificar si ahora existe después de procesar
                boolean existeAhora = eventoRepository.existsById(eventoDTO.getId());
                if (existeAhora) {
                    guardados++;
                    if (!yaExistia) {
                        log.info("Evento {} fue creado exitosamente", eventoDTO.getId());
                    } else {
                        log.info("Evento {} fue actualizado exitosamente", eventoDTO.getId());
                    }
                } else {
                    log.warn("Evento {} NO existe en BD despues de procesar - NO se guardo", eventoDTO.getId());
                }
            } catch (Exception e) {
                errores++;
                log.error("Error al procesar evento con ID {}: {}", eventoDTO.getId(), e.getMessage(), e);
            }
        }

        log.info("========================================");
        log.info("Sincronizacion completada: {} procesados, {} guardados exitosamente, {} errores",
            procesados, guardados, errores);
        log.info("========================================");
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
            log.warn("EventoDTO recibido es null, se omite");
            return;
        }

        log.debug("Procesando evento ID={}, titulo={}, fecha={}, direccion={}, filaAsientos={}, columnAsientos={}, precioEntrada={}",
            eventoDTO.getId(),
            eventoDTO.getTitulo(),
            eventoDTO.getFecha(),
            eventoDTO.getDireccion(),
            eventoDTO.getFilaAsientos(),
            eventoDTO.getColumnAsientos(),
            eventoDTO.getPrecioEntrada());

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

        log.debug("Evento mapeado: ID={}, titulo={}, resumen={}, descripcion={}, fecha={}, direccion={}, filaAsientos={}, columnAsientos={}, precioEntrada={}",
            evento.getId(),
            evento.getTitulo(),
            evento.getResumen() != null ? evento.getResumen().substring(0, Math.min(30, evento.getResumen().length())) : "null",
            evento.getDescripcion() != null ? evento.getDescripcion().substring(0, Math.min(30, evento.getDescripcion().length())) : "null",
            evento.getFecha(),
            evento.getDireccion(),
            evento.getFilaAsientos(),
            evento.getColumnAsientos(),
            evento.getPrecioEntrada());

        // EventoTipo: buscar por id si viene, sino por nombre, sino crear
        if (eventoDTO.getEventoTipo() != null) {
            EventoTipo tipo = null;
            if (eventoDTO.getEventoTipo().getId() != null) {
                tipo = eventoTipoRepository.findById(eventoDTO.getEventoTipo().getId()).orElse(null);
            }
            if (tipo == null && eventoDTO.getEventoTipo().getNombre() != null) {
                String nombre = eventoDTO.getEventoTipo().getNombre().trim();
                // Usar método eficiente del repositorio en lugar de findAll().stream()
                tipo = eventoTipoRepository.findByNombreIgnoreCase(nombre).orElseGet(() -> {
                    EventoTipo nuevo = new EventoTipo();
                    nuevo.setNombre(nombre);
                    nuevo.setDescripcion(eventoDTO.getEventoTipo().getDescripcion());
                    // Guardar en transacción separada para evitar problemas
                    return saveEventoTipoNewTransaction(nuevo);
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
            Evento saved = saveEventoNewTransaction(evento);
            // Verificar si realmente se guardó comparando el objeto retornado con el original
            // Si retorna el mismo objeto sin cambios, significa que no se guardó por validación
            if (saved != null && saved.getId() != null) {
                // Verificar si el evento ya existía en la BD
                boolean yaExistia = eventoRepository.existsById(evento.getId());
                if (yaExistia) {
                    log.info("Evento {} actualizado exitosamente en BD local", evento.getId());
                } else {
                    log.info("Evento {} creado exitosamente en BD local (nuevo)", evento.getId());
                }
            } else {
                log.warn("Evento {} NO se guardo (retorno null o sin ID) - probablemente fallo validacion", evento.getId());
            }
        } catch (Exception e) {
            log.error("ERROR CRITICO guardando evento {}: {}", evento.getId(), e.getMessage(), e);
            log.error("Stack trace completo:", e);
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
            // Usar método eficiente del repositorio en lugar de findAll().stream()
            tipo = eventoTipoRepository.findByNombreIgnoreCase(nombre).orElseGet(() -> {
                EventoTipo nuevo = new EventoTipo();
                nuevo.setNombre(nombre);
                nuevo.setDescripcion(eventoDetalleDTO.getEventoTipo().getDescripcion());
                // Guardar en transacción separada para evitar problemas
                return saveEventoTipoNewTransaction(nuevo);
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
            Evento saved = saveEventoNewTransaction(evento);
            if (saved != null && saved.getId() != null) {
                log.info("Evento detallado {} guardado/actualizado exitosamente en BD local", evento.getId());
            } else {
                log.warn("Evento detallado {} NO se guardo (retorno null o sin ID)", evento.getId());
            }
        } catch (Exception e) {
            log.error("ERROR CRITICO guardando evento detallado {}: {}", evento.getId(), e.getMessage(), e);
            log.error("Stack trace completo:", e);
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Evento saveEventoNewTransaction(Evento evento) {
        // Validar TODOS los campos requeridos antes de persistir
        StringBuilder validationErrors = new StringBuilder();

        if (evento.getTitulo() == null || evento.getTitulo().trim().isEmpty()) {
            validationErrors.append("titulo es null o vacio; ");
        }
        if (evento.getResumen() == null || evento.getResumen().trim().isEmpty()) {
            validationErrors.append("resumen es null o vacio; ");
        }
        if (evento.getDescripcion() == null || evento.getDescripcion().trim().isEmpty()) {
            validationErrors.append("descripcion es null o vacio; ");
        }
        if (evento.getFecha() == null) {
            validationErrors.append("fecha es null; ");
        }
        if (evento.getDireccion() == null || evento.getDireccion().trim().isEmpty()) {
            validationErrors.append("direccion es null o vacia; ");
        }
        if (evento.getFilaAsientos() == null || evento.getFilaAsientos() < 1) {
            validationErrors.append("filaAsientos es null o < 1; ");
        }
        if (evento.getColumnAsientos() == null || evento.getColumnAsientos() < 1) {
            validationErrors.append("columnAsientos es null o < 1; ");
        }
        if (evento.getPrecioEntrada() == null || evento.getPrecioEntrada().compareTo(java.math.BigDecimal.ZERO) < 0) {
            validationErrors.append("precioEntrada es null o < 0; ");
        }
        if (evento.getEventoTipo() == null) {
            validationErrors.append("eventoTipo es null; ");
        }

        if (validationErrors.length() > 0) {
            log.warn("Evento {} incompleto, no se persiste en REQUIRES_NEW. Errores: {}", evento.getId(), validationErrors.toString());
            log.warn("Datos del evento: titulo={}, resumen={}, descripcion={}, fecha={}, direccion={}, filaAsientos={}, columnAsientos={}, precioEntrada={}, eventoTipo={}",
                evento.getTitulo(),
                evento.getResumen() != null ? evento.getResumen().substring(0, Math.min(50, evento.getResumen().length())) : "null",
                evento.getDescripcion() != null ? evento.getDescripcion().substring(0, Math.min(50, evento.getDescripcion().length())) : "null",
                evento.getFecha(),
                evento.getDireccion(),
                evento.getFilaAsientos(),
                evento.getColumnAsientos(),
                evento.getPrecioEntrada(),
                evento.getEventoTipo() != null ? evento.getEventoTipo().getNombre() : "null");
            return evento;
        }

        try {
            log.debug("Intentando guardar evento {} en BD. Datos: titulo={}, fecha={}, direccion={}",
                evento.getId(), evento.getTitulo(), evento.getFecha(), evento.getDireccion());
            Evento saved = eventoRepository.save(evento);
            // Forzar flush para asegurar que se persista inmediatamente
            eventoRepository.flush();
            log.info("[REQUIRES_NEW] Evento {} guardado exitosamente en BD. Titulo: {}, ID guardado: {}",
                saved.getId(), saved.getTitulo(), saved.getId());
            return saved;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.error("[REQUIRES_NEW] ERROR de integridad al guardar evento {}: {}", evento.getId(), e.getMessage(), e);
            if (e.getCause() != null) {
                log.error("Causa raiz: {}", e.getCause().getMessage());
            }
            throw e;
        } catch (jakarta.validation.ConstraintViolationException e) {
            log.error("[REQUIRES_NEW] ERROR de validacion al guardar evento {}: {}", evento.getId(), e.getMessage(), e);
            e.getConstraintViolations().forEach(v ->
                log.error("Violacion: {} - {}", v.getPropertyPath(), v.getMessage())
            );
            throw e;
        } catch (Exception e) {
            log.error("[REQUIRES_NEW] ERROR inesperado al guardar evento {} en BD: {}", evento.getId(), e.getMessage(), e);
            log.error("Tipo de excepcion: {}", e.getClass().getName());
            if (e.getCause() != null) {
                log.error("Causa: {}", e.getCause().getMessage());
            }
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EventoTipo saveEventoTipoNewTransaction(EventoTipo eventoTipo) {
        // Validar que el nombre esté presente
        if (eventoTipo.getNombre() == null || eventoTipo.getNombre().trim().isEmpty()) {
            log.warn("EventoTipo sin nombre, no se persiste en REQUIRES_NEW");
            return eventoTipo;
        }
        try {
            EventoTipo saved = eventoTipoRepository.save(eventoTipo);
            log.info("[REQUIRES_NEW] EventoTipo {} guardado exitosamente en BD (nombre: {})", saved.getId(), saved.getNombre());
            return saved;
        } catch (Exception e) {
            log.error("[REQUIRES_NEW] ERROR al guardar EventoTipo '{}' en BD: {}", eventoTipo.getNombre(), e.getMessage(), e);
            throw e;
        }
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
