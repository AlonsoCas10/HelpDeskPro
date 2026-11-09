package com.helpdeskpro.helpdeskpro_backend.hu08;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.helpdeskpro.helpdeskpro_backend.model.Evento;
import com.helpdeskpro.helpdeskpro_backend.repository.ClienteRepository;
import com.helpdeskpro.helpdeskpro_backend.repository.DeudorRepository;
import com.helpdeskpro.helpdeskpro_backend.repository.UsuarioCriticoRepository;
import com.helpdeskpro.helpdeskpro_backend.service.EvaluadorRiesgoService;

/**
 * Pruebas unitarias de QA para el cálculo del Risk Score (HU08). Repositorios simulados: no tocan la base.
 */
@ExtendWith(MockitoExtension.class)
class HU08EvaluadorRiesgoTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private DeudorRepository deudorRepository;

    @Mock
    private UsuarioCriticoRepository usuarioCriticoRepository;

    @InjectMocks
    private EvaluadorRiesgoService evaluadorRiesgoService;

    @Test
    @DisplayName("HU08-CP-U-06 El cálculo no se rompe con el remitente ausente")
    void HU08_CP_U_06_remitente_ausente_no_rompe_el_calculo() {
        Evento evento = new Evento();

        int puntos = evaluadorRiesgoService.evaluarRiesgo(evento);

        assertAll(
                () -> assertEquals(45, puntos, "con remitente ausente suma el máximo de remitente (20) y de contexto (25)"),
                () -> assertTrue(puntos <= 100, "el Risk Score nunca supera 100"));
    }
}
