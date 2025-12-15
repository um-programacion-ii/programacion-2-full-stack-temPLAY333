package com.example.demo.service;

import com.example.demo.service.dto.AsientoDTO;
import com.example.demo.service.dto.MapaAsientosDTO;
import java.util.List;
import java.util.Optional;

public interface AsientoService {

    // Métodos de estado de asientos (usados por los controladores para consultar Redis a través del proxy)
    MapaAsientosDTO obtenerEstadoAsientos(Long eventoId, Integer filas, Integer columnas);

    boolean isAsientoDisponible(MapaAsientosDTO mapa, int fila, int columna);

    // Métodos CRUD para la entidad Asiento
    AsientoDTO save(AsientoDTO asientoDTO);

    AsientoDTO update(AsientoDTO asientoDTO);

    Optional<AsientoDTO> partialUpdate(AsientoDTO asientoDTO);

    List<AsientoDTO> findAll();

    Optional<AsientoDTO> findOne(Long id);

    void delete(Long id);
}
