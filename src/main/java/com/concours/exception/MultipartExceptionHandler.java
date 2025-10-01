package com.concours.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Gestionnaire global d'erreurs pour les problèmes multipart
 */
@ControllerAdvice
@Slf4j
public class MultipartExceptionHandler {

    /**
     * Gère les erreurs de taille de fichier dépassée
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e,
                                              HttpServletRequest request,
                                              RedirectAttributes redirectAttributes) {

        log.error("Taille de fichier dépassée: {}", e.getMessage());

        // Calculer la taille maximale autorisée
        long maxSize = e.getMaxUploadSize();
        String maxSizeFormatted = formatSize(maxSize);

        String errorMessage = String.format(
                "La taille des fichiers dépasse la limite autorisée (%s). " +
                        "Veuillez réduire la taille de vos fichiers PDF.",
                maxSizeFormatted
        );

        redirectAttributes.addFlashAttribute("error", errorMessage);

        // Rediriger vers le formulaire de candidature
        return "redirect:/public/candidature";
    }

    /**
     * Gère les erreurs multipart génériques (y compris FileCountLimitExceededException)
     */
    @ExceptionHandler(MultipartException.class)
    public String handleMultipartException(MultipartException e,
                                           HttpServletRequest request,
                                           RedirectAttributes redirectAttributes) {

        log.error("Erreur multipart: {}", e.getMessage(), e);

        String errorMessage;

        // Analyser le type d'erreur spécifique
        if (e.getMessage().contains("FileCountLimitExceededException") ||
                e.getMessage().contains("attachment")) {

            errorMessage = "Erreur lors du traitement des fichiers. " +
                    "Assurez-vous de n'envoyer que les 3 fichiers requis (CV, CIN, Diplôme) " +
                    "et que chaque fichier soit au format PDF.";

            log.error("FileCountLimitExceededException détectée - probable problème de configuration multipart");

        } else if (e.getMessage().contains("FileSizeLimitExceededException")) {

            errorMessage = "Un ou plusieurs fichiers sont trop volumineux. " +
                    "La taille maximale autorisée est de 100MB par fichier.";

        } else if (e.getMessage().contains("SizeLimitExceededException")) {

            errorMessage = "La taille totale de votre requête dépasse la limite autorisée. " +
                    "Veuillez réduire la taille de vos fichiers PDF.";

        } else {

            errorMessage = "Erreur lors du traitement des fichiers. " +
                    "Veuillez vérifier que vos fichiers sont au format PDF et " +
                    "ne dépassent pas 100MB chacun.";
        }

        redirectAttributes.addFlashAttribute("error", errorMessage);

        return "redirect:/public/candidature";
    }

    /**
     * Gère les erreurs générales lors de l'upload
     */
    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalState(IllegalStateException e,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {

        if (e.getMessage().contains("multipart")) {
            log.error("Erreur d'état multipart: {}", e.getMessage());

            String errorMessage = "Erreur lors du traitement du formulaire. " +
                    "Veuillez réessayer en vous assurant que tous les fichiers " +
                    "sont correctement sélectionnés.";

            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/public/candidature";
        }

        // Si ce n'est pas lié au multipart, laisser le gestionnaire par défaut
        throw e;
    }

    /**
     * Formate une taille en octets en format lisible
     */
    private String formatSize(long bytes) {
        if (bytes < 0) return "Inconnue";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    /**
     * Classe pour diagnostiquer les erreurs multipart
     */
    public static class MultipartErrorDiagnostic {

        public static void logMultipartError(Exception e) {
            log.error("=== DIAGNOSTIC ERREUR MULTIPART ===");
            log.error("Type d'exception: {}", e.getClass().getSimpleName());
            log.error("Message: {}", e.getMessage());

            if (e.getCause() != null) {
                log.error("Cause: {} - {}", e.getCause().getClass().getSimpleName(), e.getCause().getMessage());
            }

            // Recommandations basées sur le type d'erreur
            if (e.getMessage().contains("FileCountLimit")) {
                log.error("RECOMMANDATION: Augmenter maxFileCount et maxPartCount dans la configuration");
            } else if (e.getMessage().contains("FileSizeLimit")) {
                log.error("RECOMMANDATION: Vérifier spring.servlet.multipart.max-file-size");
            } else if (e.getMessage().contains("SizeLimit")) {
                log.error("RECOMMANDATION: Vérifier spring.servlet.multipart.max-request-size");
            }

            log.error("=====================================");
        }
    }
}