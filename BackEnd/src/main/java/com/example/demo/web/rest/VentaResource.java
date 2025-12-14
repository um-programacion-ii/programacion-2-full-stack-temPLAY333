package com.example.demo.web.rest;

import com.example.demo.service.VentaService;
import com.example.demo.service.dto.AsientoSeleccionDTO;
import com.example.demo.service.dto.RealizarVentaRequestDTO;
import com.example.demo.service.dto.RealizarVentaResponseDTO;
import com.example.demo.service.dto.VentaDTO;
import java.security.Principal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller para operaciones de venta.
 */
@RestController
@RequestMapping("/api/ventas")
public class VentaResource {

    private static final Logger log = LoggerFactory.getLogger(VentaResource.class);

    private final VentaService ventaService;

    public VentaResource(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    /**
     * POST /api/ventas/evento/{eventoId}/realizar : Realiza una venta de asientos.
     * Los asientos deben estar previamente bloqueados e incluir el nombre de la persona.
     *
     * Request Body:
     * {
     *   "eventoId": 1,
     *   "fecha": "2025-12-14T20:00:00.000Z",
     *   "precioVenta": 10000.00,
     *   "asientos": [
     *     { "fila": 2, "columna": 3, "persona": "Juan Pérez" },
     *     { "fila": 2, "columna": 4, "persona": "María García" }
     *   ]
     * }
     *
     * @param eventoId ID del evento (debe coincidir con el del body)
     * @param request Datos completos de la venta incluyendo asientos con nombres
     * @param principal Usuario autenticado
     * @return Respuesta de la venta
     */
    @PostMapping("/evento/{eventoId}/realizar")
    public ResponseEntity<RealizarVentaResponseDTO> realizarVenta(
        @PathVariable Long eventoId,
        @RequestBody RealizarVentaRequestDTO request,
        Principal principal
    ) {
        String username = principal.getName();
        log.debug("REST request para realizar venta de {} asientos del evento {} por usuario {}",
            request.getAsientos().size(), eventoId, username);

        // Validar que eventoId del path coincida con el del body
        if (!eventoId.equals(request.getEventoId())) {
            RealizarVentaResponseDTO errorResponse = new RealizarVentaResponseDTO();
            errorResponse.setResultado(false);
            errorResponse.setDescripcion("El eventoId del path no coincide con el del body");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Validar que haya asientos
        if (request.getAsientos().isEmpty()) {
            RealizarVentaResponseDTO errorResponse = new RealizarVentaResponseDTO();
            errorResponse.setResultado(false);
            errorResponse.setDescripcion("Debe seleccionar al menos un asiento");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        if (request.getAsientos().size() > 4) {
            RealizarVentaResponseDTO errorResponse = new RealizarVentaResponseDTO();
            errorResponse.setResultado(false);
            errorResponse.setDescripcion("No se pueden vender más de 4 asientos por compra");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Realizar venta a través del Proxy
        RealizarVentaResponseDTO response = ventaService.realizarVenta(request, username);

        if (Boolean.TRUE.equals(response.getResultado())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * GET /api/ventas : Obtiene todas las ventas del usuario autenticado.
     *
     * @param principal Usuario autenticado
     * @return Lista de ventas
     */
    @GetMapping
    public ResponseEntity<List<VentaDTO>> obtenerVentasUsuario(Principal principal) {
        String username = principal.getName();
        log.debug("REST request para obtener ventas del usuario {}", username);

        List<VentaDTO> ventas = ventaService.obtenerVentasUsuario(username);

        return ResponseEntity.ok(ventas);
    }

    /**
     * GET /api/ventas/{id} : Obtiene los detalles de una venta específica.
     * Solo permite acceder a ventas del usuario autenticado.
     *
     * @param id ID de la venta
     * @param principal Usuario autenticado
     * @return Detalles de la venta
     */
    @GetMapping("/{id}")
    public ResponseEntity<VentaDTO> obtenerVenta(@PathVariable Long id, Principal principal) {
        String username = principal.getName();
        log.debug("REST request para obtener venta {} del usuario {}", id, username);

        return ventaService.obtenerVenta(id, username)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}

