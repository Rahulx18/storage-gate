package com.personal.storagegate.service;

import com.personal.storagegate.entity.AppUser;
import com.personal.storagegate.entity.Permission;
import com.personal.storagegate.entity.SignupRequest;
import com.personal.storagegate.entity.SignupStatus;
import com.personal.storagegate.repository.AppUserRepository;
import com.personal.storagegate.repository.SignupRequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@EnableScheduling
public class SignupApprovalJob {

    private final SignupRequestRepository requestRepository;
    private final AppUserRepository userRepository;
    private final MailService mailService;

    public SignupApprovalJob(SignupRequestRepository requestRepository, AppUserRepository userRepository,
                             MailService mailService) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.mailService = mailService;
    }

    @Scheduled(initialDelay = 10_000, fixedDelayString = "${signup.approval-poll-ms:60000}")
    @Transactional
    public void provisionApproved() {
        for (SignupRequest request : requestRepository.findAllByStatusAndProvisionedAtIsNull(SignupStatus.APPROVED)) {
            if (!userRepository.existsByUsername(request.getEmail())) {
                AppUser user = new AppUser();
                user.setUsername(request.getEmail());
                user.setPasswordHash(request.getPasswordHash());
                user.setPermission(request.getPermission() != null ? request.getPermission() : Permission.EDITOR);
                userRepository.save(user);
                log.info("Provisioned {} as {}", user.getUsername(), user.getPermission());
                mailService.sendApproved(user.getUsername());
            }
            request.setProvisionedAt(Instant.now());
        }
    }
}
