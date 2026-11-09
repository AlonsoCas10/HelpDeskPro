package com.helpdeskpro.helpdeskpro_backend.hu01;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.util.ReflectionTestUtils;

import com.helpdeskpro.helpdeskpro_backend.configs.SecurityConfig;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpServer;

/**
 * Pruebas unitarias de QA para HU01: validación del token y lectura de roles en SecurityConfig.
 * No usan Cognito: generan sus propias llaves y publican su propio JWKS en un puerto local.
 */
class HU01SeguridadTokenTest {

    private static final String EMISOR = "https://qa.invalid/pool";
    private static final String CLIENTE = "qa-cliente";
    private static final long UNA_HORA = 3_600_000L;

    private static RSAKey llave;
    private static RSAKey otraLlave;
    private static HttpServer jwks;
    private static String jwksUri;

    @BeforeAll
    static void publicarJwks() throws Exception {
        llave = new RSAKeyGenerator(2048).keyID("qa").generate();
        otraLlave = new RSAKeyGenerator(2048).keyID("qa").generate();
        byte[] cuerpo = new JWKSet(llave.toPublicJWK()).toString().getBytes(UTF_8);

        jwks = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        jwks.createContext("/jwks", intercambio -> {
            intercambio.getResponseHeaders().add("Content-Type", "application/json");
            intercambio.sendResponseHeaders(200, cuerpo.length);
            intercambio.getResponseBody().write(cuerpo);
            intercambio.close();
        });
        jwks.start();
        jwksUri = "http://127.0.0.1:" + jwks.getAddress().getPort() + "/jwks";
    }

    @AfterAll
    static void detenerJwks() {
        jwks.stop(0);
    }

    private static SecurityConfig configuracion() {
        SecurityConfig config = new SecurityConfig();
        ReflectionTestUtils.setField(config, "jwkSetUri", jwksUri);
        ReflectionTestUtils.setField(config, "issuerUri", EMISOR);
        ReflectionTestUtils.setField(config, "clientId", CLIENTE);
        return config;
    }

    private static JWTClaimsSet.Builder accessTokenValido() {
        Date ahora = new Date();
        return new JWTClaimsSet.Builder()
                .issuer(EMISOR)
                .subject("qa-hu01")
                .issueTime(ahora)
                .expirationTime(new Date(ahora.getTime() + UNA_HORA))
                .claim("token_use", "access")
                .claim("client_id", CLIENTE);
    }

    private static String firmar(JWTClaimsSet claims, RSAKey conLlave) throws JOSEException {
        SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("qa").build(), claims);
        jwt.sign(new RSASSASigner(conLlave));
        return jwt.serialize();
    }

    @Test
    @DisplayName("HU01-CP-U-19 Aceptar solo access tokens vigentes, firmados y del cliente de la aplicación")
    void HU01_CP_U_19_aceptar_solo_access_tokens_validos() throws Exception {
        JwtDecoder decoder = configuracion().jwtDecoder();
        Date ahora = new Date();

        String valido = firmar(accessTokenValido().build(), llave);
        String idToken = firmar(accessTokenValido().claim("token_use", "id").build(), llave);
        String otroCliente = firmar(accessTokenValido().claim("client_id", "otro-cliente").build(), llave);
        String otroEmisor = firmar(accessTokenValido().issuer("https://otro.invalid/pool").build(), llave);
        String vencido = firmar(accessTokenValido()
                .issueTime(new Date(ahora.getTime() - 3 * UNA_HORA))
                .expirationTime(new Date(ahora.getTime() - 2 * UNA_HORA)).build(), llave);
        String firmaAjena = firmar(accessTokenValido().build(), otraLlave);
        String sinFirma = new PlainJWT(accessTokenValido().build()).serialize();

        assertAll(
                () -> assertDoesNotThrow(() -> decoder.decode(valido), "un access token válido debe aceptarse"),
                () -> assertThrows(JwtException.class, () -> decoder.decode(idToken), "un ID token debe rechazarse"),
                () -> assertThrows(JwtException.class, () -> decoder.decode(otroCliente), "un token de otro cliente debe rechazarse"),
                () -> assertThrows(JwtException.class, () -> decoder.decode(otroEmisor), "un token de otro emisor debe rechazarse"),
                () -> assertThrows(JwtException.class, () -> decoder.decode(vencido), "un token vencido debe rechazarse"),
                () -> assertThrows(JwtException.class, () -> decoder.decode(firmaAjena), "un token firmado con otra llave debe rechazarse"),
                () -> assertThrows(JwtException.class, () -> decoder.decode(sinFirma), "un token sin firma debe rechazarse"));
    }

    private static Set<String> roles(JwtAuthenticationConverter conversor, List<String> grupos) {
        Jwt.Builder jwt = Jwt.withTokenValue("qa").header("alg", "RS256").subject("qa-hu01");
        if (grupos != null) {
            jwt.claim("cognito:groups", grupos);
        }
        // Spring Security agrega también autoridades que no son roles (por ejemplo FACTOR_BEARER):
        // aquí solo interesan los roles.
        return conversor.convert(jwt.build()).getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(autoridad -> autoridad.startsWith("ROLE_"))
                .collect(Collectors.toSet());
    }

    @Test
    @DisplayName("HU01-CP-U-20 Reconocer el grupo de administrador de Cognito como rol del backend")
    void HU01_CP_U_20_reconocer_grupo_administrador() {
        JwtAuthenticationConverter conversor = configuracion().jwtAuthenticationConverter();

        assertAll(
                () -> assertEquals(Set.of("ROLE_ADMINISTRADOR"), roles(conversor, List.of("Administrador")),
                        "el grupo Administrador, como existe en Cognito, debe dar el rol de administrador"),
                () -> assertEquals(Set.of("ROLE_ADMINISTRADOR"), roles(conversor, List.of("ADMINISTRADOR")),
                        "el grupo ADMINISTRADOR también debe dar el rol de administrador"),
                () -> assertEquals(Set.of("ROLE_USUARIO"), roles(conversor, List.of("Usuario")),
                        "el grupo Usuario no debe dar el rol de administrador"),
                () -> assertEquals(Set.of(), roles(conversor, null),
                        "un token sin grupos no debe tener ningún rol"));
    }
}
