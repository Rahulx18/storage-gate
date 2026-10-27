package com.personal.storagegate.dto.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoogleTokenResponse(
        String access_token,
        String refresh_token,
        Integer expires_in,
        String scope,
        String token_type
) {
}
