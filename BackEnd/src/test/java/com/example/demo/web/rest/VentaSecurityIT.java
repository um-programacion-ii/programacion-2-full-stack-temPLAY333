package com.example.demo.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.demo.IntegrationTest;
import com.example.demo.domain.Evento;
import com.example.demo.domain.EventoTipo;
import com.example.demo.domain.User;
import com.example.demo.domain.Venta;
import com.example.demo.repository.EventoRepository;
import com.example.demo.repository.EventoTipoRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VentaRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests de seguridad para endpoints de Venta.
 * Verifica que los usuarios solo puedan acceder a sus propias ventas.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class VentaSecurityIT {

    private static final String ENTITY_API_URL = "/api/ventas";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private EventoTipoRepository eventoTipoRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restVentaMockMvc;

    private User usuario1;
    private User usuario2;
    private Evento evento;
    private Venta ventaUsuario1;
    private Venta ventaUsuario2;

    @BeforeEach
    void initTest() {
        // Crear usuarios
        usuario1 = new User();
        usuario1.setLogin("usuario1");
        usuario1.setPassword("$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K"); // password
        usuario1.setActivated(true);
        usuario1.setEmail("usuario1@localhost");
        usuario1.setFirstName("Usuario");
        usuario1.setLastName("Uno");
        usuario1.setLangKey("es");
        usuario1 = userRepository.saveAndFlush(usuario1);

        usuario2 = new User();
        usuario2.setLogin("usuario2");
        usuario2.setPassword("$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K"); // password
        usuario2.setActivated(true);
        usuario2.setEmail("usuario2@localhost");
        usuario2.setFirstName("Usuario");
        usuario2.setLastName("Dos");
        usuario2.setLangKey("es");
        usuario2 = userRepository.saveAndFlush(usuario2);

        // Crear tipo de evento
        EventoTipo eventoTipo = new EventoTipo();
        eventoTipo.setNombre("Concierto");
        eventoTipo = eventoTipoRepository.saveAndFlush(eventoTipo);

        // Crear evento
        evento = new Evento();
        evento.setTitulo("Evento Test");
        evento.setResumen("Descripción corta");
        evento.setDescripcion("Descripción larga del evento");
        evento.setFecha(Instant.now());
        evento.setDireccion("Teatro Municipal");
        evento.setFilaAsientos(10);
        evento.setColumnAsientos(10);
        evento.setPrecioEntrada(new BigDecimal("100.00"));
        evento.setEventoTipo(eventoTipo);
        evento = eventoRepository.saveAndFlush(evento);

        // Crear venta para usuario1
        ventaUsuario1 = new Venta();
        ventaUsuario1.setVentaId(1L);
        ventaUsuario1.setFechaVenta(Instant.now());
        ventaUsuario1.setResultado(true);
        ventaUsuario1.setDescripcion("Venta exitosa");
        ventaUsuario1.setPrecioVenta(new BigDecimal("400.00"));
        ventaUsuario1.setUsuario(usuario1);
        ventaUsuario1.setEvento(evento);
        ventaUsuario1 = ventaRepository.saveAndFlush(ventaUsuario1);

        // Crear venta para usuario2
        ventaUsuario2 = new Venta();
        ventaUsuario2.setVentaId(2L);
        ventaUsuario2.setFechaVenta(Instant.now());
        ventaUsuario2.setResultado(true);
        ventaUsuario2.setDescripcion("Venta exitosa");
        ventaUsuario2.setPrecioVenta(new BigDecimal("200.00"));
        ventaUsuario2.setUsuario(usuario2);
        ventaUsuario2.setEvento(evento);
        ventaUsuario2 = ventaRepository.saveAndFlush(ventaUsuario2);
    }

    @Test
    @Transactional
    @WithMockUser(username = "usuario1")
    void getAllVentas_shouldReturnOnlyCurrentUserVentas() throws Exception {
        // Usuario1 solo debe ver su propia venta
        restVentaMockMvc
            .perform(get(ENTITY_API_URL))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(ventaUsuario1.getId().intValue()))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @Transactional
    @WithMockUser(username = "usuario2")
    void getAllVentas_shouldReturnOnlyUser2Ventas() throws Exception {
        // Usuario2 solo debe ver su propia venta
        restVentaMockMvc
            .perform(get(ENTITY_API_URL))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(ventaUsuario2.getId().intValue()))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @Transactional
    @WithMockUser(username = "usuario1")
    void getVenta_shouldReturnOwnVenta() throws Exception {
        // Usuario1 puede acceder a su propia venta
        restVentaMockMvc
            .perform(get(ENTITY_API_URL_ID, ventaUsuario1.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(ventaUsuario1.getId().intValue()))
            .andExpect(jsonPath("$.precioVenta").value(400.00));
    }

    @Test
    @Transactional
    @WithMockUser(username = "usuario1")
    void getVenta_shouldNotReturnOtherUserVenta() throws Exception {
        // Usuario1 NO puede acceder a la venta de Usuario2
        restVentaMockMvc
            .perform(get(ENTITY_API_URL_ID, ventaUsuario2.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @WithMockUser(username = "usuario2")
    void getVenta_shouldNotAccessUser1Venta() throws Exception {
        // Usuario2 NO puede acceder a la venta de Usuario1
        restVentaMockMvc
            .perform(get(ENTITY_API_URL_ID, ventaUsuario1.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @WithMockUser(username = "usuario2")
    void getVenta_shouldReturnOwnVentaForUser2() throws Exception {
        // Usuario2 puede acceder a su propia venta
        restVentaMockMvc
            .perform(get(ENTITY_API_URL_ID, ventaUsuario2.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(ventaUsuario2.getId().intValue()))
            .andExpect(jsonPath("$.precioVenta").value(200.00));
    }

    @Test
    @Transactional
    void getAllVentas_shouldRequireAuthentication() throws Exception {
        // Sin autenticación, debe ser rechazado
        restVentaMockMvc
            .perform(get(ENTITY_API_URL))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    void getVenta_shouldRequireAuthentication() throws Exception {
        // Sin autenticación, debe ser rechazado
        restVentaMockMvc
            .perform(get(ENTITY_API_URL_ID, ventaUsuario1.getId()))
            .andExpect(status().isUnauthorized());
    }
}

