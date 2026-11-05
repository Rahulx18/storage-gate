package com.personal.storagegate.dto.user;

import com.personal.storagegate.entity.AppUser;
import com.personal.storagegate.entity.Permission;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(UUID id, String email, Permission permission, long quotaBytes, long usedBytes,
                           Instant createdAt) {

    public static UserResponse from(AppUser user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getPermission(), user.getQuotaBytes(),
                user.getUsedBytes(), user.getCreatedAt());
    }
}
