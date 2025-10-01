package com.concours.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

public final class ApplicationConstants {

    private ApplicationConstants() {
        // Classe utilitaire - constructeur privé
    }

    // Formats de date
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String DATETIME_FORMAT = "dd/MM/yyyy HH:mm";

    // Tailles de fichiers
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    // Extensions autorisées
    public static final String[] ALLOWED_FILE_EXTENSIONS = {".pdf", ".doc", ".docx", ".jpg", ".jpeg", ".png"};

    // Messages
    public static final String MESSAGE_CANDIDATURE_SOUMISE = "Votre candidature a été soumise avec succès";
    public static final String MESSAGE_CANDIDATURE_VALIDEE = "Votre candidature a été validée";
    public static final String MESSAGE_CANDIDATURE_REJETEE = "Votre candidature a été rejetée";

    // Rôles
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_GESTIONNAIRE_GLOBAL = "ROLE_GESTIONNAIRE_GLOBAL";
    public static final String ROLE_GESTIONNAIRE_LOCAL = "ROLE_GESTIONNAIRE_LOCAL";
    public static final String ROLE_CANDIDAT = "ROLE_CANDIDAT";
}