package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.example.demo.IntegrationTest;
import com.example.demo.domain.Evento;
import com.example.demo.domain.EventoTipo;
import com.example.demo.domain.Integrante;
import com.example.demo.repository.EventoRepository;
import com.example.demo.repository.EventoTipoRepository;
import com.example.demo.repository.IntegranteRepository;
import com.example.demo.service.dto.EventoDTO;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * Test de integración para EventoSyncService.
 * Verifica que los eventos se sincronicen correctamente desde la Cátedra a la BD local.
 */
@IntegrationTest
class EventoSyncServiceIT {

    @Autowired
    private EventoSyncService eventoSyncService;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private EventoTipoRepository eventoTipoRepository;

    @Autowired
    private IntegranteRepository integranteRepository;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setup() {
        eventoRepository.deleteAll();
        eventoTipoRepository.deleteAll();
        integranteRepository.deleteAll();

        // Configurar mock server para simular respuestas del proxy
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    @Transactional
    void testSyncEventsFromCatedra_shouldSaveEventsToDatabase() {
        // Given: Mock de respuesta del proxy con eventos
        String mockResponse = """
            [
                {
                    "id": 1,
                    "titulo": "Concierto de Rock",
                    "resumen": "Gran concierto de rock",
                    "descripcion": "Descripción completa del evento",
                    "fecha": "2025-12-15T20:00:00",
                    "direccion": "Av. Siempre Viva 123",
                    "imagen": "http://example.com/imagen.jpg",
                    "filaAsientos": 10,
                    "columnAsientos": 20,
                    "precioEntrada": 5000.0,
                    "eventoTipo": {
                        "id": 1,
                        "nombre": "Concierto"
                    },
                    "integrantes": [
                        {
                            "id": 1,
                            "nombre": "Juan",
                            "apellido": "Pérez",
                            "identificacion": "12345678"
                        }
                    ]
                }
            ]
            """;

        mockServer
            .expect(requestTo("http://localhost:8080/api/proxy/eventos"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        // When: Sincronizar eventos
        eventoSyncService.syncEventsFromCatedra();

        // Then: Verificar que se guardaron en BD
        List<Evento> eventos = eventoRepository.findAll();
        assertThat(eventos).hasSize(1);

        Evento evento = eventos.get(0);
        assertThat(evento.getTitulo()).isEqualTo("Concierto de Rock");
        assertThat(evento.getResumen()).isEqualTo("Gran concierto de rock");
        assertThat(evento.getPrecioEntrada()).isEqualTo(new BigDecimal("5000.0"));
        assertThat(evento.getEventoTipo()).isNotNull();
        assertThat(evento.getEventoTipo().getNombre()).isEqualTo("Concierto");
        assertThat(evento.getIntegrantes()).hasSize(1);

        // Verificar que EventoTipo se guardó
        List<EventoTipo> eventoTipos = eventoTipoRepository.findAll();
        assertThat(eventoTipos).hasSize(1);

        // Verificar que Integrante se guardó
        List<Integrante> integrantes = integranteRepository.findAll();
        assertThat(integrantes).hasSize(1);
        assertThat(integrantes.get(0).getNombre()).isEqualTo("Juan");
        assertThat(integrantes.get(0).getApellido()).isEqualTo("Pérez");

        mockServer.verify();
    }

    @Test
    @Transactional
    void testSyncEventsFromCatedra_shouldUpdateExistingEvents() {
        // Given: Un evento ya existe en BD
        EventoTipo eventoTipo = new EventoTipo();
        eventoTipo.setId(1L);
        eventoTipo.setNombre("Concierto");
        eventoTipoRepository.save(eventoTipo);

        Evento existingEvento = new Evento();
        existingEvento.setId(1L);
        existingEvento.setTitulo("Título Viejo");
        existingEvento.setEventoTipo(eventoTipo);
        eventoRepository.save(existingEvento);

        // Mock de respuesta con datos actualizados
        String mockResponse = """
            [
                {
                    "id": 1,
                    "titulo": "Título Actualizado",
                    "resumen": "Resumen actualizado",
                    "descripcion": "Descripción actualizada",
                    "fecha": "2025-12-15T20:00:00",
                    "direccion": "Dirección actualizada",
                    "imagen": "http://example.com/nueva-imagen.jpg",
                    "filaAsientos": 15,
                    "columnAsientos": 25,
                    "precioEntrada": 6000.0,
                    "eventoTipo": {
                        "id": 1,
                        "nombre": "Concierto"
                    },
                    "integrantes": []
                }
            ]
            """;

        mockServer
            .expect(requestTo("http://localhost:8080/api/proxy/eventos"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        // When: Sincronizar eventos
        eventoSyncService.syncEventsFromCatedra();

        // Then: Verificar que se actualizó
        List<Evento> eventos = eventoRepository.findAll();
        assertThat(eventos).hasSize(1);

        Evento evento = eventos.get(0);
        assertThat(evento.getId()).isEqualTo(1L);
        assertThat(evento.getTitulo()).isEqualTo("Título Actualizado");
        assertThat(evento.getResumen()).isEqualTo("Resumen actualizado");
        assertThat(evento.getPrecioEntrada()).isEqualTo(new BigDecimal("6000.0"));

        mockServer.verify();
    }

    @Test
    @Transactional
    void testObtenerEventosResumidos_shouldReturnEventsFromLocalDatabase() {
        // Given: Eventos en la BD local
        EventoTipo eventoTipo = new EventoTipo();
        eventoTipo.setNombre("Teatro");
        eventoTipoRepository.save(eventoTipo);

        Evento evento1 = new Evento();
        evento1.setTitulo("Evento 1");
        evento1.setResumen("Resumen 1");
        evento1.setPrecioEntrada(new BigDecimal("1000.0"));
        evento1.setEventoTipo(eventoTipo);
        eventoRepository.save(evento1);

        Evento evento2 = new Evento();
        evento2.setTitulo("Evento 2");
        evento2.setResumen("Resumen 2");
        evento2.setPrecioEntrada(new BigDecimal("2000.0"));
        evento2.setEventoTipo(eventoTipo);
        eventoRepository.save(evento2);

        // When: Obtener eventos resumidos
        var eventosResumidos = eventoSyncService.obtenerEventosResumidos();

        // Then: Verificar resultados
        assertThat(eventosResumidos).hasSize(2);
        assertThat(eventosResumidos).extracting("titulo").containsExactlyInAnyOrder("Evento 1", "Evento 2");
    }

    @Test
    @Transactional
    void testObtenerEventosCompletos_shouldReturnCompleteEventsFromLocalDatabase() {
        // Given: Evento completo en BD local
        EventoTipo eventoTipo = new EventoTipo();
        eventoTipo.setNombre("Deportes");
        eventoTipoRepository.save(eventoTipo);

        Integrante integrante = new Integrante();
        integrante.setNombre("Carlos");
        integrante.setApellido("López");
        integrante.setIdentificacion("87654321");
        integranteRepository.save(integrante);

        Evento evento = new Evento();
        evento.setTitulo("Partido de Fútbol");
        evento.setResumen("Gran partido");
        evento.setDescripcion("Descripción completa");
        evento.setPrecioEntrada(new BigDecimal("3000.0"));
        evento.setEventoTipo(eventoTipo);
        evento.getIntegrantes().add(integrante);
        eventoRepository.save(evento);

        // When: Obtener eventos completos
        List<EventoDTO> eventosCompletos = eventoSyncService.obtenerEventosCompletos();

        // Then: Verificar resultados
        assertThat(eventosCompletos).hasSize(1);
        EventoDTO eventoDTO = eventosCompletos.get(0);
        assertThat(eventoDTO.getTitulo()).isEqualTo("Partido de Fútbol");
        assertThat(eventoDTO.getEventoTipo()).isNotNull();
        assertThat(eventoDTO.getIntegrantes()).hasSize(1);
    }

    @Test
    @Transactional
    void testObtenerEventoPorId_shouldReturnEventFromLocalDatabase() {
        // Given: Evento en BD local
        Evento evento = new Evento();
        evento.setTitulo("Evento Único");
        evento.setResumen("Resumen único");
        evento.setPrecioEntrada(new BigDecimal("1500.0"));
        Evento savedEvento = eventoRepository.save(evento);

        // When: Obtener evento por ID
        var eventoOpt = eventoSyncService.obtenerEventoPorId(savedEvento.getId());

        // Then: Verificar resultado
        assertThat(eventoOpt).isPresent();
        assertThat(eventoOpt.orElseThrow().getTitulo()).isEqualTo("Evento Único");
    }
}

