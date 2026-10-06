package com.personal.storagegate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
public class GoogleDriveClient {

    private final RestClient restClient = RestClient.create();

    public Map<String, Object> getAbout(String accessToken) {
        return restClient.get()
                .uri("https://www.googleapis.com/drive/v3/about?fields=user,storageQuota")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);
    }

    public String createTestFile(String accessToken) {
        String name = "storage-gate-test-" + Instant.now().getEpochSecond() + ".txt";

        Map<String, Object> created = restClient.post()
                .uri("https://www.googleapis.com/drive/v3/files")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("name", name))
                .retrieve()
                .body(Map.class);

        if (created == null || created.get("id") == null) {
            throw new IllegalStateException("drive didn't return a file id");
        }
        String fileId = created.get("id").toString();

        restClient.patch()
                .uri("https://www.googleapis.com/upload/drive/v3/files/{id}?uploadType=media", fileId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.TEXT_PLAIN)
                .body("storage-gate connectivity test")
                .retrieve()
                .toBodilessEntity();

        return fileId;
    }
}
