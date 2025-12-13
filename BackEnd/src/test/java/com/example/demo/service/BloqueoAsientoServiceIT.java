package com.example.demo.service;

import com.example.demo.IntegrationTest;
import com.example.demo.service.dto.AsientoSeleccionDTO;
import com.example.demo.service.dto.BloquearAsientosResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de integración para el flujo completo de Bloqueo de Asientos.
 * Valida el proceso desde la selección hasta el bloqueo temporal.
 */
@IntegrationTest
@Transactional
class BloqueoAsientoServiceIT {

    @Autowired
    private BloqueoAsientoService bloqueoAsientoService;

    private List<AsientoSeleccionDTO> asientosValidos;

    @BeforeEach
    void setUp() {
        asientosValidos = new ArrayList<>();

        AsientoSeleccionDTO asiento1 = new AsientoSeleccionDTO();
        asiento1.setFila(1);
        asiento1.setColumna(1);
        asientosValidos.add(asiento1);

        AsientoSeleccionDTO asiento2 = new AsientoSeleccionDTO();
        asiento2.setFila(1);
        asiento2.setColumna(2);
        asientosValidos.add(asiento2);
    }

    @Test
    void validarAsientos_conDimensionesDeEvento10x10_debeValidarCorrectamente() {
        // Given
        int maxFilas = 10;
        int maxColumnas = 10;

        // When
        boolean resultado = bloqueoAsientoService.validarAsientos(asientosValidos, maxFilas, maxColumnas);

        // Then
        assertThat(resultado).isTrue();
    }

    @Test
    void validarAsientos_conDimensionesDeEvento20x15_debeValidarCorrectamente() {
        // Given
        int maxFilas = 20;
        int maxColumnas = 15;

        // When
        boolean resultado = bloqueoAsientoService.validarAsientos(asientosValidos, maxFilas, maxColumnas);

        // Then
        assertThat(resultado).isTrue();
    }

    @Test
    void validarAsientos_conEventoPequeño3x3_debeRechazarAsientosFueraDeRango() {
        // Given
        int maxFilas = 3;
        int maxColumnas = 3;

        List<AsientoSeleccionDTO> asientosFueraDeRango = new ArrayList<>();
        AsientoSeleccionDTO asiento = new AsientoSeleccionDTO();
        asiento.setFila(5);
        asiento.setColumna(2);
        asientosFueraDeRango.add(asiento);

        // When
        boolean resultado = bloqueoAsientoService.validarAsientos(asientosFueraDeRango, maxFilas, maxColumnas);

        // Then
        assertThat(resultado).isFalse();
    }

    @Test
    void validarAsientos_conUnSoloAsiento_debeSerValido() {
        // Given
        List<AsientoSeleccionDTO> unAsiento = new ArrayList<>();
        AsientoSeleccionDTO asiento = new AsientoSeleccionDTO();
        asiento.setFila(1);
        asiento.setColumna(1);
        unAsiento.add(asiento);

        int maxFilas = 10;
        int maxColumnas = 10;

        // When
        boolean resultado = bloqueoAsientoService.validarAsientos(unAsiento, maxFilas, maxColumnas);

        // Then
        assertThat(resultado).isTrue();
    }

    @Test
    void validarAsientos_con4Asientos_debeSerValido() {
        // Given
        List<AsientoSeleccionDTO> cuatroAsientos = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            AsientoSeleccionDTO asiento = new AsientoSeleccionDTO();
            asiento.setFila(2);
            asiento.setColumna(i);
            cuatroAsientos.add(asiento);
        }

        int maxFilas = 10;
        int maxColumnas = 10;

        // When
        boolean resultado = bloqueoAsientoService.validarAsientos(cuatroAsientos, maxFilas, maxColumnas);

        // Then
        assertThat(resultado).isTrue();
    }

    @Test
    void validarAsientos_enEsquinaSuperiorIzquierda_debeSerValido() {
        // Given
        List<AsientoSeleccionDTO> asientos = new ArrayList<>();
        AsientoSeleccionDTO asiento = new AsientoSeleccionDTO();
        asiento.setFila(1);
        asiento.setColumna(1);
        asientos.add(asiento);

        int maxFilas = 10;
        int maxColumnas = 10;

        // When
        boolean resultado = bloqueoAsientoService.validarAsientos(asientos, maxFilas, maxColumnas);

        // Then
        assertThat(resultado).isTrue();
    }

    @Test
    void validarAsientos_enEsquinaInferiorDerecha_debeSerValido() {
        // Given
        List<AsientoSeleccionDTO> asientos = new ArrayList<>();
        AsientoSeleccionDTO asiento = new AsientoSeleccionDTO();
        asiento.setFila(10);
        asiento.setColumna(10);
        asientos.add(asiento);

        int maxFilas = 10;
        int maxColumnas = 10;

        // When
        boolean resultado = bloqueoAsientoService.validarAsientos(asientos, maxFilas, maxColumnas);

        // Then
        assertThat(resultado).isTrue();
    }

    @Test
    void validarAsientos_conPosicionesNoConsecutivas_debeSerValido() {
        // Given
        List<AsientoSeleccionDTO> asientos = new ArrayList<>();

        AsientoSeleccionDTO asiento1 = new AsientoSeleccionDTO();
        asiento1.setFila(1);
        asiento1.setColumna(1);
        asientos.add(asiento1);

        AsientoSeleccionDTO asiento2 = new AsientoSeleccionDTO();
        asiento2.setFila(5);
        asiento2.setColumna(8);
        asientos.add(asiento2);

        int maxFilas = 10;
        int maxColumnas = 10;

        // When
        boolean resultado = bloqueoAsientoService.validarAsientos(asientos, maxFilas, maxColumnas);

        // Then
        assertThat(resultado).isTrue();
    }

    @Test
    void validarAsientos_conFilaDistintaMismaColumna_debeSerValido() {
        // Given
        List<AsientoSeleccionDTO> asientos = new ArrayList<>();

        AsientoSeleccionDTO asiento1 = new AsientoSeleccionDTO();
        asiento1.setFila(1);
        asiento1.setColumna(5);
        asientos.add(asiento1);

        AsientoSeleccionDTO asiento2 = new AsientoSeleccionDTO();
        asiento2.setFila(2);
        asiento2.setColumna(5);
        asientos.add(asiento2);

        int maxFilas = 10;
        int maxColumnas = 10;

        // When
        boolean resultado = bloqueoAsientoService.validarAsientos(asientos, maxFilas, maxColumnas);

        // Then
        assertThat(resultado).isTrue();
    }

    @Test
    void validarAsientos_todoEnLaMismaFila_debeSerValido() {
        // Given
        List<AsientoSeleccionDTO> asientos = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            AsientoSeleccionDTO asiento = new AsientoSeleccionDTO();
            asiento.setFila(3);
            asiento.setColumna(i);
            asientos.add(asiento);
        }

        int maxFilas = 10;
        int maxColumnas = 10;

        // When
        boolean resultado = bloqueoAsientoService.validarAsientos(asientos, maxFilas, maxColumnas);

        // Then
        assertThat(resultado).isTrue();
    }

    @Test
    void validarAsientos_distribuidosEnVariasFilas_debeSerValido() {
        // Given
        List<AsientoSeleccionDTO> asientos = new ArrayList<>();

        AsientoSeleccionDTO asiento1 = new AsientoSeleccionDTO();
        asiento1.setFila(1);
        asiento1.setColumna(1);
        asientos.add(asiento1);

        AsientoSeleccionDTO asiento2 = new AsientoSeleccionDTO();
        asiento2.setFila(2);
        asiento2.setColumna(1);
        asientos.add(asiento2);

        AsientoSeleccionDTO asiento3 = new AsientoSeleccionDTO();
        asiento3.setFila(3);
        asiento3.setColumna(1);
        asientos.add(asiento3);

        int maxFilas = 10;
        int maxColumnas = 10;

        // When
        boolean resultado = bloqueoAsientoService.validarAsientos(asientos, maxFilas, maxColumnas);

        // Then
        assertThat(resultado).isTrue();
    }
}

