package com.concours.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileUploadService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String uploadFile(MultipartFile file, String subdirectory) throws IOException {
        // Validation du fichier
        validateFile(file);

        // Création du répertoire s'il n'existe pas
        Path uploadPath = Paths.get(uploadDir, subdirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Génération d'un nom de fichier unique
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        // Sauvegarde du fichier
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Le fichier est vide");
        }

        // Vérification de la taille (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException("La taille du fichier dépasse 10MB");
        }

        // Vérification des extensions autorisées
        String filename = file.getOriginalFilename();
        String[] allowedExtensions = {".pdf", ".jpg", ".jpeg", ".png"};
        boolean validExtension = false;

        for (String ext : allowedExtensions) {
            if (filename.toLowerCase().endsWith(ext)) {
                validExtension = true;
                break;
            }
        }

        if (!validExtension) {
            throw new RuntimeException("Type de fichier non autorisé. Formats acceptés: PDF, JPG, JPEG, PNG");
        }
    }

    public void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.deleteIfExists(path);
    }
}