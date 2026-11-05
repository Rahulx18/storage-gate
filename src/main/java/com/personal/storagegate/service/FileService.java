package com.personal.storagegate.service;

import com.personal.storagegate.entity.StorageAccount;
import com.personal.storagegate.entity.StorageAccountStatus;
import com.personal.storagegate.entity.StorageFile;
import com.personal.storagegate.repository.AppUserRepository;
import com.personal.storagegate.repository.StorageAccountRepository;
import com.personal.storagegate.repository.StorageFileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.OutputStream;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    private final StorageFileRepository fileRepository;
    private final StorageAccountRepository accountRepository;
    private final AppUserRepository userRepository;
    private final StorageBackend backend;

    public FileService(StorageFileRepository fileRepository, StorageAccountRepository accountRepository,
                       AppUserRepository userRepository, StorageBackend backend) {
        this.fileRepository = fileRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.backend = backend;
    }

    public StorageFile upload(UUID ownerId, MultipartFile file) {
        long size = file.getSize();
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "empty file");
        }
        if (userRepository.reserveQuota(ownerId, size) == 0) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(413), "quota exceeded");
        }
        try {
            StorageAccount account = pickAccount();
            String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
            String remoteId = backend.upload(account, fileName(file), contentType, file.getResource());

            StorageFile stored = new StorageFile();
            stored.setOwnerId(ownerId);
            stored.setAccountId(account.getId());
            stored.setRemoteId(remoteId);
            stored.setName(fileName(file));
            stored.setContentType(contentType);
            stored.setSizeBytes(size);
            return fileRepository.save(stored);
        } catch (RuntimeException e) {
            userRepository.releaseQuota(ownerId, size);
            throw e;
        }
    }

    public List<StorageFile> list(UUID ownerId) {
        return fileRepository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId);
    }

    public StorageFile find(UUID id, UUID userId, boolean admin) {
        return fileRepository.findById(id)
                .filter(f -> admin || f.getOwnerId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no such file"));
    }

    public void stream(StorageFile file, OutputStream out) {
        backend.download(account(file), file.getRemoteId(), out);
    }

    public void delete(StorageFile file) {
        backend.delete(account(file), file.getRemoteId());
        fileRepository.delete(file);
        userRepository.releaseQuota(file.getOwnerId(), file.getSizeBytes());
    }

    private StorageAccount account(StorageFile file) {
        return accountRepository.findById(file.getAccountId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "storage account missing"));
    }

    private StorageAccount pickAccount() {
        return accountRepository.findAllByStatus(StorageAccountStatus.ACTIVE).stream()
                .min(Comparator.comparingLong(a -> fileRepository.sumSizeByAccount(a.getId())))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "no storage account available"));
    }

    private String fileName(MultipartFile file) {
        String name = StringUtils.getFilename(StringUtils.cleanPath(file.getOriginalFilename() == null
                ? "" : file.getOriginalFilename()));
        if (name == null || name.isBlank()) {
            return "file";
        }
        return name.length() > 255 ? name.substring(0, 255) : name;
    }
}
