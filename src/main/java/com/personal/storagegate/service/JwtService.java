package com.personal.storagegate.service;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.personal.storagegate.entity.AppUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Base64;

@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final long ttlSeconds;

    public JwtService(@Value("${jwt.secret}") String base64Secret,
                      @Value("${jwt.ttl-seconds:900}") long ttlSeconds) {
        byte[] raw = Base64.getDecoder().decode(base64Secret.trim());
        if (raw.length < 32) {
            throw new IllegalStateException("jwt secret must be at least 32 bytes, got " + raw.length);
        }
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(new SecretKeySpec(raw, "HmacSHA256")));
        this.ttlSeconds = ttlSeconds;
    }

    public String issue(AppUser user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getId().toString())
                .claim("permission", user.getPermission().name())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(ttlSeconds))
                .build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }
}
