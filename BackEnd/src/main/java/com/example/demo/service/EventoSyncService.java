package com.example.demo.service;

import com.example.demo.domain.Evento;
import com.example.demo.repository.EventoRepository;
import com.example.demo.service.dto.EventoDTO;
import com.example.demo.service.dto.EventoResumenDTO;
import com.example.demo.service.mapper.EventoMapper;
import com.example.demo.service.mapper.EventoResumenMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de sincronización de eventos locales con los datos de la cátedra a través del proxy.
 *
 * Regla simple inicial:
 * - La cátedra es la fuente de verdad.
 * - Para cada EventoDTO recibido, se hace upsert en la tabla evento.
 * - Eventos que ya no aparecen en el listado remoto se consideran expirados o eliminados
 *   (para esta primera versión solo los dejamos tal cual; la lógica de expiración se puede
 *   extender más adelante).
 */
@Service
@Transactional
public class EventoSyncService {

    private static final Logger log = LoggerFactory.getLogger(EventoSyncService.class);

    private final EventoRepository eventoRepository;
    private final EventoMapper eventoMapper;
    private final EventoResumenMapper eventoResumenMapper;
    private final EventoExternalClient eventoExternalClient;

    public EventoSyncService(
        EventoRepository eventoRepository,
        EventoMapper eventoMapper,
        EventoResumenMapper eventoResumenMapper,
        EventoExternalClient eventoExternalClient
    ) {
        this.eventoRepository = eventoRepository;
        this.eventoMapper = eventoMapper;
        this.eventoResumenMapper = eventoResumenMapper;
        this.eventoExternalClient = eventoExternalClient;
    }

    /**
     * Sincroniza todos los eventos desde el proxy (cátedra) a la base de datos local.
     * Usa el ID del DTO como clave (se asume que coincide con el ID de la entidad Evento).
     */
    public void sincronizarEventosCompletos() {
        log.debug("Sincronizando eventos completos desde proxy hacia BD local");
        List<EventoDTO> remotos = eventoExternalClient.listarEventosCompletos();
        Map<Long, EventoDTO> porId = new HashMap<>();
        for (EventoDTO dto : remotos) {
            if (dto.getId() != null) {
                porId.put(dto.getId(), dto);
            }
        }

        for (EventoDTO dto : porId.values()) {
            Optional<Evento> existenteOpt = eventoRepository.findById(dto.getId());
            Evento entidad;
            if (existenteOpt.isPresent()) {
                entidad = existenteOpt.get();
                // Sobrescribimos los campos básicos desde el DTO
                Evento actualizado = eventoMapper.toEntity(dto);
                actualizado.setId(entidad.getId());
                entidad = updatedFrom(actualizado, entidad);
            } else {
                entidad = eventoMapper.toEntity(dto);
            }
            eventoRepository.save(entidad);
        }
    }

    private Evento updatedFrom(Evento origen, Evento destino) {
        destino.setTitulo(origen.getTitulo());
        destino.setResumen(origen.getResumen());
        destino.setDescripcion(origen.getDescripcion());
        destino.setFecha(origen.getFecha());
        destino.setDireccion(origen.getDireccion());
        destino.setImagen(origen.getImagen());
        destino.setFilaAsientos(origen.getFilaAsientos());
        destino.setColumnAsientos(origen.getColumnAsientos());
        destino.setPrecioEntrada(origen.getPrecioEntrada());
        destino.setEventoTipo(origen.getEventoTipo());
        destino.setIntegrantes(origen.getIntegrantes());
        return destino;
    }

    /**
     * Devuelve una lista resumida de eventos desde la BD local.
     */
    @Transactional(readOnly = true)
    public List<EventoResumenDTO> obtenerEventosResumidos() {
        return eventoRepository.findAll().stream().map(eventoResumenMapper::toDto).toList();
    }

    /**
     * Devuelve una lista completa de eventos desde la BD local.
     */
    @Transactional(readOnly = true)
    public List<EventoDTO> obtenerEventosCompletos() {
        return eventoRepository.findAll().stream().map(eventoMapper::toDto).toList();
    }

    /**
     * Obtiene un evento individual desde la BD local.
     */
    @Transactional(readOnly = true)
    public Optional<EventoDTO> obtenerEventoPorId(Long id) {
        return eventoRepository.findById(id).map(eventoMapper::toDto);
    }
}

