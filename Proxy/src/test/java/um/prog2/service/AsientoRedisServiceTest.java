package um.prog2.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import um.prog2.dto.evento.asientos.AsientoBloqueoEstadoDTO;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsientoRedisServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    @SuppressWarnings("unchecked")
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private AsientoRedisService asientoRedisService;

    @BeforeEach
    void setUp() {
        asientoRedisService = new AsientoRedisService(redisTemplate);
    }

    @Test
    void obtenerEstadoAsientosDesdeHashDeberiaRetornarListaOrdenada() {
        // Arrange
        Long eventoId = 1L;
        String hashKey = "evento:1:asientos";

        Map<Object, Object> hash = new HashMap<>();
        hash.put("1:5", "DISPONIBLE");
        hash.put("2:10", "BLOQUEADO");
        hash.put("1:3", "DISPONIBLE");

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(hashKey)).thenReturn(hash);

        // Act
        List<AsientoBloqueoEstadoDTO> result = asientoRedisService.obtenerEstadoAsientos(eventoId);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());

        // Verificar orden: primero fila 1, luego fila 2
        assertEquals(1, result.get(0).getFila());
        assertEquals(3, result.get(0).getColumna());
        assertEquals("DISPONIBLE", result.get(0).getEstado());

        assertEquals(1, result.get(1).getFila());
        assertEquals(5, result.get(1).getColumna());
        assertEquals("DISPONIBLE", result.get(1).getEstado());

        assertEquals(2, result.get(2).getFila());
        assertEquals(10, result.get(2).getColumna());
        assertEquals("BLOQUEADO", result.get(2).getEstado());

        verify(hashOperations, times(1)).entries(hashKey);
    }

    @Test
    void obtenerEstadoAsientosDesdeKeysIndividualesComoFallback() {
        // Arrange
        Long eventoId = 2L;
        String hashKey = "evento:2:asientos";
        String pattern = "evento:2:asiento:*";

        // Hash vacío, activará fallback
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(hashKey)).thenReturn(new HashMap<>());

        Set<String> keys = new HashSet<>(Arrays.asList(
                "evento:2:asiento:1:5",
                "evento:2:asiento:3:2"
        ));

        when(redisTemplate.keys(pattern)).thenReturn(keys);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("evento:2:asiento:1:5")).thenReturn("DISPONIBLE");
        when(valueOperations.get("evento:2:asiento:3:2")).thenReturn("VENDIDO");

        // Act
        List<AsientoBloqueoEstadoDTO> result = asientoRedisService.obtenerEstadoAsientos(eventoId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        verify(redisTemplate, times(1)).keys(pattern);
        verify(valueOperations, times(2)).get(anyString());
    }

    @Test
    void obtenerEstadoAsientosDeberiaRetornarListaVaciaCuandoNoHayDatos() {
        // Arrange
        Long eventoId = 99L;
        String hashKey = "evento:99:asientos";
        String pattern = "evento:99:asiento:*";

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(hashKey)).thenReturn(new HashMap<>());
        when(redisTemplate.keys(pattern)).thenReturn(new HashSet<>());

        // Act
        List<AsientoBloqueoEstadoDTO> result = asientoRedisService.obtenerEstadoAsientos(eventoId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void obtenerEstadoAsientosDeberiaManejarExcepcionYUsarFallback() {
        // Arrange
        Long eventoId = 3L;
        String hashKey = "evento:3:asientos";
        String pattern = "evento:3:asiento:*";

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(hashKey)).thenThrow(new RuntimeException("Redis connection error"));

        Set<String> keys = new HashSet<>(Arrays.asList("evento:3:asiento:1:1"));
        when(redisTemplate.keys(pattern)).thenReturn(keys);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("evento:3:asiento:1:1")).thenReturn("DISPONIBLE");

        // Act
        List<AsientoBloqueoEstadoDTO> result = asientoRedisService.obtenerEstadoAsientos(eventoId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getFila());
        assertEquals(1, result.get(0).getColumna());
    }
}
