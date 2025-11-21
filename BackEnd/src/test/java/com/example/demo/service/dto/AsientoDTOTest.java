package com.example.demo.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AsientoDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AsientoDTO.class);
        AsientoDTO asientoDTO1 = new AsientoDTO();
        asientoDTO1.setId(1L);
        AsientoDTO asientoDTO2 = new AsientoDTO();
        assertThat(asientoDTO1).isNotEqualTo(asientoDTO2);
        asientoDTO2.setId(asientoDTO1.getId());
        assertThat(asientoDTO1).isEqualTo(asientoDTO2);
        asientoDTO2.setId(2L);
        assertThat(asientoDTO1).isNotEqualTo(asientoDTO2);
        asientoDTO1.setId(null);
        assertThat(asientoDTO1).isNotEqualTo(asientoDTO2);
    }
}
