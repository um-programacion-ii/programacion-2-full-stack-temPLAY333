package um.prog2.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import um.prog2.dto.evento.consulta.EventoDetalleDTO;
import um.prog2.dto.evento.consulta.EventoResumenDTO;
import um.prog2.dto.evento.consulta.EventoDTO;
import um.prog2.dto.evento.shared.EventoTipoDTO;
import um.prog2.dto.evento.bloqueo.AsientoEstadoDTO;
import um.prog2.dto.evento.bloqueo.AsientoPosicionDTO;
import um.prog2.dto.evento.bloqueo.BloquearAsientosRequestDTO;
import um.prog2.dto.evento.bloqueo.BloquearAsientosResponseDTO;
import um.prog2.service.AsientoRedisService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventoProxyController.class)
class EventoProxyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WebClient webClient;

    @MockBean
    private WebClient.Builder webClientBuilder;

    @MockBean
    private AsientoRedisService asientoRedisService;


    @Test
    void obtenerEstadoAsientosDeberiaRetornarListaDesdeRedis() throws Exception {
        // Arrange
        Long eventoId = 1L;
        AsientoEstadoDTO asiento1 = new AsientoEstadoDTO();
        asiento1.setFila(1);
        asiento1.setColumna(5);
        asiento1.setEstado("DISPONIBLE");

        AsientoEstadoDTO asiento2 = new AsientoEstadoDTO();
        asiento2.setFila(2);
        asiento2.setColumna(10);
        asiento2.setEstado("BLOQUEADO");

        List<AsientoEstadoDTO> asientos = Arrays.asList(asiento1, asiento2);

        when(asientoRedisService.obtenerEstadoAsientos(eventoId)).thenReturn(asientos);

        // Act (async porque usa Mono.defer)
        MvcResult result = mockMvc.perform(get("/proxy/eventos/{id}/asientos-estado", eventoId))
                .andExpect(request().asyncStarted())
                .andReturn();

        // Assert
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fila").value(1))
                .andExpect(jsonPath("$[0].columna").value(5))
                .andExpect(jsonPath("$[0].estado").value("DISPONIBLE"))
                .andExpect(jsonPath("$[1].fila").value(2))
                .andExpect(jsonPath("$[1].columna").value(10))
                .andExpect(jsonPath("$[1].estado").value("BLOQUEADO"));

        verify(asientoRedisService, times(1)).obtenerEstadoAsientos(eventoId);
    }

    @Test
    void bloquearAsientosDeberiaMockearLlamadaYPublicarKafka() throws Exception {
        // Arrange
        BloquearAsientosRequestDTO request = new BloquearAsientosRequestDTO();
        request.setEventoId(1L);
        AsientoPosicionDTO pos = new AsientoPosicionDTO();
        pos.setFila(1);
        pos.setColumna(5);
        request.setAsientos(Arrays.asList(pos));

        BloquearAsientosResponseDTO response = new BloquearAsientosResponseDTO();
        response.setResultado(true);
        response.setEventoId(1L);

        // Mocks locales WebClient
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(BloquearAsientosResponseDTO.class)).thenReturn(Mono.just(response));

        // Act (async)
        MvcResult result = mockMvc.perform(post("/proxy/eventos/bloquear-asientos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(request().asyncStarted())
                .andReturn();

        // Assert
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value(true))
                .andExpect(jsonPath("$.eventoId").value(1));
    }

    @Test
    void obtenerEventoDeberiaMockearLlamadaExterna() throws Exception {
        // Arrange
        Long eventoId = 10L;
        EventoDetalleDTO evento = new EventoDetalleDTO();
        evento.setId(eventoId);
        evento.setTitulo("Concierto Rock");
        evento.setFecha(Instant.parse("2025-12-01T20:00:00Z"));

        // Mocks locales WebClient
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Object.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(EventoDetalleDTO.class)).thenReturn(Mono.just(evento));

        // Act (async)
        MvcResult result = mockMvc.perform(get("/proxy/eventos/{id}", eventoId))
                .andExpect(request().asyncStarted())
                .andReturn();

        // Assert
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventoId))
                .andExpect(jsonPath("$.titulo").value("Concierto Rock"));
    }

    @Test
    void listarEventosResumidosDeberiaMockearLlamadaExterna() throws Exception {
        // Arrange
        EventoTipoDTO eventoTipo = new EventoTipoDTO();
        eventoTipo.setId(1L);
        eventoTipo.setNombre("Conferencia");
        eventoTipo.setDescripcion("Conferencia");

        EventoResumenDTO evento1 = new EventoResumenDTO();
        evento1.setId(1L);
        evento1.setTitulo("Conferencia Nerd");
        evento1.setResumen("Esta es una conferencia de Nerds");
        evento1.setFecha(Instant.parse("2025-11-10T11:00:00Z"));
        evento1.setPrecioEntrada(new BigDecimal("2500.00"));
        evento1.setEventoTipo(eventoTipo);

        EventoResumenDTO evento2 = new EventoResumenDTO();
        evento2.setId(2L);
        evento2.setTitulo("Concierto Rock");
        evento2.setResumen("Un gran concierto de rock");
        evento2.setFecha(Instant.parse("2025-12-01T20:00:00Z"));
        evento2.setPrecioEntrada(new BigDecimal("5000.00"));
        evento2.setEventoTipo(eventoTipo);

        List<EventoResumenDTO> eventos = Arrays.asList(evento1, evento2);

        // Mocks WebClient
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(eventos));

        // Act (async)
        MvcResult result = mockMvc.perform(get("/proxy/eventos/resumidos"))
                .andExpect(request().asyncStarted())
                .andReturn();

        // Assert
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Conferencia Nerd"))
                .andExpect(jsonPath("$[0].resumen").value("Esta es una conferencia de Nerds"))
                .andExpect(jsonPath("$[0].precioEntrada").value(2500.00))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].titulo").value("Concierto Rock"))
                .andExpect(jsonPath("$[1].resumen").value("Un gran concierto de rock"))
                .andExpect(jsonPath("$[1].precioEntrada").value(5000.00));

        // Verificar que se llamó al WebClient con la URL correcta
        verify(webClient, times(1)).get();
        verify(requestHeadersUriSpec, times(1)).uri(contains("eventos-resumidos"));
    }

    @Test
    void listarEventosCompletosDeberiaMockearLlamadaExterna() throws Exception {
        // Arrange
        EventoTipoDTO eventoTipo = new EventoTipoDTO();
        eventoTipo.setId(1L);
        eventoTipo.setNombre("Conferencia");
        eventoTipo.setDescripcion("Conferencia");

        EventoDTO evento1 = new EventoDTO();
        evento1.setId(1L);
        evento1.setTitulo("Conferencia Nerd");
        evento1.setResumen("Esta es una conferencia de Nerds");
        evento1.setDescripcion("Esta es una conferencia de prueba completa");
        evento1.setFecha(Instant.parse("2025-11-10T11:00:00Z"));
        evento1.setDireccion("Mendoza 123, Montevideo");
        evento1.setImagen("https://ejemplo.com/imagen1.jpg");
        evento1.setFilaAsientos(10);
        evento1.setColumnAsientos(15);
        evento1.setPrecioEntrada(new BigDecimal("2500.00"));
        evento1.setEventoTipo(eventoTipo);

        EventoDTO evento2 = new EventoDTO();
        evento2.setId(2L);
        evento2.setTitulo("Concierto Rock");
        evento2.setResumen("Un gran concierto de rock");
        evento2.setDescripcion("El mejor concierto de rock del año");
        evento2.setFecha(Instant.parse("2025-12-01T20:00:00Z"));
        evento2.setDireccion("Estadio Nacional, Montevideo");
        evento2.setImagen("https://ejemplo.com/imagen2.jpg");
        evento2.setFilaAsientos(20);
        evento2.setColumnAsientos(25);
        evento2.setPrecioEntrada(new BigDecimal("5000.00"));
        evento2.setEventoTipo(eventoTipo);

        List<EventoDTO> eventos = Arrays.asList(evento1, evento2);

        // Mocks WebClient
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(eventos));

        // Act (async)
        MvcResult result = mockMvc.perform(get("/proxy/eventos"))
                .andExpect(request().asyncStarted())
                .andReturn();

        // Assert
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Conferencia Nerd"))
                .andExpect(jsonPath("$[0].direccion").value("Mendoza 123, Montevideo"))
                .andExpect(jsonPath("$[0].filaAsientos").value(10))
                .andExpect(jsonPath("$[0].columnAsientos").value(15))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].titulo").value("Concierto Rock"))
                .andExpect(jsonPath("$[1].direccion").value("Estadio Nacional, Montevideo"))
                .andExpect(jsonPath("$[1].filaAsientos").value(20))
                .andExpect(jsonPath("$[1].columnAsientos").value(25));

        // Verificar que se llamó al WebClient con la URL correcta
        verify(webClient, times(1)).get();
        verify(requestHeadersUriSpec, times(1)).uri(contains("eventos"));
    }
}
