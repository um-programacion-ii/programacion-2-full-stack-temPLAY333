package com.example.demo.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class EventoTipoTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static EventoTipo getEventoTipoSample1() {
        return new EventoTipo().id(1L).nombre("nombre1").descripcion("descripcion1");
    }

    public static EventoTipo getEventoTipoSample2() {
        return new EventoTipo().id(2L).nombre("nombre2").descripcion("descripcion2");
    }

    public static EventoTipo getEventoTipoRandomSampleGenerator() {
        return new EventoTipo()
            .id(longCount.incrementAndGet())
            .nombre(UUID.randomUUID().toString())
            .descripcion(UUID.randomUUID().toString());
    }
}
