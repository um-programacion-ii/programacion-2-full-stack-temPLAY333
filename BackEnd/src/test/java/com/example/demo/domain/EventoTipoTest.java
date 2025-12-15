package com.example.demo.domain;

import static com.example.demo.domain.EventoTestSamples.*;
import static com.example.demo.domain.EventoTipoTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class EventoTipoTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(EventoTipo.class);
        EventoTipo eventoTipo1 = getEventoTipoSample1();
        EventoTipo eventoTipo2 = new EventoTipo();
        assertThat(eventoTipo1).isNotEqualTo(eventoTipo2);

        eventoTipo2.setId(eventoTipo1.getId());
        assertThat(eventoTipo1).isEqualTo(eventoTipo2);

        eventoTipo2 = getEventoTipoSample2();
        assertThat(eventoTipo1).isNotEqualTo(eventoTipo2);
    }

    @Test
    void eventoTest() {
        EventoTipo eventoTipo = getEventoTipoRandomSampleGenerator();
        Evento eventoBack = getEventoRandomSampleGenerator();

        eventoTipo.setEvento(eventoBack);
        assertThat(eventoTipo.getEvento()).isEqualTo(eventoBack);
        assertThat(eventoBack.getEventoTipo()).isEqualTo(eventoTipo);

        eventoTipo.evento(null);
        assertThat(eventoTipo.getEvento()).isNull();
        assertThat(eventoBack.getEventoTipo()).isNull();
    }
}
