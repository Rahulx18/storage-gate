package com.personal.storagegate.service;

import com.personal.storagegate.entity.StorageAccount;
import com.personal.storagegate.entity.StorageDestination;
import com.personal.storagegate.repository.StorageAccountRepository;
import org.springframework.stereotype.Service;

@Service
public class StorageAccountService {

    private final StorageAccountRepository repository;

    public StorageAccountService(StorageAccountRepository repository) {
        this.repository = repository;
    }

    public StorageAccount saveGoogleDriveAccount(String email, String refreshToken) {
        StorageAccount account = repository
                .findByDestinationAndEmail(StorageDestination.GOOGLE_DRIVE, email)
                .orElseGet(StorageAccount::new);

        account.setDestination(StorageDestination.GOOGLE_DRIVE);
        account.setEmail(email);
        account.setRefreshToken(refreshToken);
        return repository.save(account);
    }
}
