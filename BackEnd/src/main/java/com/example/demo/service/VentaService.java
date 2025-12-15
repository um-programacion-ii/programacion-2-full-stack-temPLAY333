package com.example.demo.service;

import com.example.demo.domain.Venta;
import com.example.demo.repository.VentaRepository;
import com.example.demo.service.dto.AsientoSeleccionDTO;
import com.example.demo.service.dto.AsientoVentaDTO;
import com.example.demo.service.dto.RealizarVentaRequestDTO;
import com.example.demo.service.dto.RealizarVentaResponseDTO;
import com.example.demo.service.dto.VentaDTO;
import com.example.demo.service.mapper.VentaMapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * Servicio para realizar ventas de asientos a través del Proxy.
 *
 * NOTE: This is a concrete implementation class kept so tests that instantiate
 * VentaService directly (new VentaService(...)) continue to work. The real
 * Spring-managed bean remains `VentaServiceImpl` annotated with @Service.
 */
@Transactional
public class VentaService {

    private static final Logger LOG = LoggerFactory.getLogger(VentaService.class);

    protected final VentaRepository ventaRepository;

    protected final VentaMapper ventaMapper;

    protected final RestTemplate restTemplate;

    // default proxy base url; in tests the field can be overridden via ReflectionTestUtils
    protected String proxyBaseUrl = "http://localhost:8080";

    public VentaService(VentaRepository ventaRepository, VentaMapper ventaMapper) {
        this.ventaRepository = ventaRepository;
        this.ventaMapper = ventaMapper;
        this.restTemplate = new RestTemplate();
    }

    public VentaDTO save(VentaDTO ventaDTO) {
        LOG.debug("Request to save Venta : {}", ventaDTO);
        Venta venta = ventaMapper.toEntity(ventaDTO);
        venta = ventaRepository.save(venta);
        return ventaMapper.toDto(venta);
    }

    public VentaDTO update(VentaDTO ventaDTO) {
        LOG.debug("Request to update Venta : {}", ventaDTO);
        Venta venta = ventaMapper.toEntity(ventaDTO);
        venta = ventaRepository.save(venta);
        return ventaMapper.toDto(venta);
    }

    public Optional<VentaDTO> partialUpdate(VentaDTO ventaDTO) {
        LOG.debug("Request to partially update Venta : {}", ventaDTO);

        return ventaRepository
            .findById(ventaDTO.getId())
            .map(existingVenta -> {
                ventaMapper.partialUpdate(existingVenta, ventaDTO);
                return existingVenta;
            })
            .map(ventaRepository::save)
            .map(ventaMapper::toDto);
    }

    public Page<VentaDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Ventas");
        return ventaRepository.findAll(pageable).map(ventaMapper::toDto);
    }

    public Page<VentaDTO> findAllWithEagerRelationships(Pageable pageable) {
        return ventaRepository.findAllWithEagerRelationships(pageable).map(ventaMapper::toDto);
    }

    public Optional<VentaDTO> findOne(Long id) {
        LOG.debug("Request to get Venta : {}", id);
        return ventaRepository.findOneWithEagerRelationships(id).map(ventaMapper::toDto);
    }

    public void delete(Long id) {
        LOG.debug("Request to delete Venta : {}", id);
        ventaRepository.deleteById(id);
    }

    // Business methods
    public RealizarVentaResponseDTO realizarVenta(Long eventoId, List<AsientoSeleccionDTO> asientos, String username) {
        LOG.info("Realizando venta de {} asientos para evento {} por usuario {}", asientos.size(), eventoId, username);

        try {
            RealizarVentaRequestDTO request = new RealizarVentaRequestDTO();
            request.setEventoId(eventoId);

            List<AsientoVentaDTO> asientosVenta = asientos.stream()
                .map(a -> {
                    AsientoVentaDTO dto = new AsientoVentaDTO();
                    dto.setFila(a.getFila());
                    dto.setColumna(a.getColumna());
                    dto.setPersona(username);
                    return dto;
                })
                .collect(Collectors.toList());

            request.setAsientos(asientosVenta);

            String url = proxyBaseUrl + "/api/ventas/realizar";
            RealizarVentaResponseDTO response = restTemplate.postForObject(url, request, RealizarVentaResponseDTO.class);

            if (response != null && Boolean.TRUE.equals(response.getResultado())) {
                LOG.info("Venta realizada exitosamente en la Cátedra para evento {}", eventoId);
                persistirVentaLocal(eventoId, asientos, username, response);
            } else {
                LOG.warn("Venta para evento {} no fue exitosa: {}", eventoId, response != null ? response.getDescripcion() : "Sin respuesta");
            }
            return response != null ? response : crearErrorResponse("Sin respuesta del proxy");
        } catch (Exception e) {
            LOG.error("Error al realizar venta para evento {}", eventoId, e);
            return crearErrorResponse("Error al comunicarse con el servicio: " + e.getMessage());
        }
    }

    protected RealizarVentaResponseDTO crearErrorResponse(String msg) {
        RealizarVentaResponseDTO errorResponse = new RealizarVentaResponseDTO();
        errorResponse.setResultado(false);
        errorResponse.setDescripcion(msg);
        return errorResponse;
    }

    protected void persistirVentaLocal(Long eventoId, List<AsientoSeleccionDTO> asientos, String username, RealizarVentaResponseDTO response) {
        try {
            LOG.debug("Persistiendo venta local para evento {}", eventoId);
            LOG.info("Venta aceptada por la Cátedra para evento {}. Se persistirá cuando llegue confirmación vía Kafka", eventoId);
        } catch (Exception e) {
            LOG.error("Error al procesar venta localmente para evento {}", eventoId, e);
        }
    }

    public List<VentaDTO> obtenerVentasUsuario(String username) {
        LOG.debug("Obteniendo ventas para usuario {}", username);

        List<Venta> ventas = ventaRepository.findByUsuarioIsCurrentUser();

        return ventas.stream().map(ventaMapper::toDto).toList();
    }

    public Optional<VentaDTO> obtenerVenta(Long ventaId, String username) {
        LOG.debug("Obteniendo venta con ID {} para usuario {}", ventaId, username);

        return ventaRepository.findById(ventaId)
            .filter(venta -> venta.getUsuario().getLogin().equals(username))
            .map(ventaMapper::toDto);
    }
}
