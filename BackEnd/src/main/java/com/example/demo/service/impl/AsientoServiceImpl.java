package com.example.demo.service.impl;

import com.example.demo.domain.Asiento;
import com.example.demo.repository.AsientoRepository;
import com.example.demo.service.AsientoService;
import com.example.demo.service.dto.AsientoDTO;
import com.example.demo.service.dto.AsientoRedisDTO;
import com.example.demo.service.dto.MapaAsientosDTO;
import com.example.demo.service.mapper.AsientoMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * Service Implementation for managing {@link com.example.demo.domain.Asiento} and proxy seat state.
 */
@Service
@Transactional
public class AsientoServiceImpl implements AsientoService {

    private static final Logger LOG = LoggerFactory.getLogger(AsientoServiceImpl.class);

    private final AsientoRepository asientoRepository;

    private final AsientoMapper asientoMapper;

    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate;

    @Value("${app.proxy.base-url:http://localhost:8080}")
    private String proxyBaseUrl;

    public AsientoServiceImpl(AsientoRepository asientoRepository, AsientoMapper asientoMapper, ObjectMapper objectMapper) {
        this.asientoRepository = asientoRepository;
        this.asientoMapper = asientoMapper;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public AsientoDTO save(AsientoDTO asientoDTO) {
        LOG.debug("Request to save Asiento : {}", asientoDTO);
        Asiento asiento = asientoMapper.toEntity(asientoDTO);
        asiento = asientoRepository.save(asiento);
        return asientoMapper.toDto(asiento);
    }

    @Override
    public AsientoDTO update(AsientoDTO asientoDTO) {
        LOG.debug("Request to update Asiento : {}", asientoDTO);
        Asiento asiento = asientoMapper.toEntity(asientoDTO);
        asiento = asientoRepository.save(asiento);
        return asientoMapper.toDto(asiento);
    }

    @Override
    public Optional<AsientoDTO> partialUpdate(AsientoDTO asientoDTO) {
        LOG.debug("Request to partially update Asiento : {}", asientoDTO);

        return asientoRepository
            .findById(asientoDTO.getId())
            .map(existingAsiento -> {
                asientoMapper.partialUpdate(existingAsiento, asientoDTO);

                return existingAsiento;
            })
            .map(asientoRepository::save)
            .map(asientoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsientoDTO> findAll() {
        LOG.debug("Request to get all Asientos");
        return asientoRepository.findAll().stream().map(asientoMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AsientoDTO> findOne(Long id) {
        LOG.debug("Request to get Asiento : {}", id);
        return asientoRepository.findById(id).map(asientoMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Asiento : {}", id);
        asientoRepository.deleteById(id);
    }

    // --- Proxy seat-state methods ---
    @Override
    public MapaAsientosDTO obtenerEstadoAsientos(Long eventoId, Integer filas, Integer columnas) {
        LOG.debug("Obteniendo estado de asientos para evento {} desde Proxy", eventoId);

        try {
            String url = proxyBaseUrl + "/api/eventos/" + eventoId + "/asientos-estado";
            String jsonResponse = restTemplate.getForObject(url, String.class);

            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                LOG.warn("No se obtuvieron datos de asientos para evento {}", eventoId);
                return crearMapaVacio(eventoId, filas, columnas);
            }

            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            MapaAsientosDTO mapa = new MapaAsientosDTO();
            mapa.setEventoId(eventoId);
            mapa.setTotalFilas(filas);
            mapa.setTotalColumnas(columnas);

            List<AsientoRedisDTO> asientos = new ArrayList<>();
            JsonNode asientosNode = rootNode.has("asientos") ? rootNode.get("asientos") : rootNode;

            if (asientosNode.isArray()) {
                for (JsonNode asientoNode : asientosNode) {
                    AsientoRedisDTO asiento = new AsientoRedisDTO();
                    asiento.setFila(asientoNode.get("fila").asInt());
                    asiento.setColumna(asientoNode.get("columna").asInt());
                    asiento.setEstado(asientoNode.get("estado").asText());

                    if (asientoNode.has("expira") && !asientoNode.get("expira").isNull()) {
                        String expiraStr = asientoNode.get("expira").asText();
                        asiento.setExpira(Instant.parse(expiraStr));
                    }

                    asientos.add(asiento);
                }
            }

            mapa.setAsientos(asientos);
            LOG.debug("Se obtuvieron {} asientos bloqueados/vendidos para evento {}", asientos.size(), eventoId);
            return mapa;
        } catch (Exception e) {
            LOG.error("Error al obtener estado de asientos para evento {} desde Proxy", eventoId, e);
            return crearMapaVacio(eventoId, filas, columnas);
        }
    }

    @Override
    public boolean isAsientoDisponible(MapaAsientosDTO mapa, int fila, int columna) {
        Instant ahora = Instant.now();

        for (AsientoRedisDTO asiento : mapa.getAsientos()) {
            if (asiento.getFila() == fila && asiento.getColumna() == columna) {
                String estado = asiento.getEstado();

                if ("Vendido".equalsIgnoreCase(estado)) {
                    return false;
                }

                if ("Bloqueado".equalsIgnoreCase(estado)) {
                    Instant expira = asiento.getExpira();
                    if (expira != null && ahora.isBefore(expira)) {
                        return false;
                    }
                    return true;
                }
            }
        }

        return true;
    }

    private MapaAsientosDTO crearMapaVacio(Long eventoId, Integer filas, Integer columnas) {
        MapaAsientosDTO mapa = new MapaAsientosDTO();
        mapa.setEventoId(eventoId);
        mapa.setTotalFilas(filas);
        mapa.setTotalColumnas(columnas);
        mapa.setAsientos(new ArrayList<>());
        return mapa;
    }
}
