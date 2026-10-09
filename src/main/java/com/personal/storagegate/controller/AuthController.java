package com.personal.storagegate.controller;

import com.personal.storagegate.dto.user.AuthRequest;
import com.personal.storagegate.dto.user.TokenResponse;
import com.personal.storagegate.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public TokenResponse signup(@RequestBody AuthRequest request) {
        return new TokenResponse(authService.signup(request.username(), request.password()));
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody AuthRequest request) {
        return new TokenResponse(authService.login(request.username(), request.password()));
    }
}
