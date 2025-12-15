package com.example.demo.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AlumnoProfileDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AlumnoProfileDTO.class);
        AlumnoProfileDTO alumnoProfileDTO1 = new AlumnoProfileDTO();
        alumnoProfileDTO1.setId(1L);
        AlumnoProfileDTO alumnoProfileDTO2 = new AlumnoProfileDTO();
        assertThat(alumnoProfileDTO1).isNotEqualTo(alumnoProfileDTO2);
        alumnoProfileDTO2.setId(alumnoProfileDTO1.getId());
        assertThat(alumnoProfileDTO1).isEqualTo(alumnoProfileDTO2);
        alumnoProfileDTO2.setId(2L);
        assertThat(alumnoProfileDTO1).isNotEqualTo(alumnoProfileDTO2);
        alumnoProfileDTO1.setId(null);
        assertThat(alumnoProfileDTO1).isNotEqualTo(alumnoProfileDTO2);
    }
}
