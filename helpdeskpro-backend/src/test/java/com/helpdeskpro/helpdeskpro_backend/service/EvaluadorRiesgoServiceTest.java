
package com.helpdeskpro.helpdeskpro_backend.service;

import com.helpdeskpro.helpdeskpro_backend.model.Evento;
import com.helpdeskpro.helpdeskpro_backend.model.Cliente;

import com.helpdeskpro.helpdeskpro_backend.repository.ClienteRepository;
import com.helpdeskpro.helpdeskpro_backend.repository.DeudorRepository;
import com.helpdeskpro.helpdeskpro_backend.repository.UsuarioCriticoRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluadorRiesgoServiceTest {

    // Simulamos los repositorios para no necesitar SQL Server.
    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private DeudorRepository deudorRepository;

    @Mock
    private UsuarioCriticoRepository usuarioCriticoRepository;

    // Inyectamos los repositorios simulados en el evaluador.
    @InjectMocks
    private EvaluadorRiesgoService evaluador;

    // =====================================================
    // CASO 1: DOMINIO DIFERENTE AL REGISTRADO
    // =====================================================

    @Test
    void dominioDistinto_sumaCuatroPuntos() {

        Evento evento = new Evento();

        evento.setRemitente("atacante@dominio-falso.com");
        evento.setDominioRegistrado("empresa.com");
        evento.setRemitenteConocido(true);

        // Simulamos que el correo sí está registrado.
        // Así aislamos la diferencia entre dominios.
        when(clienteRepository.findByCorreoAutorizadoIgnoreCase(
                "atacante@dominio-falso.com"))
                .thenReturn(Optional.of(Mockito.mock(Cliente.class)));

        when(deudorRepository.findByCorreoAutorizadoIgnoreCase(
                "atacante@dominio-falso.com"))
                .thenReturn(Optional.empty());

        int puntos = evaluador.evaluarRiesgo(evento);

        assertEquals(4, puntos);
    }

    // =====================================================
    // CASO 2: REMITENTE NO CONOCIDO
    // =====================================================

    @Test
    void remitenteNoConocido_sumaCuatroPuntos() {

        Evento evento = new Evento();

        evento.setRemitente("sheldon.schur@manpowergroup.vp.com");
        evento.setDominioRegistrado("manpowergroup.vp.com");
        evento.setRemitenteConocido(false);

        // Simulamos que el correo está autorizado.
        when(clienteRepository.findByCorreoAutorizadoIgnoreCase(
                "sheldon.schur@manpowergroup.vp.com"))
                .thenReturn(Optional.of(Mockito.mock(Cliente.class)));

        when(deudorRepository.findByCorreoAutorizadoIgnoreCase(
                "sheldon.schur@manpowergroup.vp.com"))
                .thenReturn(Optional.empty());

        int puntos = evaluador.evaluarRiesgo(evento);

        assertEquals(4, puntos);
    }

    // =====================================================
    // CASO 3: REMITENTE VACÍO
    // =====================================================

    @Test
    void remitenteVacio_sumaCuarentaYCincoPuntos() {

        Evento evento = new Evento();

        evento.setRemitente("");
        evento.setDominioRegistrado("manpowergroup.vp.com");
        evento.setRemitenteConocido(true);

        int puntos = evaluador.evaluarRiesgo(evento);

        // 20 puntos por remitente no identificado.
        // 25 puntos por contexto de factoring no verificable.
        assertEquals(45, puntos);
    }

    // =====================================================
    // CASO 4: REMITENTE NULO
    // =====================================================

    @Test
    void remitenteNulo_sumaCuarentaYCincoPuntos() {

        Evento evento = new Evento();

        evento.setRemitente(null);
        evento.setDominioRegistrado("manpowergroup.vp.com");
        evento.setRemitenteConocido(true);

        int puntos = evaluador.evaluarRiesgo(evento);

        // 20 puntos por remitente no identificado.
        // 25 puntos por contexto de factoring no verificable.
        assertEquals(45, puntos);
    }
}
