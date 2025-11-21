package um.prog2.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import um.prog2.dto.evento.bloqueo.AsientoEstadoDTO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AsientoRedisService {

    private static final Logger log = LoggerFactory.getLogger(AsientoRedisService.class);

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public AsientoRedisService(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    /**
     * Lee Redis de la cátedra según el formato documentado:
     *  key: "evento_" + eventoId
     *  value: JSON con estructura {"eventoId":1,"asientos":[{"fila":1,"columna":3,"estado":"Bloqueado",...}, ...]}
     * Solo se guardan asientos BLOQUEADOS o VENDIDOS, el resto se considera disponible.
     */
    public List<AsientoEstadoDTO> obtenerEstadoAsientos(Long eventoId) {
        String key = "evento_" + eventoId;
        List<AsientoEstadoDTO> result = new ArrayList<>();

        try {
            String json = redis.opsForValue().get(key);
            if (json == null || json.isBlank()) {
                log.debug("No hay datos en Redis para la key {} (ningún asiento bloqueado/vendido)", key);
                return result; // Lista vacía => todos disponibles
            }

            JsonNode root = objectMapper.readTree(json);
            JsonNode asientosNode = root.get("asientos");
            if (asientosNode != null && asientosNode.isArray()) {
                for (JsonNode asientoNode : asientosNode) {
                    AsientoEstadoDTO dto = new AsientoEstadoDTO();
                    dto.setFila(asientoNode.path("fila").asInt());
                    dto.setColumna(asientoNode.path("columna").asInt());
                    dto.setEstado(asientoNode.path("estado").asText(null));
                    // el campo expira puede existir o no; aquí no lo mapeamos porque tu DTO no lo tiene
                    result.add(dto);
                }
                result.sort(Comparator.comparing(AsientoEstadoDTO::getFila)
                                       .thenComparing(AsientoEstadoDTO::getColumna));
            }
        } catch (Exception ex) {
            log.error("Error leyendo Redis para evento {} y key {}: {}", eventoId, key, ex.getMessage());
        }

        return result;
    }
}
