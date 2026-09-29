package com.helpdeskpro.helpdeskpro_backend.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Casos de prueba para EvaluadorRiesgoService.evaluarRemitente().
 *
 * Reglas actuales del método (ver EvaluadorRiesgoService.java):
 *  - Si remitente o dominioRegistrado son null           -> 20 puntos
 *  - Si el remitente no contiene "@"                     -> 20 puntos
 *  - Si el dominio del remitente != dominioRegistrado    -> +8 puntos
 *  - Si solo remitenteConocido es false                  -> +4 puntos
 */
class EvaluadorRiesgoServiceTest {

    private final EvaluadorRiesgoService evaluador = new EvaluadorRiesgoService();

    // =========================================================
    // CASO DE PRUEBA 1
    // Título: Dominio del remitente distinto al dominio registrado
    // Precondición: ninguna (método puro, sin estado)
    // Datos de entrada:
    //   remitente = "atacante@dominio-falso.com"
    //   dominioRegistrado = "empresa.com"
    //   remitenteConocido = true   (para aislar solo la regla de dominio)
    // Pasos:
    //   1. Llamar a evaluarRemitente(remitente, dominioRegistrado, remitenteConocido)
    // Resultado esperado:
    //   Puntaje = 8 (solo se activa la regla de dominio distinto)
    // =========================================================
    @Test
    void dominioDistinto_sumaOchoPuntos() {

        int puntos = evaluador.evaluarRemitente(
                "atacante@dominio-falso.com",
                "empresa.com",
                true
        );

        assertEquals(8, puntos);
    }

    // =========================================================
    // CASO DE PRUEBA 2
    // Título: Dominio del remitente no conocido
    // Precondición: ninguna (método puro, sin estado)
    // Datos de entrada:
    //   remitente = "sheldon.schur@manpowergroup.vp.com"
    //   dominioRegistrado = "manpowergroup.vp.com"
    //   remitenteConocido = falso   (para evaluar solo la regla de dominio)
    // Pasos:
    //   1. Llamar a evaluarRemitente(remitente, dominioRegistrado, remitenteConocido)
    // Resultado esperado:
    //   Puntaje = 4 (solo se activa la regla de remitente no Conocido)
    // =========================================================
    @Test
    void remitenteNoConocido_sumaCuatroPuntos() {
        int puntos = evaluador.evaluarRemitente(
                "sheldon.schur@manpowergroup.vp.com",
                "manpowergroup.vp.com",
                false
        );
        assertEquals(4, puntos);
    }

    // =========================================================
    // EDGE CASES
    // =========================================================
    // CASO DE PRUEBA 3
    // Título: Remitente vacio
    // Precondición: ninguna (método puro, sin estado)
    // Datos de entrada:
    //   remitente = ""
    //   dominioRegistrado = "manpowergroup.vp.com"
    //   remitenteConocido = true   (para evitar la regla de dominio)
    // Pasos:
    //   1. Llamar a evaluarRemitente(remitente, dominioRegistrado, remitenteConocido)
    // Resultado esperado:
    //   Puntaje = 20 (se activa la regla de arroba)
    // =========================================================
    @Test
    void remitenteVacio_sumaVeintePuntos() {
        int puntos = evaluador.evaluarRemitente(
                "",
                "manpowergroup.vp.com",
                true
        );
        assertEquals(20, puntos);
    }
    // CASO DE PRUEBA 3
    // Título: Remitente nulo
    // Precondición: ninguna (método puro, sin estado)
    // Datos de entrada:
    //   remitente = ""
    //   dominioRegistrado = "manpowergroup.vp.com"
    //   remitenteConocido = true   (para evitar la regla de dominio)
    // Pasos:
    //   1. Llamar a evaluarRemitente(remitente, dominioRegistrado, remitenteConocido)
    // Resultado esperado:
    //   Puntaje = 20 (se activa la regla de remitente nulo or dominioRegistrado nulo)
    // =========================================================
    @Test
    void remitenteNulo_sumaVeintePuntos() {
        int puntos = evaluador.evaluarRemitente(
                null,
                "manpowergroup.vp.com",
                true
        );
        assertEquals(20, puntos);
    }
}
