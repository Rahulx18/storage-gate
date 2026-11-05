package com.personal.storagegate.repository;

import com.personal.storagegate.entity.StorageFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StorageFileRepository extends JpaRepository<StorageFile, UUID> {

    List<StorageFile> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

    boolean existsByOwnerId(UUID ownerId);

    boolean existsByAccountIdAndOwnerIdNot(UUID accountId, UUID ownerId);

    @Query("select coalesce(sum(f.sizeBytes), 0) from StorageFile f where f.accountId = :accountId")
    long sumSizeByAccount(@Param("accountId") UUID accountId);
}
