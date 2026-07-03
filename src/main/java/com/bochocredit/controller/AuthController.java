package com.bochocredit.controller;

import com.bochocredit.dto.auth.AuthDtos.LoginRequest;
import com.bochocredit.dto.auth.AuthDtos.LoginResponse;
import com.bochocredit.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    /** Equivalente a la ruta "/" (POST) del Flask original: login con usuario y clave. */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /*
    @PostMapping("/login/test")
    public ResponseEntity<String> getPassword(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(request.password() + ":" + passwordEncoder.encode(request.password()));
    }
     */
}
