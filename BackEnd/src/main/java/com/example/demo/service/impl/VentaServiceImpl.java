package com.example.demo.service.impl;

import com.example.demo.domain.Venta;
import com.example.demo.repository.VentaRepository;
import com.example.demo.service.VentaService;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * Service Implementation for managing {@link com.example.demo.domain.Venta}.
 */
@Service
@Transactional
public class VentaServiceImpl extends VentaService {

    private static final Logger LOG = LoggerFactory.getLogger(VentaServiceImpl.class);

    private final RestTemplate restTemplate;

    // base URL del proxy
    private final String proxyBaseUrl = "http://localhost:8080"; // valor por defecto; puede inyectarse si se desea

    public VentaServiceImpl(VentaRepository ventaRepository, VentaMapper ventaMapper) {
        super(ventaRepository, ventaMapper);
        this.restTemplate = new RestTemplate();
    }

    // The class inherits implementations from VentaService. You may override methods here if customization is required.
}
