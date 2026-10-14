package com.helpdeskpro.helpdeskpro_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import com.helpdeskpro.helpdeskpro_backend.dtos.MessageDto;

public class PublicController {
    @GetMapping("/public")
    public ResponseEntity<MessageDto> publicMessage(){
        return ResponseEntity.ok(new MessageDto("This is a public message"));
    }
}
