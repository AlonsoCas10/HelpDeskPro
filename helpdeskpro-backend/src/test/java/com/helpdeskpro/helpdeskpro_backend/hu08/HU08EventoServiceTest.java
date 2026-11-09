package com.helpdeskpro.helpdeskpro_backend.hu08;

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
 * Pruebas unitarias de QA para la derivación del nivel y el estado al guardar el evento (HU08).
 */
@ExtendWith(MockitoExtension.class)
class HU08EventoServiceTest {

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private EvaluadorRiesgoService evaluadorRiesgoService;

    @InjectMocks
    private EventoService eventoService;

    @Test
    @DisplayName("HU08-CP-U-07 El nivel y el estado se derivan del Risk Score en el servidor")
    void HU08_CP_U_07_nivel_y_estado_se_derivan_del_puntaje() {
        when(evaluadorRiesgoService.evaluarRiesgo(any(Evento.class))).thenReturn(30);
        when(eventoRepository.save(any(Evento.class))).thenAnswer(invocacion -> {
            Evento guardado = invocacion.getArgument(0);
            if (guardado.getId() == null) {
                guardado.setId(8L);
            }
            return guardado;
        });

        Evento enviado = new Evento();
        enviado.setAsunto("QA-HU08-U07");
        enviado.setClasificacion("Crítico");
        enviado.setEstado("Cerrado");

        Evento guardado = eventoService.guardarEvento(enviado);

        assertAll(
                () -> assertEquals(30, guardado.getRiskScore(), "el Risk Score debe ser el que calcula el servidor"),
                () -> assertEquals("Medio", guardado.getClasificacion(), "con 30 puntos el nivel debe ser Medio"),
                () -> assertEquals("Requiere revisión", guardado.getEstado(), "el estado inicial debe ser Requiere revisión"));
    }
}
