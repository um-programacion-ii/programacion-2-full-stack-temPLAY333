package com.example.demo.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class IntegranteDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(IntegranteDTO.class);
        IntegranteDTO integranteDTO1 = new IntegranteDTO();
        integranteDTO1.setId(1L);
        IntegranteDTO integranteDTO2 = new IntegranteDTO();
        assertThat(integranteDTO1).isNotEqualTo(integranteDTO2);
        integranteDTO2.setId(integranteDTO1.getId());
        assertThat(integranteDTO1).isEqualTo(integranteDTO2);
        integranteDTO2.setId(2L);
        assertThat(integranteDTO1).isNotEqualTo(integranteDTO2);
        integranteDTO1.setId(null);
        assertThat(integranteDTO1).isNotEqualTo(integranteDTO2);
    }
}
