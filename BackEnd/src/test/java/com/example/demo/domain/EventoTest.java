package com.example.demo.domain;

import static com.example.demo.domain.EventoTestSamples.*;
import static com.example.demo.domain.EventoTipoTestSamples.*;
import static com.example.demo.domain.IntegranteTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EventoTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Evento.class);
        Evento evento1 = getEventoSample1();
        Evento evento2 = new Evento();
        assertThat(evento1).isNotEqualTo(evento2);

        evento2.setId(evento1.getId());
        assertThat(evento1).isEqualTo(evento2);

        evento2 = getEventoSample2();
        assertThat(evento1).isNotEqualTo(evento2);
    }

    @Test
    void eventoTipoTest() {
        Evento evento = getEventoRandomSampleGenerator();
        EventoTipo eventoTipoBack = getEventoTipoRandomSampleGenerator();

        evento.setEventoTipo(eventoTipoBack);
        assertThat(evento.getEventoTipo()).isEqualTo(eventoTipoBack);

        evento.eventoTipo(null);
        assertThat(evento.getEventoTipo()).isNull();
    }

    @Test
    void integrantesTest() {
        Evento evento = getEventoRandomSampleGenerator();
        Integrante integranteBack = getIntegranteRandomSampleGenerator();

        evento.addIntegrantes(integranteBack);
        assertThat(evento.getIntegrantes()).containsOnly(integranteBack);

        evento.removeIntegrantes(integranteBack);
        assertThat(evento.getIntegrantes()).doesNotContain(integranteBack);

        evento.integrantes(new HashSet<>(Set.of(integranteBack)));
        assertThat(evento.getIntegrantes()).containsOnly(integranteBack);

        evento.setIntegrantes(new HashSet<>());
        assertThat(evento.getIntegrantes()).doesNotContain(integranteBack);
    }
}
