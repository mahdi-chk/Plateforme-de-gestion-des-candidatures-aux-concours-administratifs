package com.concours.service;

import com.concours.entity.*;
import com.concours.exception.ValidationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

@Service
public class ValidationService {

    public void validerCandidature(Candidat candidat, Concours concours, Specialite specialite) {
        Map<String, String> erreurs = new HashMap<>();

        // Validation de l'âge (exemple: max 40 ans pour les concours publics)
        if (candidat.getDateNaissance() != null) {
            int age = Period.between(candidat.getDateNaissance(), LocalDate.now()).getYears();
            if (age > 40) {
                erreurs.put("age", "L'âge ne doit pas dépasser 40 ans");
            }
            if (age < 18) {
                erreurs.put("age", "L'âge doit être d'au moins 18 ans");
            }
        }

        // Validation des dates du concours
        LocalDate maintenant = LocalDate.now();
        if (maintenant.isBefore(concours.getDateOuverture())) {
            erreurs.put("concours", "Les candidatures ne sont pas encore ouvertes");
        }
        if (maintenant.isAfter(concours.getDateCloture())) {
            erreurs.put("concours", "Les candidatures sont fermées");
        }

        // Validation de l'adéquation diplôme/spécialité
        if (!verifierAdequationDiplomeSpecialite(candidat.getDiplome(), specialite.getLibelle())) {
            erreurs.put("diplome", "Le diplôme ne correspond pas à la spécialité choisie");
        }

        if (!erreurs.isEmpty()) {
            throw new ValidationException("Erreurs de validation", erreurs);
        }
    }

    private boolean verifierAdequationDiplomeSpecialite(String diplome, String specialite) {
        // Logique de vérification de l'adéquation diplôme/spécialité
        // Cette méthode peut être enrichie avec des règles métier complexes

        if (diplome == null || specialite == null) {
            return false;
        }

        String diplomeNormalise = diplome.toLowerCase().trim();
        String specialiteNormalisee = specialite.toLowerCase().trim();

        // Règles d'exemple
        if (specialiteNormalisee.contains("comptabilité") || specialiteNormalisee.contains("comptable")) {
            return diplomeNormalise.contains("comptabilité") ||
                    diplomeNormalise.contains("gestion") ||
                    diplomeNormalise.contains("finance");
        }

        if (specialiteNormalisee.contains("informatique") || specialiteNormalisee.contains("réseaux")) {
            return diplomeNormalise.contains("informatique") ||
                    diplomeNormalise.contains("réseaux") ||
                    diplomeNormalise.contains("systèmes");
        }

        if (specialiteNormalisee.contains("génie civil") || specialiteNormalisee.contains("btp")) {
            return diplomeNormalise.contains("génie civil") ||
                    diplomeNormalise.contains("btp") ||
                    diplomeNormalise.contains("construction");
        }

        // Par défaut, accepter si aucune règle spécifique
        return true;
    }

    public void validerDocuments(Map<TypeDocument, String> documents) {
        Map<String, String> erreurs = new HashMap<>();

        // Vérifier que tous les documents obligatoires sont présents
        if (!documents.containsKey(TypeDocument.CIN) || documents.get(TypeDocument.CIN) == null) {
            erreurs.put("cin", "Document CIN obligatoire");
        }

        if (!documents.containsKey(TypeDocument.CV) || documents.get(TypeDocument.CV) == null) {
            erreurs.put("cv", "CV obligatoire");
        }

        if (!documents.containsKey(TypeDocument.DIPLOME) || documents.get(TypeDocument.DIPLOME) == null) {
            erreurs.put("diplome", "Document diplôme obligatoire");
        }

        if (!erreurs.isEmpty()) {
            throw new ValidationException("Documents manquants", erreurs);
        }
    }
}
