package com.concours.controller;

import com.concours.dto.*;
import com.concours.entity.StatutCandidature;
import com.concours.exception.BusinessException;
import com.concours.service.*;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/gestionnaire-local")
@PreAuthorize("hasRole('GESTIONNAIRE_LOCAL')")
public class GestionnaireLocalController {

    @Autowired
    private CandidatureService candidatureService;

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private ConcoursService concoursService;

    @Autowired
    private SpecialiteService specialiteService;

    @Autowired
    private CentreExamenService centreExamenService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        try {
            String username = principal.getName();
            Long userId = utilisateurService.getUserIdByUsername(username);
            UtilisateurDTO utilisateur = utilisateurService.getUtilisateurById(userId);

            // Vérifier si l'utilisateur a des centres affectés
            if (utilisateur.getCentresAffectes() == null || utilisateur.getCentresAffectes().isEmpty()) {
                model.addAttribute("error", "Aucun centre d'examen n'est associé à votre compte. Veuillez contacter l'administrateur.");
                return "gestionnaire-local/dashboard";
            }

            // Prendre le premier centre affecté
            Long centreId = utilisateur.getCentresAffectes().get(0).getId();
            CentreExamenDTO centre = centreExamenService.getCentreExamenById(centreId);

            // Obtenir les statistiques spécifiques au centre
            StatistiquesLocalDTO stats = getStatistiquesLocal(centreId);

            model.addAttribute("stats", stats);
            model.addAttribute("utilisateur", utilisateur);
            model.addAttribute("centre", centre);

            return "gestionnaire-local/dashboard";
        } catch (Exception e) {
            log.error("Erreur lors du chargement du dashboard gestionnaire local", e);
            model.addAttribute("error", "Erreur lors du chargement des statistiques: " + e.getMessage());
            return "gestionnaire-local/dashboard";
        }
    }

    @GetMapping("/candidatures/list")
    public String listeCandidatures(Model model,
                                    Principal principal,
                                    @RequestParam(required = false) Long concoursId,
                                    @RequestParam(required = false) Long specialiteId,
                                    @RequestParam(required = false) String statut,
                                    @RequestParam(required = false) String diplome,
                                    @RequestParam(required = false) String search,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        try {
            String username = principal.getName();
            Long userId = utilisateurService.getUserIdByUsername(username);
            UtilisateurDTO utilisateur = utilisateurService.getUtilisateurById(userId);

            Long centreId = getCentreIdFromUser(utilisateur);
            if (centreId == null) {
                model.addAttribute("error", "Aucun centre d'examen associé à votre compte");
                return "gestionnaire-local/candidatures/list";
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by("dateDepot").descending());
            Page<CandidatureDTO> candidatures;

            // Recherche par texte si search est fourni
            if (search != null && !search.trim().isEmpty()) {
                // Implémenter cette méthode dans le service si nécessaire
                candidatures = candidatureService.getCandidaturesWithFilters(
                        null, null, centreId, statut, diplome, pageable);
            } else {
                // Filtrage standard pour le centre
                candidatures = candidatureService.getCandidaturesByCentreWithFilters(
                        centreId, concoursId, specialiteId, statut, diplome, pageable);
            }

            model.addAttribute("candidatures", candidatures);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", candidatures.getTotalPages());
            model.addAttribute("totalElements", candidatures.getTotalElements());
            model.addAttribute("pageSize", size);

            // Données pour les filtres
            model.addAttribute("concoursList", concoursService.getAllConcours(PageRequest.of(0, 100)).getContent());
            model.addAttribute("specialites", specialiteService.listerToutesLesSpecialites());
            model.addAttribute("centreActuel", centreExamenService.getCentreExamenById(centreId));

            // Valeurs des filtres actuels
            model.addAttribute("concoursId", concoursId);
            model.addAttribute("specialiteId", specialiteId);
            model.addAttribute("statut", statut);
            model.addAttribute("diplome", diplome);
            model.addAttribute("search", search);

            return "gestionnaire-local/candidatures/list";
        } catch (Exception e) {
            log.error("Erreur lors du chargement des candidatures", e);
            model.addAttribute("error", "Erreur lors du chargement des candidatures: " + e.getMessage());
            return "gestionnaire-local/candidatures/list";
        }
    }

    @GetMapping("/candidatures/details/{numero}")
    public String detailsCandidature(@PathVariable String numero, Model model, RedirectAttributes redirectAttributes) {
        try {
            CandidatureDTO candidature = candidatureService.getCandidatureByNumero(numero);
            if (candidature == null) {
                redirectAttributes.addFlashAttribute("error", "Candidature non trouvée: " + numero);
                return "redirect:/gestionnaire-local/candidatures/list";
            }
            model.addAttribute("candidature", candidature);
            return "gestionnaire-local/candidatures/details";
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/gestionnaire-local/candidatures/list";
        }
    }

    @GetMapping("/candidatures/validation")
    public String validationCandidatures(Model model,
                                         Principal principal,
                                         @RequestParam(defaultValue = "EN_ATTENTE") String statutFiltre,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "15") int size) {
        try {
            String username = principal.getName();
            Long userId = utilisateurService.getUserIdByUsername(username);
            UtilisateurDTO utilisateur = utilisateurService.getUtilisateurById(userId);

            Long centreId = getCentreIdFromUser(utilisateur);
            if (centreId == null) {
                model.addAttribute("error", "Aucun centre d'examen associé à votre compte");
                return "gestionnaire-local/candidatures/validation";
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by("dateDepot").ascending());
            StatutCandidature statut = StatutCandidature.valueOf(statutFiltre);

            Page<CandidatureDTO> candidatures = candidatureService.getCandidaturesByCentreAndStatut(
                    centreId, statut, pageable);

            // Statistiques pour le header
            long totalEnAttente = candidatureService.countCandidaturesEnAttenteByCentre(centreId);
            long totalValidees = candidatureService.countCandidaturesValideesByCentre(centreId);
            long totalRejetees = candidatureService.countCandidaturesRejeteesByCentre(centreId);

            model.addAttribute("candidatures", candidatures);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", candidatures.getTotalPages());
            model.addAttribute("statutFiltre", statutFiltre);
            model.addAttribute("totalEnAttente", totalEnAttente);
            model.addAttribute("totalValidees", totalValidees);
            model.addAttribute("totalRejetees", totalRejetees);

            return "gestionnaire-local/candidatures/details";
        } catch (Exception e) {
            log.error("Erreur lors du chargement des candidatures à valider", e);
            model.addAttribute("error", "Erreur lors du chargement des candidatures: " + e.getMessage());
            return "gestionnaire-local/candidatures/details";
        }
    }

    @PostMapping("/candidatures/valider/{numero}")
    public String validerCandidature(@PathVariable String numero,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {
        try {
            String username = principal.getName();
            Long userId = utilisateurService.getUserIdByUsername(username);
            UtilisateurDTO utilisateur = utilisateurService.getUtilisateurById(userId);

            CandidatureDTO candidature = candidatureService.getCandidatureByNumero(numero);
            Long centreId = getCentreIdFromUser(utilisateur);

            // Vérification d'accès (à décommenter et adapter)
            // if (centreId == null || !candidature.getCentreId().equals(centreId)) {
            //     redirectAttributes.addFlashAttribute("error", "Accès non autorisé à cette candidature");
            //     return "redirect:/gestionnaire-local/candidatures/list";
            // }

            candidatureService.validerCandidature(numero, userId);

            redirectAttributes.addFlashAttribute("success",
                    "Candidature " + numero + " validée avec succès !");

        } catch (Exception e) {
            log.error("Erreur lors de la validation de la candidature {}", numero, e);
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la validation : " + e.getMessage());
        }

        // Redirection vers la page de détails de la candidature
        return "redirect:/gestionnaire-local/candidatures/details/" + numero;
    }

    @PostMapping("/candidatures/rejeter/{numero}")
    public String rejeterCandidature(@PathVariable String numero,
                                     @RequestParam String motif,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {
        try {
            String username = principal.getName();
            Long userId = utilisateurService.getUserIdByUsername(username);
            UtilisateurDTO utilisateur = utilisateurService.getUtilisateurById(userId);

            CandidatureDTO candidature = candidatureService.getCandidatureByNumero(numero);
            Long centreId = getCentreIdFromUser(utilisateur);

            // Vérification d'accès (à décommenter et adapter)
            // if (centreId == null || !candidature.getCentreId().equals(centreId)) {
            //     redirectAttributes.addFlashAttribute("error", "Accès non autorisé à cette candidature");
            //     return "redirect:/gestionnaire-local/candidatures/list";
            // }

            if (motif == null || motif.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Le motif de rejet est obligatoire");
                return "redirect:/gestionnaire-local/candidatures/details/" + numero;
            }

            candidatureService.rejeterCandidature(numero, userId, motif);

            redirectAttributes.addFlashAttribute("success",
                    "Candidature " + numero + " rejetée avec succès !");

        } catch (Exception e) {
            log.error("Erreur lors du rejet de la candidature {}", numero, e);
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors du rejet : " + e.getMessage());
        }

        // Redirection vers la page de détails de la candidature
        return "redirect:/gestionnaire-local/candidatures/details/" + numero;
    }

    @GetMapping("/candidatures/communication/{numero}")
    public String communicationCandidat(@PathVariable String numero,
                                        Model model,
                                        Principal principal,
                                        RedirectAttributes redirectAttributes) {
        try {
            String username = principal.getName();
            Long userId = utilisateurService.getUserIdByUsername(username);
            UtilisateurDTO utilisateur = utilisateurService.getUtilisateurById(userId);

            CandidatureDTO candidature = candidatureService.getCandidatureByNumero(numero);
            Long centreId = getCentreIdFromUser(utilisateur);

            // Vérification corrigée avec null check
//            if (candidature.getCentreId() == null || !candidature.getCentreId().equals(centreId)) {
//                redirectAttributes.addFlashAttribute("error", "Accès non autorisé à cette candidature");
//                return "redirect:/gestionnaire-local/candidatures/list";
//            }

            model.addAttribute("candidature", candidature);
            model.addAttribute("messageForm", new MessageCandidatDTO());

            return "gestionnaire-local/candidatures/communication";
        } catch (Exception e) {
            log.error("Erreur lors du chargement de la communication", e);
            redirectAttributes.addFlashAttribute("error", "Erreur lors du chargement: " + e.getMessage());
            return "redirect:/gestionnaire-local/candidatures/list";
        }
    }

    @PostMapping("/candidatures/envoyer-message/{numero}")
    public String envoyerMessage(@PathVariable String numero,
                                 @ModelAttribute MessageCandidatDTO messageDTO,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            String username = principal.getName();
            Long userId = utilisateurService.getUserIdByUsername(username);
            UtilisateurDTO utilisateur = utilisateurService.getUtilisateurById(userId);

            CandidatureDTO candidature = candidatureService.getCandidatureByNumero(numero);
            Long centreId = getCentreIdFromUser(utilisateur);

//            if (centreId == null || !candidature.getCentreId().equals(centreId)) {
//                redirectAttributes.addFlashAttribute("error", "Accès non autorisé à cette candidature");
//                return "redirect:/gestionnaire-local/candidatures/list";
//            }

            if (messageDTO.getContenu() == null || messageDTO.getContenu().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Le message ne peut pas être vide");
                return "redirect:/gestionnaire-local/candidatures/communication/" + numero;
            }

            // Envoyer le message (à implémenter selon votre service de notification)
            // notificationService.envoyerMessageCandidat(numero, userId, messageDTO.getContenu(), messageDTO.getSujet());

            redirectAttributes.addFlashAttribute("success", "Message envoyé avec succès !");

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi du message", e);
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'envoi: " + e.getMessage());
        }

        return "redirect:/gestionnaire-local/candidatures/communication/" + numero;
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        try {
            String username = principal.getName();
            Long userId = utilisateurService.getUserIdByUsername(username);
            UtilisateurDTO utilisateur = utilisateurService.getUtilisateurById(userId);

//            // Vérifier que l'utilisateur a bien le rôle gestionnaire local
//            if (!utilisateur.getRole().equals("GESTIONNAIRE_LOCAL")) {
//                return "redirect:/access-denied";
//            }

            model.addAttribute("utilisateur", utilisateur);
            return "gestionnaire-local/profile";
        } catch (BusinessException e) {
            log.error("Utilisateur non trouvé", e);
            return "redirect:/login?error=userNotFound";
        }
    }

    @PostMapping("/candidatures/delete/{numero}")
    @PreAuthorize("hasRole('ADMIN')")
    public String supprimerCandidature(@PathVariable String numero,
                                       RedirectAttributes redirectAttributes) {
        try {
            candidatureService.supprimerCandidature(numero);
            redirectAttributes.addFlashAttribute("success",
                    "Candidature " + numero + " supprimée avec succès");
        } catch (BusinessException e) {
            log.error("Erreur lors de la suppression de la candidature", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la suppression", e);
            redirectAttributes.addFlashAttribute("error", "Erreur technique lors de la suppression");
        }
        return "redirect:/gestionnaire-local/candidatures/list";
    }

    // Méthodes privées utilitaires

    private Long getCentreIdFromUser(UtilisateurDTO utilisateur) {
        if (utilisateur.getCentresAffectes() != null && !utilisateur.getCentresAffectes().isEmpty()) {
            return utilisateur.getCentresAffectes().get(0).getId();
        }
        return null;
    }

    private StatistiquesLocalDTO getStatistiquesLocal(Long centreExamenId) {
        StatistiquesLocalDTO stats = new StatistiquesLocalDTO();

        try {
            CentreExamenDTO centre = centreExamenService.getCentreExamenById(centreExamenId);

            // Candidatures dans le centre
            long nbCandidaturesCentre = candidatureService.countCandidaturesByCentre(centreExamenId);
            stats.setNbCandidatsCentre(nbCandidaturesCentre);

            // Spécialités disponibles dans le centre
            int nbSpecialitesCentre = centreExamenService.countSpecialitesByCentre(centreExamenId);
            stats.setNbSpecialitesCentre(nbSpecialitesCentre);

            // Places restantes (capacité - candidatures validées)
            long candidaturesValidees = candidatureService.countCandidaturesValideesByCentre(centreExamenId);
            long placesRestantes = Math.max(0, centre.getCapacite() - candidaturesValidees);
            stats.setNbPlacesRestantes(placesRestantes);

            // Candidatures en attente de traitement
            long candidaturesEnAttente = candidatureService.countCandidaturesEnAttenteByCentre(centreExamenId);
            stats.setNbCandidaturesEnAttente(candidaturesEnAttente);

            // Candidatures validées
            stats.setNbCandidaturesValidees(candidaturesValidees);

            // Candidatures rejetées
            long candidaturesRejetees = candidatureService.countCandidaturesRejeteesByCentre(centreExamenId);
            stats.setNbCandidaturesRejetees(candidaturesRejetees);

            // Informations du centre
            stats.setNomCentre(centre.getVilleNom());
            stats.setVilleCentre(centre.getVilleNom());
            stats.setCapaciteTotale(centre.getCapacite());

            // Taux de remplissage
            double tauxRemplissage = centre.getCapacite() > 0 ?
                    (candidaturesValidees * 100.0) / centre.getCapacite() : 0;
            stats.setTauxRemplissage((int) Math.min(100, Math.round(tauxRemplissage)));

        } catch (Exception e) {
            log.warn("Erreur lors du calcul des statistiques locales", e);
            // Valeurs par défaut en cas d'erreur
            stats.setNbCandidatsCentre(0);
            stats.setNbSpecialitesCentre(0);
            stats.setNbPlacesRestantes(0);
            stats.setNbCandidaturesEnAttente(0);
            stats.setNbCandidaturesValidees(0);
            stats.setNbCandidaturesRejetees(0);
            stats.setTauxRemplissage(0);
        }

        return stats;
    }

    // Méthode pour compter les candidatures rejetées par centre (à ajouter au service si nécessaire)
    private long countCandidaturesRejeteesByCentre(Long centreId) {
        try {
            // Cette méthode devrait être implémentée dans CandidatureService
            // Pour l'instant, on utilise une approximation
            return candidatureService.countCandidaturesByCentre(centreId) -
                    candidatureService.countCandidaturesValideesByCentre(centreId) -
                    candidatureService.countCandidaturesEnAttenteByCentre(centreId);
        } catch (Exception e) {
            log.error("Erreur lors du comptage des candidatures rejetées", e);
            return 0;
        }
    }
}