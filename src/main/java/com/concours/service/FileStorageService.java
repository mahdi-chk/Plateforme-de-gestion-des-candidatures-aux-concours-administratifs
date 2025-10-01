package com.concours.service;

import com.concours.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import java.net.MalformedURLException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FileStorageService {

    @Value("${file.upload.dir}")
    private String uploadDir;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");


    public String storeFile(MultipartFile file, String category) {
        try {
            if (file.isEmpty()) {
                throw new BusinessException("Le fichier est vide");
            }

            // Validation du type de fichier
            if (!"application/pdf".equals(file.getContentType())) {
                throw new BusinessException("Seuls les fichiers PDF sont acceptés");
            }

            // Validation de la taille (10MB max)
            long maxSize = 10 * 1024 * 1024; // 10MB
            if (file.getSize() > maxSize) {
                throw new BusinessException("Le fichier dépasse la taille maximale de 10MB");
            }

            // Création du répertoire basé sur la date
            String dateFolder = LocalDateTime.now().format(dateFormatter);
            Path categoryPath = Paths.get(uploadDir, category, dateFolder);
            Files.createDirectories(categoryPath);

            // Génération d'un nom unique pour le fichier
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String uniqueFilename = UUID.randomUUID().toString() + extension;
            Path filePath = categoryPath.resolve(uniqueFilename);

            // Sauvegarde du fichier
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Retour du chemin relatif
            String relativePath = Paths.get(category, dateFolder, uniqueFilename).toString();
            log.info("Fichier sauvegardé: {}", relativePath);

            return relativePath;

        } catch (IOException e) {
            log.error("Erreur lors de la sauvegarde du fichier", e);
            throw new BusinessException("Erreur lors de la sauvegarde du fichier: " + e.getMessage());
        }
    }

    public Path getFilePath(String relativePath) {
        return Paths.get(uploadDir).resolve(relativePath);
    }

    public boolean deleteFile(String relativePath) {
        try {
            Path filePath = getFilePath(relativePath);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Erreur lors de la suppression du fichier: {}", relativePath, e);
            return false;
        }
    }

    public boolean fileExists(String relativePath) {
        Path filePath = getFilePath(relativePath);
        return Files.exists(filePath);
    }

    public Resource loadFileAsResource(String filePath) throws IOException {
        try {
            Path file = Paths.get(uploadDir).resolve(filePath).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new IOException("Fichier non trouvé: " + filePath);
            }
        } catch (MalformedURLException e) {
            throw new IOException("Fichier non trouvé: " + filePath, e);
        }
    }
}