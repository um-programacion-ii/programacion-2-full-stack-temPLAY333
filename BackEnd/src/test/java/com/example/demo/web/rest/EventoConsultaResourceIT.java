package com.example.demo.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.IntegrationTest;
import com.example.demo.domain.Evento;
import com.example.demo.domain.EventoTipo;
import com.example.demo.repository.EventoRepository;
import com.example.demo.service.dto.EventoDTO;
import com.example.demo.service.mapper.EventoMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class EventoConsultaResourceIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private EventoMapper eventoMapper;

    @Autowired
    private EntityManager em;

    private Evento evento;

    @BeforeEach
    void initTest() {
        eventoRepository.deleteAll();
        evento = new Evento()
            .titulo("Titulo")
            .resumen("Resumen")
            .descripcion("Desc")
            .fecha(Instant.now())
            .direccion("Dir")
            .imagen("Img")
            .filaAsientos(10)
            .columnAsientos(20)
            .precioEntrada(BigDecimal.TEN);

        EventoTipo tipo = new EventoTipo().nombre("Tipo").descripcion("Desc");
        em.persist(tipo);
        em.flush();
        evento.setEventoTipo(tipo);
        eventoRepository.saveAndFlush(evento);
    }

    @Test
    void listarResumidos_deberiaResponder200() throws Exception {
        mockMvc.perform(get("/api/app/eventos/resumidos").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    void listarCompletos_deberiaResponder200() throws Exception {
        mockMvc.perform(get("/api/app/eventos").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    void obtenerDetalle_deberiaResponder200() throws Exception {
        mockMvc
            .perform(get("/api/app/eventos/" + evento.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }
}

