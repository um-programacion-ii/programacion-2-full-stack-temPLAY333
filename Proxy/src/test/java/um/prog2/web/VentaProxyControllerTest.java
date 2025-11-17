package um.prog2.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import um.prog2.dto.venta.RealizarVentaRequestDTO;
import um.prog2.dto.venta.RealizarVentaResponseDTO;
import um.prog2.dto.venta.AsientoVentaDTO;
import um.prog2.dto.consultaventas.VentaResumenDTO;
import um.prog2.dto.consultaventas.VentaDetalleDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VentaProxyController.class)
class VentaProxyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WebClient webClient;

    @MockBean
    private WebClient.Builder webClientBuilder;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void realizarVentaDeberiaMockearLlamadaYPublicarKafka() throws Exception {
        // Arrange
        RealizarVentaRequestDTO request = new RealizarVentaRequestDTO();
        request.setEventoId(5L);
        request.setFecha(Instant.parse("2025-12-01T00:00:00Z"));
        request.setPrecioVenta(BigDecimal.valueOf(150.00));

        AsientoVentaDTO asiento = new AsientoVentaDTO();
        asiento.setFila(3);
        asiento.setColumna(7);
        asiento.setPersona("Juan Perez");
        request.setAsientos(Arrays.asList(asiento));

        RealizarVentaResponseDTO response = new RealizarVentaResponseDTO();
        response.setResultado(true);
        response.setEventoId(5L);
        response.setVentaId(100L);
        response.setFechaVenta(Instant.parse("2025-12-01T10:30:00Z"));
        response.setPrecioVenta(BigDecimal.valueOf(150.00));

        // Mocks locales de la cadena de WebClient
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(RealizarVentaResponseDTO.class)).thenReturn(Mono.just(response));

        // Act (async)
        MvcResult result = mockMvc.perform(post("/proxy/ventas/realizar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(request().asyncStarted())
                .andReturn();

        // Assert
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value(true))
                .andExpect(jsonPath("$.eventoId").value(5))
                .andExpect(jsonPath("$.ventaId").value(100));

        // Verificar que se intentó publicar en Kafka
        verify(kafkaTemplate, atLeastOnce()).send(anyString(), anyString());
    }

    @Test
    void listarVentasDeberiaMockearLlamadaExterna() throws Exception {
        // Arrange
        VentaResumenDTO venta1 = new VentaResumenDTO();
        venta1.setVentaId(1L);
        venta1.setEventoId(10L);
        venta1.setResultado(true);

        VentaResumenDTO venta2 = new VentaResumenDTO();
        venta2.setVentaId(2L);
        venta2.setEventoId(11L);
        venta2.setResultado(true);

        List<VentaResumenDTO> ventas = Arrays.asList(venta1, venta2);

        // Mocks locales de la cadena de WebClient
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(org.springframework.core.ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(ventas));

        // Act (async)
        MvcResult result = mockMvc.perform(get("/proxy/ventas"))
                .andExpect(request().asyncStarted())
                .andReturn();

        // Assert
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ventaId").value(1))
                .andExpect(jsonPath("$[0].eventoId").value(10))
                .andExpect(jsonPath("$[1].ventaId").value(2))
                .andExpect(jsonPath("$[1].eventoId").value(11));
    }

    @Test
    void obtenerVentaDeberiaMockearLlamadaExterna() throws Exception {
        // Arrange
        Long ventaId = 50L;
        VentaDetalleDTO venta = new VentaDetalleDTO();
        venta.setVentaId(ventaId);
        venta.setResultado(true);
        venta.setEventoId(20L);
        venta.setFechaVenta(Instant.parse("2025-11-15T15:45:00Z"));

        // Mocks locales de la cadena de WebClient
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Object.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(VentaDetalleDTO.class)).thenReturn(Mono.just(venta));

        // Act (async)
        MvcResult result = mockMvc.perform(get("/proxy/ventas/{id}", ventaId))
                .andExpect(request().asyncStarted())
                .andReturn();

        // Assert
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ventaId").value(ventaId))
                .andExpect(jsonPath("$.eventoId").value(20))
                .andExpect(jsonPath("$.resultado").value(true));
    }

    @Test
    void realizarVentaDeberiaManejarErroresDeServicioExterno() throws Exception {
        // Arrange
        RealizarVentaRequestDTO request = new RealizarVentaRequestDTO();
        request.setEventoId(99L);
        request.setFecha(Instant.parse("2025-12-01T00:00:00Z"));
        request.setPrecioVenta(BigDecimal.valueOf(200.00));

        AsientoVentaDTO asiento = new AsientoVentaDTO();
        asiento.setFila(1);
        asiento.setColumna(1);
        asiento.setPersona("X");
        request.setAsientos(Arrays.asList(asiento));

        // Mocks locales de la cadena de WebClient
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(RealizarVentaResponseDTO.class))
                .thenReturn(Mono.error(new RuntimeException("Servicio no disponible")));

        // Act (async)
        MvcResult result = mockMvc.perform(post("/proxy/ventas/realizar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(request().asyncStarted())
                .andReturn();

        // Assert
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().is5xxServerError());
    }
}
