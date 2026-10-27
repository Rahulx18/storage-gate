package com.personal.storagegate.service;

import com.personal.storagegate.dto.google.GoogleTokenResponse;
import com.personal.storagegate.dto.google.GoogleUserInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class GoogleOAuthClient {

    public static final String DRIVE_SCOPE = "https://www.googleapis.com/auth/drive.file";
    private static final String SCOPES = "openid email " + DRIVE_SCOPE;
    private static final long STATE_TTL_SECONDS = 600;

    private final Map<String, Instant> states = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    private final RestClient restClient = RestClient.create();

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    public String buildAuthorizationUrl() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        states.put(state, Instant.now().plusSeconds(STATE_TTL_SECONDS));

        return UriComponentsBuilder.fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", SCOPES)
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    public boolean consumeState(String state) {
        states.values().removeIf(exp -> exp.isBefore(Instant.now()));
        return state != null && states.remove(state) != null;
    }

    public GoogleTokenResponse exchangeCode(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("redirect_uri", redirectUri);
        form.add("grant_type", "authorization_code");

        return restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .body(form)
                .retrieve()
                .body(GoogleTokenResponse.class);
    }

    public String refreshAccessToken(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("refresh_token", refreshToken);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("grant_type", "refresh_token");

        GoogleTokenResponse response = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .body(form)
                .retrieve()
                .body(GoogleTokenResponse.class);

        if (response == null) {
            throw new IllegalStateException("no token response from google");
        }
        return response.access_token();
    }

    public String fetchEmail(String accessToken) {
        GoogleUserInfoResponse info = restClient.get()
                .uri("https://openidconnect.googleapis.com/v1/userinfo")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(GoogleUserInfoResponse.class);

        if (info == null) {
            throw new IllegalStateException("no userinfo response from google");
        }
        return info.email();
    }
}
