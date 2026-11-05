package com.personal.storagegate.service;

import com.personal.storagegate.dto.user.UserResponse;
import com.personal.storagegate.entity.AppUser;
import com.personal.storagegate.entity.Permission;
import com.personal.storagegate.entity.StorageAccount;
import com.personal.storagegate.repository.AppUserRepository;
import com.personal.storagegate.repository.StorageAccountRepository;
import com.personal.storagegate.repository.StorageFileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final AppUserRepository userRepository;
    private final StorageAccountRepository accountRepository;
    private final StorageFileRepository fileRepository;
    private final FileService fileService;

    public UserService(AppUserRepository userRepository, StorageAccountRepository accountRepository,
                       StorageFileRepository fileRepository, FileService fileService) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.fileRepository = fileRepository;
        this.fileService = fileService;
    }

    public UserResponse get(UUID id) {
        return UserResponse.from(find(id));
    }

    public List<UserResponse> list() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional
    public UserResponse setPermission(UUID id, Permission permission, UUID actorId) {
        if (permission == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "permission required");
        }
        rejectSelf(id, actorId);
        AppUser user = find(id);
        user.setPermission(permission);
        return UserResponse.from(user);
    }

    public void delete(UUID id, UUID actorId) {
        rejectSelf(id, actorId);
        deleteAccount(id);
    }

    public void deleteAccount(UUID id) {
        AppUser user = find(id);
        if (user.getPermission() == Permission.ADMIN
                && userRepository.findAllByPermission(Permission.ADMIN).size() <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "cannot delete the last admin");
        }
        List<StorageAccount> owned = accountRepository.findAllByOwnerUserId(id);
        for (StorageAccount account : owned) {
            if (fileRepository.existsByAccountIdAndOwnerIdNot(account.getId(), id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "a linked drive still stores other users' files");
            }
        }
        fileService.list(id).forEach(fileService::delete);
        accountRepository.deleteAll(owned);
        userRepository.delete(user);
    }

    private AppUser find(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no such user"));
    }

    private void rejectSelf(UUID id, UUID actorId) {
        if (id.equals(actorId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cannot modify your own account");
        }
    }
}
