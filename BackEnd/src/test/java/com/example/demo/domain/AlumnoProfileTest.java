package com.example.demo.domain;

import static com.example.demo.domain.AlumnoProfileTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AlumnoProfileTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AlumnoProfile.class);
        AlumnoProfile alumnoProfile1 = getAlumnoProfileSample1();
        AlumnoProfile alumnoProfile2 = new AlumnoProfile();
        assertThat(alumnoProfile1).isNotEqualTo(alumnoProfile2);

        alumnoProfile2.setId(alumnoProfile1.getId());
        assertThat(alumnoProfile1).isEqualTo(alumnoProfile2);

        alumnoProfile2 = getAlumnoProfileSample2();
        assertThat(alumnoProfile1).isNotEqualTo(alumnoProfile2);
    }
}
