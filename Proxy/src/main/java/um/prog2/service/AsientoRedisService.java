package um.prog2.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Service;
import um.prog2.dto.evento.asientos.AsientoBloqueoEstadoDTO;

import java.util.*;

@Service
public class AsientoRedisService {

    private static final Logger log = LoggerFactory.getLogger(AsientoRedisService.class);

    private final StringRedisTemplate redis;

    public AsientoRedisService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public List<AsientoBloqueoEstadoDTO> obtenerEstadoAsientos(Long eventoId) {
        List<AsientoBloqueoEstadoDTO> result = new ArrayList<>();
        String hashKey = "evento:" + eventoId + ":asientos";

        try {
            HashOperations<String, String, String> hashOps = redis.opsForHash();
            Map<String, String> hash = hashOps.entries(hashKey);
            if (!hash.isEmpty()) {
                // Esperamos keys como "fila:columna" y valor como estado
                for (Map.Entry<String, String> e : hash.entrySet()) {
                    String[] parts = e.getKey().split(":");
                    if (parts.length == 2) {
                        AsientoBloqueoEstadoDTO dto = new AsientoBloqueoEstadoDTO();
                        dto.setFila(Integer.parseInt(parts[0]));
                        dto.setColumna(Integer.parseInt(parts[1]));
                        dto.setEstado(e.getValue());
                        result.add(dto);
                    }
                }
                result.sort(Comparator.comparing(AsientoBloqueoEstadoDTO::getFila).thenComparing(AsientoBloqueoEstadoDTO::getColumna));
                return result;
            }
        } catch (Exception ex) {
            log.warn("No se pudo leer hash {}: {}", hashKey, ex.getMessage());
        }

        // Fallback: buscar claves individuales tipo evento:{id}:asiento:{fila}:{columna}
        String pattern = "evento:" + eventoId + ":asiento:*";
        try {
            Set<String> keys = redis.keys(pattern);
            for (String key : keys) {
                String[] parts = key.split(":");
                if (parts.length >= 5) {
                    Integer fila = Integer.parseInt(parts[3]);
                    Integer col = Integer.parseInt(parts[4]);
                    String estado = redis.opsForValue().get(key);
                    AsientoBloqueoEstadoDTO dto = new AsientoBloqueoEstadoDTO();
                    dto.setFila(fila);
                    dto.setColumna(col);
                    dto.setEstado(estado);
                    result.add(dto);
                }
            }
            result.sort(Comparator.comparing(AsientoBloqueoEstadoDTO::getFila).thenComparing(AsientoBloqueoEstadoDTO::getColumna));
        } catch (Exception ex) {
            log.error("Error consultando Redis con patrón {}: {}", pattern, ex.getMessage());
        }

        return result;
    }
}
