package com.personal.storagegate.service;

import com.personal.storagegate.entity.StorageAccount;
import com.personal.storagegate.entity.StorageAccountStatus;
import com.personal.storagegate.entity.StorageDestination;
import com.personal.storagegate.repository.StorageAccountRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StorageAccountService {

    private final StorageAccountRepository repository;

    public StorageAccountService(StorageAccountRepository repository) {
        this.repository = repository;
    }

    public StorageAccount saveGoogleDriveAccount(String email, String refreshToken, UUID ownerId) {
        StorageAccount account = repository
                .findByDestinationAndEmail(StorageDestination.GOOGLE_DRIVE, email)
                .orElseGet(StorageAccount::new);

        String displayName = email.split("@")[0];
        account.setDestination(StorageDestination.GOOGLE_DRIVE);
        account.setEmail(email);
        account.setDisplayName(displayName);
        account.setRefreshToken(refreshToken);
        if (account.getOwnerUserId() == null) {
            account.setOwnerUserId(ownerId);
        }
        return repository.save(account);
    }
}
