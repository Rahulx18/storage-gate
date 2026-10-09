package com.personal.storagegate.repository;

import com.personal.storagegate.entity.StorageAccount;
import com.personal.storagegate.entity.StorageAccountStatus;
import com.personal.storagegate.entity.StorageDestination;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StorageAccountRepository extends JpaRepository<StorageAccount, UUID> {

    Optional<StorageAccount> findByDestinationAndEmail(StorageDestination destination, String email);

    boolean existsByDestinationAndEmail(StorageDestination destination, String email);

    List<StorageAccount> findAllByStatus(StorageAccountStatus status);
}
