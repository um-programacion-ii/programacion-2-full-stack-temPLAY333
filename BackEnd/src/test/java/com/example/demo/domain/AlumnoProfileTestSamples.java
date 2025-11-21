package com.example.demo.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class AlumnoProfileTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static AlumnoProfile getAlumnoProfileSample1() {
        return new AlumnoProfile().id(1L).nombreAlumno("nombreAlumno1");
    }

    public static AlumnoProfile getAlumnoProfileSample2() {
        return new AlumnoProfile().id(2L).nombreAlumno("nombreAlumno2");
    }

    public static AlumnoProfile getAlumnoProfileRandomSampleGenerator() {
        return new AlumnoProfile().id(longCount.incrementAndGet()).nombreAlumno(UUID.randomUUID().toString());
    }
}
