package com.example.demo.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class IntegranteTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Integrante getIntegranteSample1() {
        return new Integrante().id(1L).nombre("nombre1").apellido("apellido1").identificacion("identificacion1");
    }

    public static Integrante getIntegranteSample2() {
        return new Integrante().id(2L).nombre("nombre2").apellido("apellido2").identificacion("identificacion2");
    }

    public static Integrante getIntegranteRandomSampleGenerator() {
        return new Integrante()
            .id(longCount.incrementAndGet())
            .nombre(UUID.randomUUID().toString())
            .apellido(UUID.randomUUID().toString())
            .identificacion(UUID.randomUUID().toString());
    }
}
