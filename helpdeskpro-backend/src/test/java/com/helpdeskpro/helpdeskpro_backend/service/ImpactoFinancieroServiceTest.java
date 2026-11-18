package com.helpdeskpro.helpdeskpro_backend.service;

import com.helpdeskpro.helpdeskpro_backend.model.Cliente;
import com.helpdeskpro.helpdeskpro_backend.model.ConfiguracionEmpresa;
import com.helpdeskpro.helpdeskpro_backend.model.Moneda;
import com.helpdeskpro.helpdeskpro_backend.model.ResultadoImpactoFinanciero;
import com.helpdeskpro.helpdeskpro_backend.repository.ClienteRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImpactoFinancieroServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ImpactoFinancieroService service;

    @Test
    void calculaEjemploDocumentado() {
        ConfiguracionEmpresa configuracion = ConfiguracionEmpresa.builder()
                .umbralMontoRelevante(new BigDecimal("50000"))
                .build();

        Cliente cliente = Cliente.builder()
                .id(1L)
                .ruc("20123456789")
                .razonSocial("Comercial ABC S.A.C.")
                .moneda(Moneda.builder().id(1L).nombre("PEN").build())
                .montoSemanal(new BigDecimal("100000"))
                .comision(new BigDecimal("0.03"))
                .diasOperativos(5)
                .activo(true)
                .configuracionEmpresa(configuracion)
                .build();

        when(clienteRepository.findByRuc("20123456789"))
                .thenReturn(Optional.of(cliente));

        ResultadoImpactoFinanciero resultado = service.calcular(
                "20123456789",
                80,
                new BigDecimal("50000")
        );

        assertTrue(resultado.estimable());
        assertEquals("Crítico", resultado.nivelRiesgo());
        assertEquals("PEN", resultado.moneda());
        assertEquals(new BigDecimal("20000.00"), resultado.montoDiario());
        assertEquals(2, resultado.diasInterrupcion());
        assertEquals(new BigDecimal("40000.00"), resultado.volumenAfectado());
        assertEquals(new BigDecimal("1200.00"), resultado.perdidaIngresos());
        assertEquals(new BigDecimal("50000"), resultado.exposicionDirecta());
        assertEquals("Crítico", resultado.categoria());
    }

    @Test
    void omitePerdidaCuandoFaltaComision() {
        ConfiguracionEmpresa configuracion = ConfiguracionEmpresa.builder()
                .umbralMontoRelevante(new BigDecimal("100000"))
                .build();

        Cliente cliente = Cliente.builder()
                .ruc("20123456789")
                .moneda(Moneda.builder().id(1L).nombre("PEN").build())
                .montoSemanal(new BigDecimal("100000"))
                .diasOperativos(5)
                .activo(true)
                .configuracionEmpresa(configuracion)
                .build();

        when(clienteRepository.findByRuc("20123456789"))
                .thenReturn(Optional.of(cliente));

        ResultadoImpactoFinanciero resultado = service.calcular(
                "20123456789",
                60,
                null
        );

        assertTrue(resultado.estimable());
        assertEquals(new BigDecimal("20000.00"), resultado.montoDiario());
        assertEquals(new BigDecimal("20000.00"), resultado.volumenAfectado());
        assertNull(resultado.perdidaIngresos());
        assertEquals("Bajo", resultado.categoria());
        assertTrue(resultado.advertencias().stream()
                .anyMatch(advertencia -> advertencia.contains("comisión")));
    }

    @Test
    void noEstimaSinClienteActivo() {
        when(clienteRepository.findByRuc("20123456789"))
                .thenReturn(Optional.empty());

        ResultadoImpactoFinanciero resultado = service.calcular(
                "20123456789",
                80,
                new BigDecimal("50000")
        );

        assertFalse(resultado.estimable());
        assertEquals(
                "Impacto financiero no estimable por falta de contexto.",
                resultado.mensaje()
        );
    }
}
