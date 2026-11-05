package com.personal.storagegate.controller;

import com.personal.storagegate.dto.user.PermissionRequest;
import com.personal.storagegate.dto.user.UserResponse;
import com.personal.storagegate.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return userService.get(UUID.fromString(jwt.getSubject()));
    }

    @GetMapping
    public List<UserResponse> list() {
        return userService.list();
    }

    @PatchMapping("/{id}/permission")
    public UserResponse setPermission(@PathVariable UUID id, @RequestBody PermissionRequest request,
                                      @AuthenticationPrincipal Jwt jwt) {
        return userService.setPermission(id, request.permission(), UUID.fromString(jwt.getSubject()));
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMe(@AuthenticationPrincipal Jwt jwt) {
        userService.deleteAccount(UUID.fromString(jwt.getSubject()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        userService.delete(id, UUID.fromString(jwt.getSubject()));
    }
}
