
package com.helpdeskpro.helpdeskpro_backend.service;

import com.helpdeskpro.helpdeskpro_backend.model.Evento;
import com.helpdeskpro.helpdeskpro_backend.model.Cliente;
import com.helpdeskpro.helpdeskpro_backend.model.Deudor;
import com.helpdeskpro.helpdeskpro_backend.model.UsuarioCritico;

import com.helpdeskpro.helpdeskpro_backend.repository.ClienteRepository;
import com.helpdeskpro.helpdeskpro_backend.repository.DeudorRepository;
import com.helpdeskpro.helpdeskpro_backend.repository.UsuarioCriticoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EvaluadorRiesgoService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private DeudorRepository deudorRepository;

    @Autowired
    private UsuarioCriticoRepository usuarioCriticoRepository;

    // =====================================================
    // MÉTODO PRINCIPAL: EVALUAR LOS CINCO CRITERIOS
    // =====================================================

    public int evaluarRiesgo(Evento evento) {

        int puntosRemitente = evaluarRemitente(evento);
        int puntosEnlacesArchivos = evaluarEnlacesYArchivos(evento);
        int puntosContenido = evaluarContenido(evento);
        int puntosFactoring = evaluarContextoFactoring(evento);
        int puntosCriticidad = evaluarCriticidad(evento);

        int riesgoTotal =
                puntosRemitente
                + puntosEnlacesArchivos
                + puntosContenido
                + puntosFactoring
                + puntosCriticidad;

        // El resultado nunca debe superar los 100 puntos.
        return Math.min(riesgoTotal, 100);
    }

    // =====================================================
    // 1. REMITENTE Y DOMINIO (MÁXIMO 20 PUNTOS)
    // =====================================================

    private int evaluarRemitente(Evento evento) {

        int puntos = 0;

        String correo = normalizarCorreo(evento.getRemitente());

        if (correo == null) {
            correo = normalizarCorreo(evento.getCorreoRemitente());
        }

        if (correo == null) {
            return 20;
        }

        String dominio = extraerDominio(correo);

        boolean remitenteAutorizado = false;

        // Comprobar si el correo pertenece a un cliente registrado.
        var cliente = clienteRepository
                .findByCorreoAutorizadoIgnoreCase(correo);

        if (cliente.isPresent()) {
            remitenteAutorizado = true;
        }

        // Comprobar si el correo pertenece a un deudor registrado.
        var deudor = deudorRepository
                .findByCorreoAutorizadoIgnoreCase(correo);

        if (deudor.isPresent()) {
            remitenteAutorizado = true;
        }

        // Comprobar el dominio autorizado de los clientes.
        if (!remitenteAutorizado && dominio != null) {

            var clienteDominio = clienteRepository
                    .findByDominioAutorizadoIgnoreCase(dominio);

            if (clienteDominio.isPresent()) {
                remitenteAutorizado = true;
            }
        }

        // Comprobar el dominio autorizado de los deudores.
        if (!remitenteAutorizado && dominio != null) {

            var deudorDominio = deudorRepository
                    .findByDominioAutorizadoIgnoreCase(dominio);

            if (deudorDominio.isPresent()) {
                remitenteAutorizado = true;
            }
        }

        if (!remitenteAutorizado) {
            puntos += 12;
        }

        // Si el remitente está marcado explícitamente como desconocido.
        if (Boolean.FALSE.equals(evento.getRemitenteConocido())) {
            puntos += 4;
        }

        // Si se dispone de un dominio registrado, comparar ambos dominios.
        String dominioRegistrado = evento.getDominioRegistrado();

        if (dominioRegistrado != null
                && !dominioRegistrado.isBlank()
                && !dominio.equalsIgnoreCase(dominioRegistrado.trim())) {

            puntos += 4;
        }

        return Math.min(puntos, 20);
    }

    // =====================================================
    // 2. ENLACES Y ARCHIVOS (MÁXIMO 20 PUNTOS)
    // =====================================================

    private int evaluarEnlacesYArchivos(Evento evento) {

        int puntos = 0;

        // Revisar enlaces del correo.
        if (evento.getEnlaces() != null) {

            for (var enlace : evento.getEnlaces()) {

                String url = enlace.getUrl();

                if (url == null || url.isBlank()) {
                    continue;
                }

                String urlNormalizada = url.toLowerCase(Locale.ROOT);

                // Enlaces que utilizan direcciones IP directamente.
                if (urlNormalizada.matches(
                        ".*https?://\\d{1,3}(\\.\\d{1,3}){3}.*")) {
                    puntos += 5;
                }

                // Enlaces que contienen caracteres sospechosos.
                if (urlNormalizada.contains("@")
                        || urlNormalizada.contains("xn--")) {
                    puntos += 4;
                }

                // Acortar enlaces dificulta identificar su destino.
                if (urlNormalizada.contains("bit.ly")
                        || urlNormalizada.contains("tinyurl.com")
                        || urlNormalizada.contains("t.co")) {
                    puntos += 3;
                }
            }
        }

        // Revisar extensiones de archivos adjuntos.
        if (evento.getArchivos() != null) {

            for (var archivo : evento.getArchivos()) {

                String extension = archivo.getExtension();

                if (extension == null || extension.isBlank()) {
                    continue;
                }

                extension = extension.toLowerCase(Locale.ROOT)
                        .replace(".", "")
                        .trim();

                // Extensiones ejecutables o potencialmente peligrosas.
                if (extension.matches(
                        "exe|js|jse|vbs|vbe|bat|cmd|ps1|scr|msi|hta|jar|com")) {

                    puntos += 10;

                } else if (extension.matches(
                        "docm|xlsm|pptm|iso|img|zip|rar|7z")) {

                    puntos += 5;
                }
            }
        }

        return Math.min(puntos, 20);
    }

    // =====================================================
    // 3. CONTENIDO Y COMPORTAMIENTO (MÁXIMO 20 PUNTOS)
    // =====================================================

    private int evaluarContenido(Evento evento) {

        int puntos = 0;

        String contenido = "";

        if (evento.getAsunto() != null) {
            contenido += evento.getAsunto() + " ";
        }

        if (evento.getContenidoTexto() != null) {
            contenido += evento.getContenidoTexto();
        }

        contenido = contenido.toLowerCase(Locale.ROOT);

        // Solicitudes de transferencias o movimientos de dinero.
        if (contieneAlguna(contenido,
                "realizar transferencia",
                "transferencia urgente",
                "depositar",
                "pago inmediato",
                "orden de pago")) {

            puntos += 5;
        }

        // Cambios de cuentas bancarias.
        if (contieneAlguna(contenido,
                "cambio de cuenta bancaria",
                "nueva cuenta bancaria",
                "actualizar cuenta bancaria",
                "nuevo cci",
                "cambio de cci")) {

            puntos += 7;
        }

        // Presión o urgencia inusual.
        if (contieneAlguna(contenido,
                "urgente",
                "inmediatamente",
                "hoy mismo",
                "última oportunidad",
                "confidencial",
                "no informar")) {

            puntos += 4;
        }

        // Solicitudes de información sensible.
        if (contieneAlguna(contenido,
                "contraseña",
                "clave bancaria",
                "credenciales",
                "token de seguridad",
                "datos de acceso")) {

            puntos += 7;
        }

        return Math.min(puntos, 20);
    }

    // =====================================================
    // 4. CONTEXTO DE FACTORING (MÁXIMO 25 PUNTOS)
    // =====================================================

    private int evaluarContextoFactoring(Evento evento) {

        int puntos = 0;

        String correo = normalizarCorreo(evento.getRemitente());

        if (correo == null) {
            correo = normalizarCorreo(evento.getCorreoRemitente());
        }

        if (correo == null) {
            return 25;
        }

        String dominio = extraerDominio(correo);

        boolean existeCliente = false;
        boolean existeDeudor = false;

        // Validar si el remitente corresponde a un cliente.
        var clienteCorreo = clienteRepository
                .findByCorreoAutorizadoIgnoreCase(correo);

        if (clienteCorreo.isPresent()) {

            existeCliente = true;

        } else if (dominio != null) {

            var clienteDominio = clienteRepository
                    .findByDominioAutorizadoIgnoreCase(dominio);

            existeCliente = clienteDominio.isPresent();
        }

        // Validar si el remitente corresponde a un deudor.
        var deudorCorreo = deudorRepository
                .findByCorreoAutorizadoIgnoreCase(correo);

        if (deudorCorreo.isPresent()) {

            existeDeudor = true;

        } else if (dominio != null) {

            var deudorDominio = deudorRepository
                    .findByDominioAutorizadoIgnoreCase(dominio);

            existeDeudor = deudorDominio.isPresent();
        }

        // Si no pertenece a ningún cliente ni deudor registrado.
        if (!existeCliente && !existeDeudor) {
            puntos += 15;
        }

        // Si existe un remitente conocido, no se agregan puntos
        // por falta de identificación en este criterio.

        return Math.min(puntos, 25);
    }

    // =====================================================
    // 5. CRITICIDAD DEL USUARIO (MÁXIMO 15 PUNTOS)
    // =====================================================

    private int evaluarCriticidad(Evento evento) {

        String destinatario = evento.getDestinatario();

        if (destinatario == null || destinatario.isBlank()) {
            return 0;
        }

        // Extraer las direcciones de correo del destinatario.
        Matcher matcher = Pattern.compile(
                "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
        ).matcher(destinatario);

        while (matcher.find()) {

            String correo = matcher.group().toLowerCase(Locale.ROOT);

            var usuarioCritico = usuarioCriticoRepository
                    .findByCorreoIgnoreCase(correo);

            if (usuarioCritico.isPresent()) {
                return 15;
            }
        }

        return 0;
    }

    // =====================================================
    // MÉTODOS AUXILIARES
    // =====================================================

    private String normalizarCorreo(String correo) {

        if (correo == null || correo.isBlank()) {
            return null;
        }

        Matcher matcher = Pattern.compile(
                "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
        ).matcher(correo);

        if (matcher.find()) {
            return matcher.group().toLowerCase(Locale.ROOT);
        }

        return null;
    }

    private String extraerDominio(String correo) {

        if (correo == null || !correo.contains("@")) {
            return null;
        }

        return correo.substring(correo.lastIndexOf("@") + 1)
                .toLowerCase(Locale.ROOT);
    }

    private boolean contieneAlguna(String texto, String... palabras) {

        for (String palabra : palabras) {

            if (texto.contains(palabra.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }

        return false;
    }
}
