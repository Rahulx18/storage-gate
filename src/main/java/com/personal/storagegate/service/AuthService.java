package com.personal.storagegate.service;

import com.personal.storagegate.entity.AppUser;
import com.personal.storagegate.entity.Permission;
import com.personal.storagegate.repository.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AppUserRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public String signup(String username, String password) {
        String name = normalize(username);
        if (name.length() < 3 || name.length() > 64 || password == null
                || password.length() < 8 || password.length() > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "username must be 3-64 chars, password 8-72 chars");
        }
        if (repository.existsByUsername(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "username taken");
        }

        AppUser user = new AppUser();
        user.setUsername(name);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setPermission(Permission.EDITOR);
        return jwtService.issue(repository.save(user));
    }

    public String login(String username, String password) {
        AppUser user = repository.findByUsername(normalize(username))
                .filter(u -> password != null && passwordEncoder.matches(password, u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials"));
        return jwtService.issue(user);
    }

    private static String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }
}
