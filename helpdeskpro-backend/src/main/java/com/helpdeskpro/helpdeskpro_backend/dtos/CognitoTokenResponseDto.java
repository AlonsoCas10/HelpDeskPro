package com.helpdeskpro.helpdeskpro_backend.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * CognitoTokenResponseDto
 */
@JsonIgnoreProperties (ignoreUnknown = true)
public record CognitoTokenResponseDto(String id_token) {

}
