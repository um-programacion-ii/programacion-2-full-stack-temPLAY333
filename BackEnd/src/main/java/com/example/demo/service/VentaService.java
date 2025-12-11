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
import java.util.stream.Collectors;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * Servicio para realizar ventas de asientos a través del Proxy.
 */
@Service
@Transactional
public class VentaService {

    private static final Logger log = LoggerFactory.getLogger(VentaService.class);

    private final RestTemplate restTemplate;
    private final VentaRepository ventaRepository;
    private final VentaMapper ventaMapper;

    @Value("${app.proxy.base-url:http://localhost:8080}")
    private String proxyBaseUrl;

    public VentaService(VentaRepository ventaRepository, VentaMapper ventaMapper) {
        this.restTemplate = new RestTemplate();
        this.ventaRepository = ventaRepository;
        this.ventaMapper = ventaMapper;
    }

    /**
     * Realiza una venta de asientos para un evento.
     * Primero notifica a la Cátedra a través del Proxy.
     * Luego persiste la venta localmente en la BD.
     *
     * @param eventoId ID del evento
     * @param asientos Lista de asientos a vender (deben estar bloqueados)
     * @param username Usuario que realiza la compra
     * @return Respuesta de la venta
     */
    public RealizarVentaResponseDTO realizarVenta(Long eventoId, List<AsientoSeleccionDTO> asientos, String username) {
        log.info("Realizando venta de {} asientos para evento {} por usuario {}", asientos.size(), eventoId, username);

        try {
            // 1. Crear request DTO
            RealizarVentaRequestDTO request = new RealizarVentaRequestDTO();
            request.setEventoId(eventoId);

            // Convertir AsientoSeleccionDTO a AsientoVentaDTO
            List<AsientoVentaDTO> asientosVenta = asientos.stream()
                .map(a -> {
                    AsientoVentaDTO dto = new AsientoVentaDTO();
                    dto.setFila(a.getFila());
                    dto.setColumna(a.getColumna());
                    dto.setPersona(username); // Por defecto usar username, idealmente vendría del request
                    return dto;
                })
                .collect(Collectors.toList());

            request.setAsientos(asientosVenta);

            // 2. Llamar al Proxy para notificar a la Cátedra
            String url = proxyBaseUrl + "/api/ventas/realizar";
            ResponseEntity<RealizarVentaResponseDTO> responseEntity = restTemplate.postForEntity(
                url,
                request,
                RealizarVentaResponseDTO.class
            );

            RealizarVentaResponseDTO response = responseEntity.getBody();

            if (response != null && Boolean.TRUE.equals(response.getResultado())) {
                log.info("Venta realizada exitosamente en la Cátedra para evento {}", eventoId);

                // 3. Persistir venta localmente
                persistirVentaLocal(eventoId, asientos, username, response);

                return response;
            } else {
                log.warn("Venta para evento {} no fue exitosa: {}", eventoId, response != null ? response.getDescripcion() : "Sin respuesta");
                return response;
            }
        } catch (Exception e) {
            log.error("Error al realizar venta para evento {}", eventoId, e);

            // Retornar respuesta de error
            RealizarVentaResponseDTO errorResponse = new RealizarVentaResponseDTO();
            errorResponse.setResultado(false);
            errorResponse.setDescripcion("Error al comunicarse con el servicio: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Persiste la venta localmente en la BD del Backend.
     */
    private void persistirVentaLocal(Long eventoId, List<AsientoSeleccionDTO> asientos, String username, RealizarVentaResponseDTO response) {
        try {
            log.debug("Persistiendo venta local para evento {}", eventoId);

            // Nota: La persistencia completa se hará cuando se reciba la confirmación
            // asíncrona vía webhook desde el Proxy (evento VENTA_COMPLETADA de Kafka)
            // Por ahora solo registramos en log que la venta fue aceptada por la Cátedra

            log.info("Venta aceptada por la Cátedra para evento {}. Se persistirá cuando llegue confirmación vía Kafka", eventoId);

            // La persistencia real se hace en EventoWebhookService.procesarVentaCompletada()
            // cuando llega el evento VENTA_COMPLETADA desde Kafka vía Proxy
        } catch (Exception e) {
            log.error("Error al procesar venta localmente para evento {}", eventoId, e);
            // No lanzar excepción para no afectar la venta en la Cátedra
        }
    }

    /**
     * Obtiene todas las ventas de un usuario.
     *
     * @param username Usuario
     * @return Lista de ventas
     */
    @Transactional(readOnly = true)
    public List<VentaDTO> obtenerVentasUsuario(String username) {
        log.debug("Obteniendo ventas para usuario {}", username);

        // TODO: Filtrar por usuario
        List<Venta> ventas = ventaRepository.findAll();

        return ventas.stream()
            .map(ventaMapper::toDto)
            .toList();
    }

    /**
     * Obtiene los detalles de una venta específica.
     *
     * @param ventaId ID de la venta
     * @return Detalles de la venta
     */
    @Transactional(readOnly = true)
    public Optional<VentaDTO> obtenerVenta(Long ventaId) {
        log.debug("Obteniendo venta con ID {}", ventaId);

        return ventaRepository.findById(ventaId)
            .map(ventaMapper::toDto);
    }
}

