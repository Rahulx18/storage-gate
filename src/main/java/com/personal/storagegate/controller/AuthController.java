package com.personal.storagegate.controller;

import com.personal.storagegate.dto.user.AuthRequest;
import com.personal.storagegate.dto.user.SignupRequestResponse;
import com.personal.storagegate.dto.user.TokenResponse;
import com.personal.storagegate.service.AuthService;
import com.personal.storagegate.service.SignupRequestService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@SecurityRequirements
public class AuthController {

    private final AuthService authService;
    private final SignupRequestService signupRequestService;

    public AuthController(AuthService authService, SignupRequestService signupRequestService) {
        this.authService = authService;
        this.signupRequestService = signupRequestService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public SignupRequestResponse signup(@RequestBody AuthRequest request, HttpServletRequest http) {
        return signupRequestService.signup(request.email(), request.password(), http.getRemoteAddr());
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody AuthRequest request) {
        return new TokenResponse(authService.login(request.email(), request.password()));
    }
}
