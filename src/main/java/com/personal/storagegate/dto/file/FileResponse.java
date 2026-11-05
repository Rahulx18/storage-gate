package com.personal.storagegate.dto.file;

import com.personal.storagegate.entity.StorageFile;

import java.time.Instant;
import java.util.UUID;

public record FileResponse(UUID id, String name, String contentType, long sizeBytes, Instant createdAt) {

    public static FileResponse from(StorageFile file) {
        return new FileResponse(file.getId(), file.getName(), file.getContentType(), file.getSizeBytes(),
                file.getCreatedAt());
    }
}
