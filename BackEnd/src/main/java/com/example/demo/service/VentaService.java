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
     * @param request Request completo con eventoId, fecha, precioVenta y asientos con persona
     * @param username Usuario que realiza la compra
     * @return Respuesta de la venta
     */
    public RealizarVentaResponseDTO realizarVenta(RealizarVentaRequestDTO request, String username) {
        log.info("Realizando venta de {} asientos para evento {} por usuario {}",
            request.getAsientos().size(), request.getEventoId(), username);

        try {
            // Llamar al Proxy para notificar a la Cátedra
            String url = proxyBaseUrl + "/api/ventas/realizar";
            ResponseEntity<RealizarVentaResponseDTO> responseEntity = restTemplate.postForEntity(
                url,
                request,
                RealizarVentaResponseDTO.class
            );

            RealizarVentaResponseDTO response = responseEntity.getBody();

            if (response != null && Boolean.TRUE.equals(response.getResultado())) {
                log.info("Venta realizada exitosamente en la Cátedra para evento {} con ventaId {}",
                    request.getEventoId(), response.getVentaId());

                // La persistencia local se hará cuando llegue la confirmación vía webhook
                // desde el Proxy (evento VENTA_COMPLETADA de Kafka)
                log.debug("Venta aceptada. Se persistirá cuando llegue confirmación vía Kafka");

            } else {
                log.warn("Venta para evento {} no fue exitosa: {}",
                    request.getEventoId(),
                    response != null ? response.getDescripcion() : "Sin respuesta");
            }
            return response;

        } catch (Exception e) {
            log.error("Error al realizar venta para evento {}", request.getEventoId(), e);

            // Retornar respuesta de error
            RealizarVentaResponseDTO errorResponse = new RealizarVentaResponseDTO();
            errorResponse.setResultado(false);
            errorResponse.setDescripcion("Error al comunicarse con el servicio: " + e.getMessage());
            return errorResponse;
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

        // Filtrar por usuario autenticado
        List<Venta> ventas = ventaRepository.findByUsuarioIsCurrentUser();

        return ventas.stream()
            .map(ventaMapper::toDto)
            .toList();
    }

    /**
     * Obtiene los detalles de una venta específica.
     * Valida que la venta pertenezca al usuario autenticado.
     *
     * @param ventaId ID de la venta
     * @param username Usuario autenticado
     * @return Detalles de la venta si pertenece al usuario
     */
    @Transactional(readOnly = true)
    public Optional<VentaDTO> obtenerVenta(Long ventaId, String username) {
        log.debug("Obteniendo venta con ID {} para usuario {}", ventaId, username);

        return ventaRepository.findById(ventaId)
            .filter(venta -> venta.getUsuario().getLogin().equals(username))
            .map(ventaMapper::toDto);
    }
}

