package com.helpdeskpro.helpdeskpro_backend.service;

import org.springframework.stereotype.Service;

@Service
public class EvaluadorRiesgoService {
public int evaluarRemitente(
        String remitente,
        String dominioRegistrado,
        Boolean remitenteConocido) {

    int puntos = 0;

    // Verificamos que existan los datos necesarios
    if (remitente == null || dominioRegistrado == null) {
        return 20;
    }

    // Buscamos el símbolo @
    int posicionArroba = remitente.indexOf("@");

    // Si el correo no tiene @, lo consideramos sospechoso
    if (posicionArroba == -1) {
        return 20;
    }

    // Extraemos el dominio del correo
    String dominioRemitente =
            remitente.substring(posicionArroba + 1);

    // REGLA 1:
    // Dominio diferente al registrado = +8
    if (!dominioRemitente.equalsIgnoreCase(dominioRegistrado)) {
        puntos += 8;
    }

    // REGLA 2:
    // Remitente no conocido = +4
    if (Boolean.FALSE.equals(remitenteConocido)) {
        puntos += 4;
    }

    return puntos;
    }
}