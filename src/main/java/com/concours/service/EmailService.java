package com.concours.service;

import com.concours.exception.BusinessException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;



    public void envoyerNotification(String email, String sujet, String message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setSubject(sujet);
        mailMessage.setText(message);

        mailSender.send(mailMessage);
    }

    public void envoyerEmailHtml(String email, String sujet, String contenuHtml) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject(sujet);
        helper.setText(contenuHtml, true);

        mailSender.send(message);
    }

    public void envoyerRappelCandidature(String email, String numeroCandidature, int joursRestants) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Rappel - Candidature en attente");
        message.setText("Votre candidature " + numeroCandidature + " est en cours de traitement.\n\n" +
                "Il reste " + joursRestants + " jour(s) avant la clôture du concours.\n\n" +
                "Merci de votre patience.");

        mailSender.send(message);
    }

    public void envoyerConfirmationCandidature(String email, String numeroCandidature) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Confirmation de candidature - MEF");
            message.setText(String.format("""
                Bonjour,

                Votre candidature a été soumise avec succès.
                
                Numéro de candidature: %s
                
                Vous pouvez suivre l'état de votre candidature en utilisant ce numéro et votre CIN sur notre portail.
                
                Cordialement,
                L'équipe des concours MEF
                """, numeroCandidature));

            message.setFrom("concours@mef.gov.ma");

            mailSender.send(message);
            log.info("Email de confirmation envoyé à: {}", email);

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de confirmation à: {}", email, e);
        }
    }

//    public void envoyerNotificationValidation(String email, String numeroCandidature) {
//        try {
//            SimpleMailMessage message = new SimpleMailMessage();
//            message.setTo(email);
//            message.setSubject("Candidature validée - MEF");
//            message.setText(String.format("""
//                Bonjour,
//
//                Nous avons le plaisir de vous informer que votre candidature a été validée.
//
//                Numéro de candidature: %s
//
//                Vous recevrez prochainement les informations concernant les modalités de l'examen.
//
//                Cordialement,
//                L'équipe des concours MEF
//                """, numeroCandidature));
//
//            message.setFrom("concours@mef.gov.ma");
//
//            mailSender.send(message);
//            log.info("Email de validation envoyé à: {}", email);
//
//        } catch (Exception e) {
//            log.error("Erreur lors de l'envoi de l'email de validation à: {}", email, e);
//        }
//    }
//
//    public void envoyerNotificationRejet(String email, String numeroCandidature, String motif) {
//        try {
//            SimpleMailMessage message = new SimpleMailMessage();
//            message.setTo(email);
//            message.setSubject("Candidature rejetée - MEF");
//            message.setText(String.format("""
//                Bonjour,
//
//                Nous vous informons que votre candidature n'a pas pu être retenue.
//
//                Numéro de candidature: %s
//                Motif: %s
//
//                Vous pouvez nous contacter pour plus d'informations.
//
//                Cordialement,
//                L'équipe des concours MEF
//                """, numeroCandidature, motif != null ? motif : "Non spécifié"));
//
//            message.setFrom("concours@mef.gov.ma");
//
//            mailSender.send(message);
//            log.info("Email de rejet envoyé à: {}", email);
//
//        } catch (Exception e) {
//            log.error("Erreur lors de l'envoi de l'email de rejet à: {}", email, e);
//        }
//    }
//
//    /**
//     * Envoie un message personnalisé à un candidat
//     */
//    public void envoyerMessageCandidat(String emailCandidat, String nomCandidat, String sujet,
//                                       String contenu, String emailGestionnaire) {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//            helper.setTo(emailCandidat);
//            helper.setSubject("MEF Concours - " + sujet);
//            helper.setReplyTo(emailGestionnaire);
//
//            String htmlContent = buildMessageCandidatTemplate(nomCandidat, contenu, emailGestionnaire);
//            helper.setText(htmlContent, true);
//
//            mailSender.send(message);
//            log.info("Message envoyé avec succès au candidat: {}", emailCandidat);
//
//        } catch (Exception e) {
//            log.error("Erreur lors de l'envoi du message au candidat", e);
//            throw new BusinessException("Erreur lors de l'envoi de l'email");
//        }
//    }
//
//     /**
//     * Template HTML pour les messages aux candidats
//     */
//    private String buildMessageCandidatTemplate(String nomCandidat, String contenu, String emailGestionnaire) {
//        return String.format("""
//        <html>
//        <head>
//            <style>
//                .container { max-width: 600px; margin: 0 auto; font-family: Arial, sans-serif; }
//                .header { background-color: #0056b3; color: white; padding: 20px; text-align: center; }
//                .content { padding: 20px; line-height: 1.6; }
//                .footer { background-color: #f8f9fa; padding: 15px; font-size: 12px; color: #6c757d; }
//            </style>
//        </head>
//        <body>
//            <div class="container">
//                <div class="header">
//                    <h2>Ministère de l'Economie et des Finances</h2>
//                    <p>Message concernant votre candidature</p>
//                </div>
//                <div class="content">
//                    <p>Bonjour %s,</p>
//                    <div style="background-color: #f8f9fa; padding: 15px; border-left: 4px solid #0056b3;">
//                        %s
//                    </div>
//                    <p>Cordialement,<br>Le Centre d'Examen</p>
//                </div>
//                <div class="footer">
//                    <p>Pour toute question, vous pouvez répondre à cet email.</p>
//                    <p>Email du gestionnaire: %s</p>
//                </div>
//            </div>
//        </body>
//        </html>
//        """, nomCandidat, contenu.replace("\n", "<br>"), emailGestionnaire);
//    }

    /**
     * Envoie un email de validation de candidature
     */
    public void envoyerNotificationValidation(String emailCandidat, String numeroCandidature) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(emailCandidat);
            helper.setSubject("MEF Concours - Candidature validée");

            String htmlContent = buildValidationTemplate(numeroCandidature);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email de validation envoyé pour la candidature: {}", numeroCandidature);

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de validation", e);
            throw new BusinessException("Erreur lors de l'envoi de l'email");
        }
    }

    /**
     * Envoie un email de rejet de candidature
     */
    public void envoyerNotificationRejet(String emailCandidat, String numeroCandidature, String motif) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(emailCandidat);
            helper.setSubject("MEF Concours - Candidature non retenue");

            String htmlContent = buildRejetTemplate(numeroCandidature, motif);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email de rejet envoyé pour la candidature: {}", numeroCandidature);

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de rejet", e);
            throw new BusinessException("Erreur lors de l'envoi de l'email");
        }
    }

    private String buildValidationTemplate(String numeroCandidature) {
        return String.format("""
        <html>
        <head>
            <style>
                .container { max-width: 600px; margin: 0 auto; font-family: Arial, sans-serif; }
                .header { background-color: #28a745; color: white; padding: 20px; text-align: center; }
                .content { padding: 20px; line-height: 1.6; }
                .footer { background-color: #f8f9fa; padding: 15px; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h2>🎉 Candidature Validée</h2>
                </div>
                <div class="content">
                    <p>Félicitations !</p>
                    <p>Votre candidature <strong>%s</strong> a été validée par notre équipe.</p>
                    <p>Vous recevrez prochainement des informations concernant les prochaines étapes du processus de sélection.</p>
                    <p>Cordialement,<br>Le Service des Concours - MEF</p>
                </div>
                <div class="footer">
                    <p>Ministère de l'Economie et des Finances</p>
                </div>
            </div>
        </body>
        </html>
        """, numeroCandidature);
    }

    private String buildRejetTemplate(String numeroCandidature, String motif) {
        return String.format("""
        <html>
        <head>
            <style>
                .container { max-width: 600px; margin: 0 auto; font-family: Arial, sans-serif; }
                .header { background-color: #dc3545; color: white; padding: 20px; text-align: center; }
                .content { padding: 20px; line-height: 1.6; }
                .motif { background-color: #f8f9fa; padding: 15px; border-left: 4px solid #dc3545; margin: 15px 0; }
                .footer { background-color: #f8f9fa; padding: 15px; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h2>Candidature non retenue</h2>
                </div>
                <div class="content">
                    <p>Nous vous remercions pour l'intérêt que vous portez à nos concours.</p>
                    <p>Malheureusement, votre candidature <strong>%s</strong> n'a pas pu être retenue.</p>
                    
                    <div class="motif">
                        <strong>Motif :</strong><br>
                        %s
                    </div>
                    
                    <p>Nous vous encourageons à postuler pour d'autres concours qui correspondent à votre profil.</p>
                    <p>Cordialement,<br>Le Service des Concours - MEF</p>
                </div>
                <div class="footer">
                    <p>Ministère de l'Economie et des Finances</p>
                </div>
            </div>
        </body>
        </html>
        """, numeroCandidature, motif);
    }
}
