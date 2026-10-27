package com.personal.storagegate.controller;

import com.personal.storagegate.dto.user.SignupRequestResponse;
import com.personal.storagegate.service.SignupRequestService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/signup-requests")
public class AdminSignupController {

    private final SignupRequestService signupRequestService;

    public AdminSignupController(SignupRequestService signupRequestService) {
        this.signupRequestService = signupRequestService;
    }

    @GetMapping
    public List<SignupRequestResponse> pending() {
        return signupRequestService.pending();
    }

    @PostMapping("/{id}/approve")
    public SignupRequestResponse approve(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt,
                                         HttpServletRequest http) {
        return signupRequestService.approve(id, UUID.fromString(jwt.getSubject()), http.getRemoteAddr());
    }

    @PostMapping("/{id}/reject")
    public SignupRequestResponse reject(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt,
                                        HttpServletRequest http) {
        return signupRequestService.reject(id, UUID.fromString(jwt.getSubject()), http.getRemoteAddr());
    }
}
