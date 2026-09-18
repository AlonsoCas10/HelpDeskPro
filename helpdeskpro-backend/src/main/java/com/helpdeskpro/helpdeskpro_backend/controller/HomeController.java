package com.helpdeskpro.helpdeskpro_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String inicio() {
        return "HelpDeskPro Backend funcionando 🚀";
    }
}
