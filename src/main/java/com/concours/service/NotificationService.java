package com.concours.service;

import com.concours.entity.Candidature;
import com.concours.entity.TypeNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailService emailService;
    // private final SmsService smsService; // À implémenter si nécessaire

    public void envoyerNotificationCandidature(Candidature candidature, String message) {
        TypeNotification typeNotif = candidature.getNotifications();
        String email = candidature.getCandidat().getEmail();
        String telephone = candidature.getCandidat().getTelephone();

        try {
            switch (typeNotif) {
                case EMAIL:
                    emailService.envoyerNotification(email, "Mise à jour candidature", message);
                    break;
                case SMS:
                    // smsService.envoyerSms(telephone, message);
                    log.info("SMS à envoyer à {} : {}", telephone, message);
                    break;
                case LES_DEUX:
                    emailService.envoyerNotification(email, "Mise à jour candidature", message);
                    // smsService.envoyerSms(telephone, message);
                    log.info("Email et SMS à envoyer pour la candidature {}", candidature.getNumero());
                    break;
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de notification pour la candidature {}: {}",
                    candidature.getNumero(), e.getMessage());
        }
    }
}