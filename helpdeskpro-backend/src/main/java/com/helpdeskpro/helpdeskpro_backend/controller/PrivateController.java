package com.helpdeskpro.helpdeskpro_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;

import com.helpdeskpro.helpdeskpro_backend.dtos.MessageDto;

public class PrivateController {
    @GetMapping ("/private")
    public ResponseEntity<MessageDto> privateMessage(@AuthenticationPrincipal Jwt jwt){
        return ResponseEntity.ok(new MessageDto("Hello " + jwt.getClaim("name")));
    }
}
