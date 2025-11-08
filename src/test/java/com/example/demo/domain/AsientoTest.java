package com.example.demo.domain;

import static com.example.demo.domain.AsientoTestSamples.*;
import static com.example.demo.domain.VentaTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AsientoTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Asiento.class);
        Asiento asiento1 = getAsientoSample1();
        Asiento asiento2 = new Asiento();
        assertThat(asiento1).isNotEqualTo(asiento2);

        asiento2.setId(asiento1.getId());
        assertThat(asiento1).isEqualTo(asiento2);

        asiento2 = getAsientoSample2();
        assertThat(asiento1).isNotEqualTo(asiento2);
    }

    @Test
    void ventaTest() {
        Asiento asiento = getAsientoRandomSampleGenerator();
        Venta ventaBack = getVentaRandomSampleGenerator();

        asiento.setVenta(ventaBack);
        assertThat(asiento.getVenta()).isEqualTo(ventaBack);

        asiento.venta(null);
        assertThat(asiento.getVenta()).isNull();
    }
}
