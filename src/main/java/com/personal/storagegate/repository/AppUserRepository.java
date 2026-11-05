package com.personal.storagegate.repository;

import com.personal.storagegate.entity.AppUser;
import com.personal.storagegate.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);

    List<AppUser> findAllByPermission(Permission permission);

    @Transactional
    @Modifying
    @Query("update AppUser u set u.usedBytes = u.usedBytes + :bytes "
            + "where u.id = :id and u.usedBytes + :bytes <= u.quotaBytes")
    int reserveQuota(@Param("id") UUID id, @Param("bytes") long bytes);

    @Transactional
    @Modifying
    @Query("update AppUser u set u.usedBytes = case when u.usedBytes > :bytes then u.usedBytes - :bytes else 0L end "
            + "where u.id = :id")
    int releaseQuota(@Param("id") UUID id, @Param("bytes") long bytes);
}
