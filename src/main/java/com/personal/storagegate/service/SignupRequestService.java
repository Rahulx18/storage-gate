package com.personal.storagegate.service;

import com.personal.storagegate.dto.user.SignupRequestResponse;
import com.personal.storagegate.entity.AppUser;
import com.personal.storagegate.entity.Permission;
import com.personal.storagegate.entity.SignupRequest;
import com.personal.storagegate.entity.SignupStatus;
import com.personal.storagegate.repository.AppUserRepository;
import com.personal.storagegate.repository.SignupRequestRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class SignupRequestService {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int MAX_EMAIL_LENGTH = 64;

    private final SignupRequestRepository requestRepository;
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public SignupRequestService(SignupRequestRepository requestRepository, AppUserRepository userRepository,
                                PasswordEncoder passwordEncoder, MailService mailService) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    public SignupRequestResponse signup(String rawEmail, String password, String ip) {
        String email = rawEmail == null ? "" : rawEmail.trim().toLowerCase();
        if (email.length() > MAX_EMAIL_LENGTH || !EMAIL.matcher(email).matches()
                || password == null || password.length() < 8 || password.length() > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "email must be valid (max 64 chars), password 8-72 chars");
        }
        if (userRepository.existsByUsername(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "account already exists");
        }
        requestRepository.findByEmail(email).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "signup request already exists (" + existing.getStatus() + ")");
        });

        SignupRequest request = new SignupRequest();
        request.setEmail(email);
        request.setPasswordHash(passwordEncoder.encode(password));
        request.setStatus(SignupStatus.PENDING);
        request.setRequestedIp(ip);
        try {
            requestRepository.saveAndFlush(request);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "signup request already exists");
        }
        mailService.notifyAdmins(request);
        return SignupRequestResponse.from(request);
    }

    public List<SignupRequestResponse> pending() {
        return requestRepository.findAllByStatusOrderByCreatedAtAsc(SignupStatus.PENDING).stream()
                .map(SignupRequestResponse::from)
                .toList();
    }

    @Transactional
    public SignupRequestResponse approve(UUID id, UUID adminId, String ip) {
        SignupRequest request = decide(id, SignupStatus.APPROVED, adminId, ip);

        AppUser user = new AppUser();
        user.setUsername(request.getEmail());
        user.setPasswordHash(request.getPasswordHash());
        user.setPermission(Permission.EDITOR);
        userRepository.save(user);

        mailService.sendApproved(request.getEmail());
        return SignupRequestResponse.from(request);
    }

    @Transactional
    public SignupRequestResponse reject(UUID id, UUID adminId, String ip) {
        return SignupRequestResponse.from(decide(id, SignupStatus.REJECTED, adminId, ip));
    }

    private SignupRequest decide(UUID id, SignupStatus status, UUID adminId, String ip) {
        SignupRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no such signup request"));
        if (request.getStatus() != SignupStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "request already " + request.getStatus());
        }
        request.setStatus(status);
        request.setDecidedBy(adminId);
        request.setDecidedIp(ip);
        request.setDecidedAt(Instant.now());
        return request;
    }
}
