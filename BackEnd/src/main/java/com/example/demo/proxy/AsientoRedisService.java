package com.example.demo.proxy;

import com.example.demo.service.dto.AsientoEstadoDTO;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Servicio que consulta el estado de los asientos de un evento en Redis de la cátedra.
 *
 * Implementación simplificada (stub) que por ahora devuelve una lista vacía para
 * evitar dependencias de Redis mientras se configura correctamente el entorno.
 */
@Service
public class AsientoRedisService {

    private static final Logger log = LoggerFactory.getLogger(AsientoRedisService.class);

    public AsientoRedisService() {
    }

    public List<AsientoEstadoDTO> obtenerEstadoAsientos(Long eventoId) {
        log.debug("Consulta de estado de asientos para evento {} (stub sin Redis)", eventoId);
        return Collections.emptyList();
    }
}
