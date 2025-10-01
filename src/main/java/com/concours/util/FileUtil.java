package com.concours.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

public final class FileUtil {

    private FileUtil() {
        // Classe utilitaire - constructeur privé
    }

    public static boolean isValidFileType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return false;
        }

        String extension = getFileExtension(fileName);
        return Arrays.asList(ApplicationConstants.ALLOWED_FILE_EXTENSIONS)
                .contains(extension.toLowerCase());
    }

    public static boolean isValidFileSize(MultipartFile file) {
        return file != null && file.getSize() <= ApplicationConstants.MAX_FILE_SIZE;
    }

    public static String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    public static String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        return System.currentTimeMillis() + "_" +
                originalFileName.replaceAll("[^a-zA-Z0-9.]", "_") +
                extension;
    }
}