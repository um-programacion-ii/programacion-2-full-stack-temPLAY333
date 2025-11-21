package um.prog2.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import um.prog2.dto.evento.bloqueo.AsientoEstadoDTO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsientoRedisServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private AsientoRedisService asientoRedisService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        asientoRedisService = new AsientoRedisService(redisTemplate, objectMapper);
    }

    @Test
    void obtenerEstadoAsientosDeberiaParsearJsonDeRedis() {
        // Arrange
        Long eventoId = 1L;
        String key = "evento_" + eventoId;
        String json = "{\"eventoId\":1,\"asientos\":[{" +
                "\"fila\":1,\"columna\":3,\"estado\":\"Bloqueado\"},{" +
                "\"fila\":2,\"columna\":4,\"estado\":\"Vendido\"}]}";

        when(valueOperations.get(key)).thenReturn(json);

        // Act
        List<AsientoEstadoDTO> result = asientoRedisService.obtenerEstadoAsientos(eventoId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getFila());
        assertEquals(3, result.get(0).getColumna());
        assertEquals("Bloqueado", result.get(0).getEstado());
        assertEquals(2, result.get(1).getFila());
        assertEquals(4, result.get(1).getColumna());
        assertEquals("Vendido", result.get(1).getEstado());

        verify(valueOperations, times(1)).get(key);
    }

    @Test
    void obtenerEstadoAsientosDeberiaRetornarListaVaciaCuandoNoHayKey() {
        // Arrange
        Long eventoId = 99L;
        String key = "evento_" + eventoId;

        when(valueOperations.get(key)).thenReturn(null);

        // Act
        List<AsientoEstadoDTO> result = asientoRedisService.obtenerEstadoAsientos(eventoId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(valueOperations, times(1)).get(key);
    }

    @Test
    void obtenerEstadoAsientosDeberiaManejarJsonInvalido() {
        // Arrange
        Long eventoId = 2L;
        String key = "evento_" + eventoId;

        when(valueOperations.get(key)).thenReturn("no-es-json");

        // Act
        List<AsientoEstadoDTO> result = asientoRedisService.obtenerEstadoAsientos(eventoId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(valueOperations, times(1)).get(key);
    }
}
