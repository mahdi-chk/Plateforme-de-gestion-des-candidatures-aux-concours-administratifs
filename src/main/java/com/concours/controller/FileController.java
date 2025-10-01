package com.concours.controller;

import com.concours.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("subDirectory") String subDirectory) {
        String filePath = fileStorageService.storeFile(file, subDirectory);
        return ResponseEntity.ok(filePath);
    }

    @GetMapping("/download/{subDirectory}/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String subDirectory, @PathVariable String filename) {
        try {
            Resource resource = fileStorageService.loadFileAsResource(subDirectory + "/" + filename);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}