package com.personal.storagegate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.io.OutputStream;
import java.net.URI;
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

    public String upload(String accessToken, String name, String contentType, Resource content) {
        URI session = restClient.post()
                .uri("https://www.googleapis.com/upload/drive/v3/files?uploadType=resumable")
                .header("Authorization", "Bearer " + accessToken)
                .header("X-Upload-Content-Type", contentType)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("name", name))
                .retrieve()
                .toBodilessEntity()
                .getHeaders()
                .getLocation();
        if (session == null) {
            throw new IllegalStateException("drive didn't return an upload session");
        }

        Map<String, Object> created = restClient.put()
                .uri(session)
                .contentType(MediaType.parseMediaType(contentType))
                .body(content)
                .retrieve()
                .body(Map.class);
        if (created == null || created.get("id") == null) {
            throw new IllegalStateException("drive didn't return a file id");
        }
        return created.get("id").toString();
    }

    public void download(String accessToken, String fileId, OutputStream out) {
        restClient.get()
                .uri("https://www.googleapis.com/drive/v3/files/{id}?alt=media", fileId)
                .header("Authorization", "Bearer " + accessToken)
                .exchange((request, response) -> {
                    if (response.getStatusCode().isError()) {
                        throw new IllegalStateException("drive download failed: " + response.getStatusCode());
                    }
                    response.getBody().transferTo(out);
                    return null;
                });
    }

    public void delete(String accessToken, String fileId) {
        try {
            restClient.delete()
                    .uri("https://www.googleapis.com/drive/v3/files/{id}", fileId)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound ignored) {
            log.warn("drive file {} already gone", fileId);
        }
    }
}
