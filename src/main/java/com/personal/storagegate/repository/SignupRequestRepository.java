package com.personal.storagegate.repository;

import com.personal.storagegate.entity.SignupRequest;
import com.personal.storagegate.entity.SignupStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SignupRequestRepository extends JpaRepository<SignupRequest, UUID> {

    Optional<SignupRequest> findByEmail(String email);

    List<SignupRequest> findAllByStatusOrderByCreatedAtAsc(SignupStatus status);
}
