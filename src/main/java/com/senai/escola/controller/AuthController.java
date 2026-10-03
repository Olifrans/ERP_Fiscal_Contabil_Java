package com.senai.escola.controller;



import com.senai.escola.dto.AuthRequest;
import com.senai.escola.dto.AuthResponse;
import com.senai.escola.entity.Usuario;
import com.senai.escola.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req) {
        return ResponseEntity.ok(service.login(req));
    }

    @PostMapping("/register")
    public ResponseEntity<Usuario> register(@RequestBody Usuario u) {
        return ResponseEntity.ok(service.register(u));
    }
}