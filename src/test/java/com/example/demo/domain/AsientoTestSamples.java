package com.example.demo.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class AsientoTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Asiento getAsientoSample1() {
        return new Asiento().id(1L).fila(1).columna(1).persona("persona1");
    }

    public static Asiento getAsientoSample2() {
        return new Asiento().id(2L).fila(2).columna(2).persona("persona2");
    }

    public static Asiento getAsientoRandomSampleGenerator() {
        return new Asiento()
            .id(longCount.incrementAndGet())
            .fila(intCount.incrementAndGet())
            .columna(intCount.incrementAndGet())
            .persona(UUID.randomUUID().toString());
    }
}
