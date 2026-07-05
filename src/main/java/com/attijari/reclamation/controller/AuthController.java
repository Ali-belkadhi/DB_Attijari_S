package com.attijari.reclamation.controller;

import com.attijari.reclamation.dto.LoginDto;
import com.attijari.reclamation.dto.LoginResponseDto;
import com.attijari.reclamation.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "auth", description = "Authentication Endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public LoginResponseDto login(@Valid @RequestBody LoginDto loginDto) {
        return authService.login(loginDto);
    }
}
