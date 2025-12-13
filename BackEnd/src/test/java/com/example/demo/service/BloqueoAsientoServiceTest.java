package com.example.demo.service;

import com.example.demo.service.dto.AsientoSeleccionDTO;
import com.example.demo.service.dto.BloquearAsientosResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test unitario para BloqueoAsientoService.
 * Valida la lógica de negocio de bloqueo de asientos.
 */
@ExtendWith(MockitoExtension.class)
class BloqueoAsientoServiceTest {

    private BloqueoAsientoService bloqueoAsientoService;

    @BeforeEach
    void setUp() {
        bloqueoAsientoService = new BloqueoAsientoService();
        ReflectionTestUtils.setField(bloqueoAsientoService, "proxyBaseUrl", "http://localhost:8080");
    }

    @Test
    void validarAsientos_conAsientosValidos_debeRetornarTrue() {
        // Given
        List<AsientoSeleccionDTO> asientos = new ArrayList<>();
        AsientoSeleccionDTO asiento1 = new AsientoSeleccionDTO();
        asiento1.setFila(1);
        asiento1.setColumna(1);
        asientos.add(asiento1);

        AsientoSeleccionDTO asiento2 = new AsientoSeleccionDTO();
        asiento2.setFila(2);
        asiento2.setColumna(3);
        asientos.add(asiento2);

        int maxFilas = 10;
        int maxColumnas = 10;

        // When
        boolean resultado = bloqueoAsientoService.validarAsientos(asientos, maxFilas, maxColumnas);

        // Then
        assertThat(resultado).isTrue();
    }

    @Test
    void validarAsientos_conListaVacia_debeRetornarFalse() {
        // Given
        List<AsientoSeleccionDTO> asientos = new ArrayList<>();
        int maxFilas = 10;
        int maxColumnas = 10;

        // When
        boolean resultado = bloqueoAsientoService.validarAsientos(asientos, maxFilas, maxColumnas);

        // Then
        assertThat(resultado).isFalse();
    }

    @Test
    void validarAsientos_conMasDe4Asientos_debeRetornarFalse() {
        // Given
        List<AsientoSeleccionDTO> asientos = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            AsientoSeleccionDTO asiento = new AsientoSeleccionDTO();
            asiento.setFila(i);
            asiento.setColumna(i);
            asientos.add(asiento);
        }

        int maxFilas = 10;
        int maxColumnas = 10;

        // When
        boolean resultado = bloqueoAsientoService.validarAsientos(asientos, maxFilas, maxColumnas);

        // Then
        assertThat(resultado).isFalse();
    }

    @Test
    void validarAsientos_conFilaNula_debeRetornarFalse() {
        // Given
        List<AsientoSeleccionDTO> asientos = new ArrayList<>();
        AsientoSeleccionDTO asiento = new AsientoSeleccionDTO();
        asiento.setFila(null);
        asiento.setColumna(1);
        asientos.add(asiento);

        int maxFilas = 10;
        int maxColumnas = 10;

        // When
        boolean resultado = bloqueoAsientoService.validarAsientos(asientos, maxFilas, maxColumnas);

        // Then
        assertThat(resultado).isFalse();
    }

    @Test
    void validarAsientos_conColumnaNula_debeRetornarFalse() {
        // Given
        List<AsientoSeleccionDTO> asientos = new ArrayList<>();
        AsientoSeleccionDTO asiento = new AsientoSeleccionDTO();
        asiento.setFila(1);
        asiento.setColumna(null);
        asientos.add(asiento);

        int maxFilas = 10;
        int maxColumnas = 10;

        // When
        boolean resultado = bloqueoAsientoService.validarAsientos(asientos, maxFilas, maxColumnas);

        // Then
        assertThat(resultado).isFalse();
    }

    @Test
    void validarAsientos_conFilaCero_debeRetornarFalse() {
        // Given
        List<AsientoSeleccionDTO> asientos = new ArrayList<>();
        AsientoSeleccionDTO asiento = new AsientoSeleccionDTO();
        asiento.setFila(0);
        asiento.setColumna(1);
        asientos.add(asiento);

        int maxFilas = 10;
        int maxColumnas = 10;

        // When
        boolean resultado = bloqueoAsientoService.validarAsientos(asientos, maxFilas, maxColumnas);

        // Then
        assertThat(resultado).isFalse();
    }

    @Test
    void validarAsientos_conColumnaNegativa_debeRetornarFalse() {
        // Given
        List<AsientoSeleccionDTO> asientos = new ArrayList<>();
        AsientoSeleccionDTO asiento = new AsientoSeleccionDTO();
        asiento.setFila(1);
        asiento.setColumna(-1);
        asientos.add(asiento);

        int maxFilas = 10;
        int maxColumnas = 10;

        // When
        boolean resultado = bloqueoAsientoService.validarAsientos(asientos, maxFilas, maxColumnas);

        // Then
        assertThat(resultado).isFalse();
    }

    @Test
    void validarAsientos_conFilaFueraDeRango_debeRetornarFalse() {
        // Given
        List<AsientoSeleccionDTO> asientos = new ArrayList<>();
        AsientoSeleccionDTO asiento = new AsientoSeleccionDTO();
        asiento.setFila(15);
        asiento.setColumna(5);
        asientos.add(asiento);

        int maxFilas = 10;
        int maxColumnas = 10;

        // When
        boolean resultado = bloqueoAsientoService.validarAsientos(asientos, maxFilas, maxColumnas);

        // Then
        assertThat(resultado).isFalse();
    }

    @Test
    void validarAsientos_conColumnaFueraDeRango_debeRetornarFalse() {
        // Given
        List<AsientoSeleccionDTO> asientos = new ArrayList<>();
        AsientoSeleccionDTO asiento = new AsientoSeleccionDTO();
        asiento.setFila(5);
        asiento.setColumna(15);
        asientos.add(asiento);

        int maxFilas = 10;
        int maxColumnas = 10;

        // When
        boolean resultado = bloqueoAsientoService.validarAsientos(asientos, maxFilas, maxColumnas);

        // Then
        assertThat(resultado).isFalse();
    }

    @Test
    void validarAsientos_conExactamente4AsientosValidos_debeRetornarTrue() {
        // Given
        List<AsientoSeleccionDTO> asientos = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            AsientoSeleccionDTO asiento = new AsientoSeleccionDTO();
            asiento.setFila(i);
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
    void validarAsientos_conAsientosEnLimiteSuperior_debeRetornarTrue() {
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
}

