package com.personal.storagegate.service;

import com.personal.storagegate.entity.StorageAccount;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.OutputStream;

@Service
public class GoogleDriveBackend implements StorageBackend {

    private final GoogleDriveClient driveClient;
    private final GoogleOAuthClient oAuthClient;

    public GoogleDriveBackend(GoogleDriveClient driveClient, GoogleOAuthClient oAuthClient) {
        this.driveClient = driveClient;
        this.oAuthClient = oAuthClient;
    }

    @Override
    public String upload(StorageAccount account, String name, String contentType, Resource content) {
        return driveClient.upload(accessToken(account), name, contentType, content);
    }

    @Override
    public void download(StorageAccount account, String remoteId, OutputStream out) {
        driveClient.download(accessToken(account), remoteId, out);
    }

    @Override
    public void delete(StorageAccount account, String remoteId) {
        driveClient.delete(accessToken(account), remoteId);
    }

    private String accessToken(StorageAccount account) {
        return oAuthClient.refreshAccessToken(account.getRefreshToken());
    }
}
