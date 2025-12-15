package com.example.demo.web.rest;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.demo.IntegrationTest;
import com.example.demo.domain.Evento;
import com.example.demo.domain.EventoTipo;
import com.example.demo.domain.Integrante;
import com.example.demo.repository.EventoRepository;
import com.example.demo.repository.EventoTipoRepository;
import com.example.demo.repository.IntegranteRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link EventoMobileResource} REST controller.
 * Usa la base de datos de desarrollo para verificar los endpoints móvil.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class EventoMobileResourceIT {

    private static final String MOBILE_API_URL = "/api/mobile/eventos";
    private static final String MOBILE_API_URL_RESUMIDOS = MOBILE_API_URL + "/resumidos";
    private static final String MOBILE_API_URL_ID = MOBILE_API_URL + "/{id}";
    private static final String MOBILE_API_URL_SYNC = MOBILE_API_URL + "/sync";
    private static final String MOBILE_API_URL_SYNC_ID = MOBILE_API_URL + "/{id}/sync";

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private EventoTipoRepository eventoTipoRepository;

    @Autowired
    private IntegranteRepository integranteRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMobileMockMvc;


    private Evento evento1;
    private Evento evento2;
    private EventoTipo eventoTipo;

    @BeforeEach
    void setUp() {
        // Limpiar datos previos
        eventoRepository.deleteAll();
        integranteRepository.deleteAll();
        eventoTipoRepository.deleteAll();
        em.flush();
        em.clear();

        // Crear EventoTipo
        eventoTipo = new EventoTipo();
        eventoTipo.setNombre("Concierto");
        eventoTipo.setDescripcion("Evento musical");
        eventoTipoRepository.saveAndFlush(eventoTipo);

        // Crear Integrantes
        Integrante integrante1 = new Integrante();
        integrante1.setNombre("Carlos");
        integrante1.setApellido("Pérez");
        integrante1.setIdentificacion("Guitarrista");
        integranteRepository.saveAndFlush(integrante1);

        Integrante integrante2 = new Integrante();
        integrante2.setNombre("Ana");
        integrante2.setApellido("García");
        integrante2.setIdentificacion("Vocalista");
        integranteRepository.saveAndFlush(integrante2);

        Set<Integrante> integrantes = new HashSet<>();
        integrantes.add(integrante1);
        integrantes.add(integrante2);

        // Crear Eventos de prueba
        evento1 = createEvento("Concierto de Rock", "Evento musical increíble", new BigDecimal("1500.00"), integrantes);
        evento2 = createEvento("Festival de Jazz", "Festival anual de jazz", new BigDecimal("2000.00"), integrantes);

        eventoRepository.saveAndFlush(evento1);
        eventoRepository.saveAndFlush(evento2);

        em.clear();
    }

    @Test
    @Transactional
    void testGetAllEventosResumidos_shouldReturnPaginatedList() throws Exception {
        // When & Then: Obtener eventos resumidos
        restMobileMockMvc
            .perform(get(MOBILE_API_URL_RESUMIDOS).param("page", "0").param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[*].titulo").value(hasItem("Concierto de Rock")))
            .andExpect(jsonPath("$[*].titulo").value(hasItem("Festival de Jazz")));
    }

    @Test
    @Transactional
    void testGetAllEventos_shouldReturnPaginatedListWithAllData() throws Exception {
        // When & Then: Obtener eventos completos
        restMobileMockMvc
            .perform(get(MOBILE_API_URL).param("page", "0").param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[*].titulo").value(hasItem("Concierto de Rock")))
            .andExpect(jsonPath("$[*].descripcion").value(hasItem("Evento musical increíble con las mejores bandas")))
            .andExpect(jsonPath("$[*].eventoTipo.nombre").value(hasItem("Concierto")));
    }

    @Test
    @Transactional
    void testGetEvento_shouldReturnEventoById() throws Exception {
        // When & Then: Obtener un evento específico
        restMobileMockMvc
            .perform(get(MOBILE_API_URL_ID, evento1.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(evento1.getId()))
            .andExpect(jsonPath("$.titulo").value("Concierto de Rock"))
            .andExpect(jsonPath("$.resumen").value("Evento musical increíble"))
            .andExpect(jsonPath("$.direccion").value("Estadio Nacional"))
            .andExpect(jsonPath("$.precioEntrada").value(1500.00))
            .andExpect(jsonPath("$.filaAsientos").value(10))
            .andExpect(jsonPath("$.columnAsientos").value(20))
            .andExpect(jsonPath("$.eventoTipo.nombre").value("Concierto"));
    }

    @Test
    @Transactional
    void testGetEvento_shouldReturn404WhenNotFound() throws Exception {
        // When & Then: Buscar evento inexistente
        restMobileMockMvc.perform(get(MOBILE_API_URL_ID, 99999L)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void testGetAllEventos_shouldSupportPagination() throws Exception {
        // Given: Crear más eventos para probar paginación
        for (int i = 3; i <= 15; i++) {
            Evento evento = createEvento("Evento " + i, "Descripción " + i, new BigDecimal("1000.00"), new HashSet<>());
            eventoRepository.saveAndFlush(evento);
        }
        em.clear();

        // When & Then: Primera página
        restMobileMockMvc
            .perform(get(MOBILE_API_URL).param("page", "0").param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(5)))
            .andExpect(header().exists("X-Total-Count"));

        // When & Then: Segunda página
        restMobileMockMvc
            .perform(get(MOBILE_API_URL).param("page", "1").param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(5)));
    }

    @Test
    @Transactional
    void testGetAllEventosResumidos_shouldReturnEmptyListWhenNoEvents() throws Exception {
        // Given: No hay eventos
        eventoRepository.deleteAll();
        em.flush();

        // When & Then: La lista debe estar vacía
        restMobileMockMvc
            .perform(get(MOBILE_API_URL_RESUMIDOS).param("page", "0").param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @Transactional
    void testSyncEventos_shouldReturnAccepted() throws Exception {
        // When & Then: Forzar sincronización manual
        restMobileMockMvc.perform(post(MOBILE_API_URL_SYNC)).andExpect(status().isAccepted());
    }

    @Test
    @Transactional
    void testSyncEvento_shouldReturnAccepted() throws Exception {
        // When & Then: Forzar sincronización de un evento específico
        restMobileMockMvc.perform(post(MOBILE_API_URL_SYNC_ID, evento1.getId())).andExpect(status().isAccepted());
    }

    @Test
    @Transactional
    void testGetAllEventos_shouldIncludeIntegrantes() throws Exception {
        // When & Then: Los eventos deben incluir sus integrantes
        restMobileMockMvc
            .perform(get(MOBILE_API_URL).param("page", "0").param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].integrantes").isArray())
            .andExpect(jsonPath("$[0].integrantes", hasSize(2)))
            .andExpect(jsonPath("$[0].integrantes[*].nombre").value(hasItem("Carlos")))
            .andExpect(jsonPath("$[0].integrantes[*].apellido").value(hasItem("Pérez")));
    }

    @Test
    @Transactional
    void testGetEvento_shouldIncludeAllRelations() throws Exception {
        // When & Then: El evento debe incluir todas sus relaciones
        restMobileMockMvc
            .perform(get(MOBILE_API_URL_ID, evento1.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.eventoTipo").exists())
            .andExpect(jsonPath("$.eventoTipo.id").exists())
            .andExpect(jsonPath("$.eventoTipo.nombre").value("Concierto"))
            .andExpect(jsonPath("$.integrantes").isArray())
            .andExpect(jsonPath("$.integrantes", hasSize(2)));
    }

    @Test
    @Transactional
    void testGetAllEventosResumidos_shouldOrderEventsByFecha() throws Exception {
        // Given: Crear eventos con fechas diferentes
        eventoRepository.deleteAll();
        em.flush();

        Evento eventoFuturo = createEvento("Evento Futuro", "En 60 días", new BigDecimal("1000.00"), new HashSet<>());
        eventoFuturo.setFecha(Instant.now().plus(60, ChronoUnit.DAYS));
        eventoRepository.saveAndFlush(eventoFuturo);

        Evento eventoCercano = createEvento("Evento Cercano", "En 10 días", new BigDecimal("1000.00"), new HashSet<>());
        eventoCercano.setFecha(Instant.now().plus(10, ChronoUnit.DAYS));
        eventoRepository.saveAndFlush(eventoCercano);

        em.clear();

        // When & Then: Los eventos deben estar ordenados
        restMobileMockMvc
            .perform(get(MOBILE_API_URL_RESUMIDOS).param("page", "0").param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].titulo").value("Evento Cercano")) // Primero el más cercano
            .andExpect(jsonPath("$[1].titulo").value("Evento Futuro"));
    }

    @Test
    @Transactional
    void testGetAllEventos_shouldReturnCorrectPrecioEntrada() throws Exception {
        // When & Then: Los precios deben ser correctos
        restMobileMockMvc
            .perform(get(MOBILE_API_URL).param("page", "0").param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.titulo=='Concierto de Rock')].precioEntrada").value(hasItem(1500.00)))
            .andExpect(jsonPath("$[?(@.titulo=='Festival de Jazz')].precioEntrada").value(hasItem(2000.00)));
    }

    @Test
    @Transactional
    void testGetEvento_shouldReturnCorrectAsientosConfiguration() throws Exception {
        // When & Then: La configuración de asientos debe ser correcta
        restMobileMockMvc
            .perform(get(MOBILE_API_URL_ID, evento1.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.filaAsientos").value(10))
            .andExpect(jsonPath("$.columnAsientos").value(20));
    }

    // ===================================================================
    // Helper methods
    // ===================================================================

    private Evento createEvento(String titulo, String resumen, BigDecimal precio, Set<Integrante> integrantes) {
        Evento evento = new Evento();
        evento.setTitulo(titulo);
        evento.setResumen(resumen);
        evento.setDescripcion(resumen + " con las mejores bandas");
        evento.setFecha(Instant.now().plus(30, ChronoUnit.DAYS));
        evento.setDireccion("Estadio Nacional");
        evento.setImagen("https://example.com/imagen.jpg");
        evento.setFilaAsientos(10);
        evento.setColumnAsientos(20);
        evento.setPrecioEntrada(precio);
        evento.setEventoTipo(eventoTipo);
        evento.setIntegrantes(integrantes);
        return evento;
    }
}

