package com.personal.storagegate.controller;

import com.personal.storagegate.dto.google.GoogleTokenResponse;
import com.personal.storagegate.entity.StorageAccount;
import com.personal.storagegate.repository.StorageAccountRepository;
import com.personal.storagegate.service.GoogleDriveClient;
import com.personal.storagegate.service.GoogleOAuthClient;
import com.personal.storagegate.service.StorageAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@RestController
public class DriveAccountController {

    private final GoogleOAuthClient oAuthClient;
    private final GoogleDriveClient driveClient;
    private final StorageAccountService accountService;
    private final StorageAccountRepository accountRepository;

    public DriveAccountController(GoogleOAuthClient oAuthClient,
                                   GoogleDriveClient driveClient,
                                   StorageAccountService accountService,
                                   StorageAccountRepository accountRepository) {
        this.oAuthClient = oAuthClient;
        this.driveClient = driveClient;
        this.accountService = accountService;
        this.accountRepository = accountRepository;
    }

    @GetMapping("/drive-accounts/authorize")
    public RedirectView authorize() {
        return new RedirectView(oAuthClient.buildAuthorizationUrl());
    }

    @GetMapping("/drive-accounts/callback")
    public StorageAccount callback(@RequestParam String code, @RequestParam String state) {
        if (!oAuthClient.consumeState(state)) {
            throw new IllegalArgumentException("invalid or expired state, restart from /drive-accounts/authorize");
        }
        GoogleTokenResponse tokens = oAuthClient.exchangeCode(code);
        if (tokens.scope() == null || !tokens.scope().contains(GoogleOAuthClient.DRIVE_SCOPE)) {
            throw new IllegalStateException("drive.file scope was not granted");
        }
        if (tokens.refresh_token() == null) {
            throw new IllegalStateException(
                    "no refresh_token, revoke access at https://myaccount.google.com/permissions and try again");
        }
        String email = oAuthClient.fetchEmail(tokens.access_token());
        log.info("Connected Drive account: {}", email);
        return accountService.saveGoogleDriveAccount(email, tokens.refresh_token());
    }

    @GetMapping("/drive-accounts/{id}/about")
    public Map<String, Object> about(@PathVariable UUID id) {
        String accessToken = accessTokenFor(id);
        return driveClient.getAbout(accessToken);
    }

    @PostMapping("/drive-accounts/{id}/test-file")
    public Map<String, String> createTestFile(@PathVariable UUID id) {
        String accessToken = accessTokenFor(id);
        String fileId = driveClient.createTestFile(accessToken);
        return Map.of("fileId", fileId);
    }

    private String accessTokenFor(UUID id) {
        StorageAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("no account with id " + id));
        return oAuthClient.refreshAccessToken(account.getRefreshToken());
    }
}
