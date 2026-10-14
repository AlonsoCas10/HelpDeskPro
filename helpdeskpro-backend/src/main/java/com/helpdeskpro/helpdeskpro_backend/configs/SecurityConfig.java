package com.helpdeskpro.helpdeskpro_backend.configs;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.beans.factory.annotation.Value;

@Configuration
@EnableWebSecurity 
@EnableMethodSecurity
public class SecurityConfig {
    @Value("${COGNITO_JWK_SET_URI}")
    private String jwkSetUri;

    @Value("${COGNITO_ISSUER_URI}")
    private String issuerUri;

    @Value("${COGNITO_CLIENT_ID}")
    private String clientId;

    @Bean 
    public CorsConfigurationSource cors(){
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> tokenUseValidator = jwt -> validateClaim(
            jwt,
            "token_use",
            "access",
            "The token is not an access token"
        );
        OAuth2TokenValidator<Jwt> clientIdValidator = jwt -> validateClaim(
            jwt,
            "client_id",
            clientId,
            "The token belongs to a different Cognito app client"
        );

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
            issuerValidator,
            tokenUseValidator,
            clientIdValidator
        ));

        return decoder;
    }

    private OAuth2TokenValidatorResult validateClaim(
            Jwt jwt,
            String claimName,
            String expectedValue,
            String errorDescription) {
        if (expectedValue.equals(jwt.getClaimAsString(claimName))) {
            return OAuth2TokenValidatorResult.success();
        }

        return OAuth2TokenValidatorResult.failure(
                new OAuth2Error("invalid_token", errorDescription, null)
        );
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<String> groups = jwt.getClaimAsStringList("cognito:groups");

            if (groups == null) {
                return List.of();
            }

            return groups.stream()
                    .map(group -> (GrantedAuthority) new SimpleGrantedAuthority(
                            "ROLE_" + group.toUpperCase(Locale.ROOT)))
                    .toList();
            });
        return converter;
    }

    @Bean
    public SecurityFilterChain security(HttpSecurity http) throws Exception {
        http.cors(c->c.configurationSource(cors()))
            .csrf(c->c.disable())
            .exceptionHandling(c->c.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .sessionManagement(c->c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(autorize->
                autorize.requestMatchers("/", "/error", "/usuarios/login", "/usuarios/registro").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated()
            )
                .oauth2ResourceServer(c->c.jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    } 
}
