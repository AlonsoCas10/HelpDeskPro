package com.helpdeskpro.helpdeskpro_backend.hu11;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.helpdeskpro.helpdeskpro_backend.model.Evento;
import com.helpdeskpro.helpdeskpro_backend.repository.EventoRepository;
import com.helpdeskpro.helpdeskpro_backend.service.EvaluadorRiesgoService;
import com.helpdeskpro.helpdeskpro_backend.service.EventoService;

/**
 * Pruebas unitarias de QA para HU11. Usan el repositorio y el evaluador simulados: no tocan la base.
 */
@ExtendWith(MockitoExtension.class)
class HU11EventoServiceTest {

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private EvaluadorRiesgoService evaluadorRiesgoService;

    @InjectMocks
    private EventoService eventoService;

    @Test
    @DisplayName("HU11-CP-U-04 Calcular el Risk Score y el estado inicial en el servidor")
    void HU11_CP_U_04_calcular_puntaje_nivel_y_estado_en_el_servidor() {
        when(evaluadorRiesgoService.evaluarRiesgo(any(Evento.class))).thenReturn(30);
        when(eventoRepository.save(any(Evento.class))).thenAnswer(invocacion -> {
            Evento guardado = invocacion.getArgument(0);
            if (guardado.getId() == null) {
                guardado.setId(7L);
            }
            return guardado;
        });

        Evento enviado = new Evento();
        enviado.setAsunto("QA-HU11-U04");
        enviado.setRiskScore(99);
        enviado.setClasificacion("Crítico");

        Evento guardado = eventoService.guardarEvento(enviado);

        assertAll(
                () -> assertEquals(30, guardado.getRiskScore(), "el Risk Score debe ser el que calcula el servidor"),
                () -> assertEquals("Medio", guardado.getClasificacion(), "con 30 puntos el nivel debe ser Medio"));
    }
}
