package com.personal.storagegate.service;

import com.personal.storagegate.entity.StorageAccount;
import org.springframework.core.io.Resource;

import java.io.OutputStream;

public interface StorageBackend {

    String upload(StorageAccount account, String name, String contentType, Resource content);

    void download(StorageAccount account, String remoteId, OutputStream out);

    void delete(StorageAccount account, String remoteId);
}
