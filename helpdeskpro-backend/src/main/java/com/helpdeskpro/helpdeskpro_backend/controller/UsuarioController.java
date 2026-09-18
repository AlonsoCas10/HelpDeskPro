package com.helpdeskpro.helpdeskpro_backend.controller;

import com.helpdeskpro.helpdeskpro_backend.model.Usuario;
import com.helpdeskpro.helpdeskpro_backend.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "http://localhost:5173")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@RequestBody Usuario usuario) {

        if (usuarioRepository.findByCorreo(usuario.getCorreo()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body("El correo ya está registrado");
        }

        Usuario nuevoUsuario = usuarioRepository.save(usuario);

        return ResponseEntity.ok(nuevoUsuario);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario usuario) {

        Optional<Usuario> usuarioEncontrado =
                usuarioRepository.findByCorreo(usuario.getCorreo());

        if (usuarioEncontrado.isEmpty()) {
            return ResponseEntity.status(401)
                    .body("Correo o contraseña incorrectos");
        }

        Usuario usuarioBD = usuarioEncontrado.get();

        if (!usuarioBD.getContraseña().equals(usuario.getContraseña())) {
            return ResponseEntity.status(401)
                    .body("Correo o contraseña incorrectos");
        }

        return ResponseEntity.ok(usuarioBD);
    }
}