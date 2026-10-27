package com.personal.storagegate.dto.user;

import com.personal.storagegate.entity.SignupRequest;
import com.personal.storagegate.entity.SignupStatus;

import java.time.Instant;
import java.util.UUID;

public record SignupRequestResponse(UUID id, String email, SignupStatus status, String requestedIp, Instant createdAt) {

    public static SignupRequestResponse from(SignupRequest request) {
        return new SignupRequestResponse(request.getId(), request.getEmail(), request.getStatus(),
                request.getRequestedIp(), request.getCreatedAt());
    }
}
