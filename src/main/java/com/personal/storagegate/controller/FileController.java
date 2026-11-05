package com.personal.storagegate.controller;

import com.personal.storagegate.dto.file.FileResponse;
import com.personal.storagegate.entity.StorageFile;
import com.personal.storagegate.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public FileResponse upload(@RequestPart("file") MultipartFile file, @AuthenticationPrincipal Jwt jwt) {
        return FileResponse.from(fileService.upload(userId(jwt), file));
    }

    @GetMapping
    public List<FileResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return fileService.list(userId(jwt)).stream().map(FileResponse::from).toList();
    }

    @GetMapping("/{id}")
    public void download(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt, HttpServletResponse response)
            throws IOException {
        StorageFile file = fileService.find(id, userId(jwt), isAdmin(jwt));
        response.setContentType(file.getContentType());
        response.setContentLengthLong(file.getSizeBytes());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(file.getName(), StandardCharsets.UTF_8).build().toString());
        fileService.stream(file, response.getOutputStream());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        fileService.delete(fileService.find(id, userId(jwt), isAdmin(jwt)));
    }

    private static UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    private static boolean isAdmin(Jwt jwt) {
        return "ADMIN".equals(jwt.getClaimAsString("permission"));
    }
}
