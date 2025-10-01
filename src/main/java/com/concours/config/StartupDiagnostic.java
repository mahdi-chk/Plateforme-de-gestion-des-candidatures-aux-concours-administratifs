package com.concours.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;

/**
 * Diagnostic de démarrage pour vérifier la configuration
 */
@Component
@Slf4j
public class StartupDiagnostic implements ApplicationListener<ApplicationReadyEvent> {

    private final Environment environment;
    private final DataSource dataSource;

    @Value("${file.upload.dir:uploads/}")
    private String uploadDir;

    @Value("${app.document.max-size:104857600}")
    private long maxFileSize;

    public StartupDiagnostic(Environment environment, DataSource dataSource) {
        this.environment = environment;
        this.dataSource = dataSource;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("=== DIAGNOSTIC DE DÉMARRAGE ===");

        checkConfiguration();
        checkDatabase();
        checkFileSystem();
        checkUploadConfiguration();

        log.info("=== FIN DU DIAGNOSTIC ===");
    }

    private void checkConfiguration() {
        log.info("--- Vérification Configuration ---");

        // Vérifier les profils actifs
        String[] activeProfiles = environment.getActiveProfiles();
        log.info("Profils actifs: {}", activeProfiles.length > 0 ?
                String.join(", ", activeProfiles) : "aucun");

        // Vérifier les propriétés critiques
        checkProperty("spring.datasource.url");
        checkProperty("spring.datasource.username");
        checkProperty("file.upload.dir");
        checkProperty("app.document.max-size");
        checkProperty("spring.servlet.multipart.max-file-size");
    }

    private void checkProperty(String propertyName) {
        String value = environment.getProperty(propertyName);
        if (value != null && !value.isEmpty()) {
            // Masquer les mots de passe
            if (propertyName.contains("password")) {
                log.info("✓ {}: [MASQUÉ]", propertyName);
            } else {
                log.info("✓ {}: {}", propertyName, value);
            }
        } else {
            log.warn("✗ Propriété manquante ou vide: {}", propertyName);
        }
    }

    private void checkDatabase() {
        log.info("--- Vérification Base de Données ---");

        try (Connection connection = dataSource.getConnection()) {
            String url = connection.getMetaData().getURL();
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            String databaseProductVersion = connection.getMetaData().getDatabaseProductVersion();

            log.info("✓ Connexion BDD réussie");
            log.info("  URL: {}", url);
            log.info("  Type: {} {}", databaseProductName, databaseProductVersion);

            // Vérifier la configuration MySQL pour les BLOB
            if ("MySQL".equals(databaseProductName)) {
                checkMySQLConfiguration(connection);
            }

        } catch (Exception e) {
            log.error("✗ Erreur de connexion à la base de données: {}", e.getMessage());
        }
    }

    private void checkMySQLConfiguration(Connection connection) {
        try {
            // Vérifier max_allowed_packet
            var stmt = connection.createStatement();
            var rs = stmt.executeQuery("SHOW VARIABLES LIKE 'max_allowed_packet'");

            if (rs.next()) {
                long maxPacket = rs.getLong("Value");
                long requiredSize = 100 * 1024 * 1024; // 100MB

                if (maxPacket >= requiredSize) {
                    log.info("✓ max_allowed_packet: {}MB", maxPacket / 1024 / 1024);
                } else {
                    log.warn("⚠ max_allowed_packet trop petit: {}MB (recommandé: {}MB)",
                            maxPacket / 1024 / 1024, requiredSize / 1024 / 1024);
                }
            }

            // Vérifier innodb_buffer_pool_size
            rs = stmt.executeQuery("SHOW VARIABLES LIKE 'innodb_buffer_pool_size'");
            if (rs.next()) {
                long bufferPoolSize = rs.getLong("Value");
                log.info("✓ innodb_buffer_pool_size: {}MB", bufferPoolSize / 1024 / 1024);

                if (bufferPoolSize < 512 * 1024 * 1024) { // 512MB
                    log.warn("⚠ innodb_buffer_pool_size petit pour des BLOB: {}MB",
                            bufferPoolSize / 1024 / 1024);
                }
            }

        } catch (Exception e) {
            log.warn("Impossible de vérifier la configuration MySQL: {}", e.getMessage());
        }
    }

    private void checkFileSystem() {
        log.info("--- Vérification Système de Fichiers ---");

        try {
            Path uploadPath = Paths.get(uploadDir);

            if (Files.exists(uploadPath)) {
                log.info("✓ Répertoire d'upload existe: {}", uploadPath.toAbsolutePath());

                if (Files.isWritable(uploadPath)) {
                    log.info("✓ Répertoire d'upload accessible en écriture");
                } else {
                    log.warn("⚠ Répertoire d'upload non accessible en écriture");
                }

                // Compter les fichiers existants
                long fileCount = Files.walk(uploadPath)
                        .filter(Files::isRegularFile)
                        .count();

                if (fileCount > 0) {
                    log.info("ℹ {} fichiers trouvés dans l'ancien système", fileCount);
                }

            } else {
                log.info("ℹ Répertoire d'upload n'existe pas encore: {}", uploadPath.toAbsolutePath());
                log.info("  Il sera créé automatiquement si nécessaire");
            }

        } catch (Exception e) {
            log.error("✗ Erreur lors de la vérification du système de fichiers: {}", e.getMessage());
        }
    }

    private void checkUploadConfiguration() {
        log.info("--- Vérification Configuration Upload ---");

        // Vérifier les limites de taille
        String maxFileSize = environment.getProperty("spring.servlet.multipart.max-file-size");
        String maxRequestSize = environment.getProperty("spring.servlet.multipart.max-request-size");

        log.info("✓ Taille max fichier: {}", maxFileSize != null ? maxFileSize : "par défaut");
        log.info("✓ Taille max requête: {}", maxRequestSize != null ? maxRequestSize : "par défaut");
        log.info("✓ Taille max document (app): {}MB", this.maxFileSize / 1024 / 1024);

        // Vérifier la cohérence
        if (maxFileSize != null && maxFileSize.contains("MB")) {
            try {
                int fileSizeMB = Integer.parseInt(maxFileSize.replace("MB", ""));
                int documentSizeMB = (int) (this.maxFileSize / 1024 / 1024);

                if (fileSizeMB < documentSizeMB) {
                    log.warn("⚠ Incohérence: spring.servlet.multipart.max-file-size ({}) < app.document.max-size ({})",
                            fileSizeMB + "MB", documentSizeMB + "MB");
                }
            } catch (Exception e) {
                // Ignorer les erreurs de parsing
            }
        }

        // Vérifier que multipart est activé
        boolean multipartEnabled = environment.getProperty("spring.servlet.multipart.enabled",
                Boolean.class, true);

        if (multipartEnabled) {
            log.info("✓ Multipart activé");
        } else {
            log.error("✗ Multipart désactivé - les uploads ne fonctionneront pas");
        }
    }
}