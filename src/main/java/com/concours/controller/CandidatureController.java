package com.concours.controller;

import com.concours.dto.*;
import com.concours.entity.Candidature;
import com.concours.entity.StatutCandidature;
import com.concours.exception.BusinessException;
import com.concours.repository.CandidatureRepository;
import com.concours.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin/candidatures")
@PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE_GLOBAL', 'GESTIONNAIRE_LOCAL')")
@RequiredArgsConstructor
public class CandidatureController {

    private final CandidatureService candidatureService;
    private final ConcoursService concoursService;
    private final SpecialiteService specialiteService;
    private final CentreExamenService centreExamenService;
    private final UtilisateurService utilisateurService;
    private final DocumentService documentService;
    private final CandidatureRepository candidatureRepository;

    /**
     * Liste des candidatures avec filtres
     */
    @GetMapping("/list")
    public String listeCandidatures(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    @RequestParam(required = false) Long concoursId,
                                    @RequestParam(required = false) Long specialiteId,
                                    @RequestParam(required = false) Long centreId,
                                    @RequestParam(required = false) String statut, // Gardez en String
                                    @RequestParam(required = false) String diplome,
                                    Model model,
                                    Authentication authentication) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CandidatureDTO> candidatures;

            log.info("Filtres - concoursId: {}, specialiteId: {}, centreId: {}, statut: {}, diplome: {}",
                    concoursId, specialiteId, centreId, statut, diplome);

            // Si c'est un gestionnaire local, filtrer par ses centres
            if (authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_GESTIONNAIRE_LOCAL"))) {

                String username = authentication.getName();
                Long userId = utilisateurService.getUserIdByUsername(username);
                UtilisateurDTO utilisateur = utilisateurService.getUtilisateurById(userId);

                log.info("Gestionnaire local détecté: {}, centres: {}", username, utilisateur.getCentresAffectes());

                if (centreId == null && !utilisateur.getCentresAffectes().isEmpty()) {
                    centreId = utilisateur.getCentresAffectes().get(0).getId();
                    log.info("Centre auto-sélectionné: {}", centreId);
                }
            }

            // Utiliser la méthode de service avec tous les filtres
            candidatures = candidatureService.getCandidaturesWithFilters(
                    concoursId, specialiteId, centreId, statut, diplome, pageable);

            log.info("Candidatures trouvées: {}", candidatures.getTotalElements());

            // Charger les données pour les filtres
            model.addAttribute("candidatures", candidatures);
            model.addAttribute("concours", concoursService.getAllConcours(PageRequest.of(0, 100)).getContent());
            model.addAttribute("specialites", specialiteService.getAllSpecialites());
            model.addAttribute("centres", centreExamenService.listerTousLesCentres());

            // Paramètres de filtrage
            model.addAttribute("concoursId", concoursId);
            model.addAttribute("specialiteId", specialiteId);
            model.addAttribute("centreId", centreId);
            model.addAttribute("statut", statut);
            model.addAttribute("diplome", diplome);

            // Pagination
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", candidatures.getTotalPages());
            model.addAttribute("totalElements", candidatures.getTotalElements());

            // Dans votre service ou contrôleur
            if (!candidatures.isEmpty()) {
                CandidatureDTO first = candidatures.getContent().iterator().next();
                log.info("Candidature DTO - Numero: {}, Statut: {}, CandidatDiplome: {}",
                        first.getNumero(), first.getStatut(), first.getCandidatDiplome());

                // Vérifiez aussi l'entité originale
                Candidature entity = candidatureRepository.findById(first.getNumero()).orElse(null);
                if (entity != null) {
                    log.info("Candidature Entity - Numero: {}, Statut: {}, Diplome: {}",
                            entity.getNumero(), entity.getStatut(), entity.getCandidat().getDiplome());
                }
            }

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des candidatures", e);
            model.addAttribute("error", "Erreur lors du chargement des candidatures");
            model.addAttribute("candidatures", Page.empty());
        }

        return "admin/candidatures/list";
    }

    /**
     * Détails d'une candidature
     */
    @GetMapping("/details/{numero}")
    public String detailsCandidature(@PathVariable String numero, Model model) {
        try {
            CandidatureDTO candidature = candidatureService.getCandidatureByNumero(numero);
            model.addAttribute("candidature", candidature);
            return "admin/candidatures/detail";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/admin/candidatures/list";
        }
    }

    /**
     * Formulaire d'édition d'une candidature
     */
    @GetMapping("/edit/{numero}")
    public String editCandidature(@PathVariable String numero, Model model) {
        try {
            CandidatureDTO candidature = candidatureService.getCandidatureByNumero(numero);

            model.addAttribute("candidature", candidature);
            model.addAttribute("statuts", StatutCandidature.values());

            return "admin/candidatures/edit";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/admin/candidatures/list";
        }
    }

    /**
     * Validation d'une candidature
     */
    @PostMapping("/valider/{numero}")
    public String validerCandidature(@PathVariable String numero,
                                     RedirectAttributes redirectAttributes,
                                     Authentication authentication) {
        try {
            String username = authentication.getName();
            Long utilisateurId = utilisateurService.getUserIdByUsername(username);

            candidatureService.validerCandidature(numero, utilisateurId);
            redirectAttributes.addFlashAttribute("success",
                    "Candidature " + numero + " validée avec succès");

        } catch (Exception e) {
            log.error("Erreur lors de la validation de la candidature " + numero, e);
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la validation : " + e.getMessage());
        }

        return "redirect:/admin/candidatures/details/" + numero;
    }

    /**
     * Rejet d'une candidature
     */
    @PostMapping("/rejeter/{numero}")
    public String rejeterCandidature(@PathVariable String numero,
                                     @RequestParam String motif,
                                     RedirectAttributes redirectAttributes,
                                     Authentication authentication) {
        try {
            if (motif == null || motif.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Le motif de rejet est obligatoire");
                return "redirect:/admin/candidatures/details/" + numero;
            }

            String username = authentication.getName();
            Long utilisateurId = utilisateurService.getUserIdByUsername(username);

            candidatureService.rejeterCandidature(numero, utilisateurId, motif);
            redirectAttributes.addFlashAttribute("success",
                    "Candidature " + numero + " rejetée avec succès");

        } catch (Exception e) {
            log.error("Erreur lors du rejet de la candidature " + numero, e);
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors du rejet : " + e.getMessage());
        }

        return "redirect:/admin/candidatures/details/" + numero;
    }

    /**
     * Téléchargement d'un document
     */
    @GetMapping("/documents/download/{documentId}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long documentId) {
        try {
            com.concours.entity.Document document = documentService.getDocumentById(documentId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(document.getContentType()));
            headers.setContentDispositionFormData("attachment", document.getNom());
            headers.setContentLength(document.getTaille());

            return new ResponseEntity<>(document.getContenu(), headers, HttpStatus.OK);

        } catch (BusinessException e) {
            log.error("Document non trouvé: {}", documentId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur lors du téléchargement du document: {}", documentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Visualisation d'un document PDF
     */
    @GetMapping("/documents/view/{documentId}")
    public ResponseEntity<byte[]> viewDocument(@PathVariable Long documentId) {
        try {
            com.concours.entity.Document document = documentService.getDocumentById(documentId);

            if (!"application/pdf".equals(document.getContentType())) {
                return ResponseEntity.badRequest().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", document.getNom());

            return new ResponseEntity<>(document.getContenu(), headers, HttpStatus.OK);

        } catch (BusinessException e) {
            log.error("Document non trouvé: {}", documentId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur lors de la visualisation du document: {}", documentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Suppression d'une candidature (admin seulement)
     */
    @PostMapping("/delete/{numero}")
    @PreAuthorize("hasRole('ADMIN')")
    public String supprimerCandidature(@PathVariable String numero,
                                       RedirectAttributes redirectAttributes) {
        try {
            // Appeler le service pour supprimer la candidature
            candidatureService.supprimerCandidature(numero);
            redirectAttributes.addFlashAttribute("success",
                    "Candidature " + numero + " supprimée avec succès");

        } catch (BusinessException e) {
            log.error("Erreur lors de la suppression de la candidature " + numero, e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la suppression de la candidature " + numero, e);
            redirectAttributes.addFlashAttribute("error",
                    "Erreur technique lors de la suppression : " + e.getMessage());
        }

        return "redirect:/admin/candidatures/list";
    }

    /**
     * Dashboard des candidatures pour un centre
     */
    @GetMapping("/dashboard/{centreId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE_LOCAL')")
    public String dashboardCentre(@PathVariable Long centreId, Model model) {
        try {
            CentreExamenDTO centre = centreExamenService.getCentreExamenById(centreId);

            // Statistiques du centre
            long totalCandidatures = candidatureService.countCandidaturesByCentre(centreId);
            long candidaturesValidees = candidatureService.countCandidaturesValideesByCentre(centreId);
            long candidaturesEnAttente = candidatureService.countCandidaturesEnAttenteByCentre(centreId);

            // Places par spécialité
            List<PlacesSpecialiteDTO> placesParSpecialite =
                    candidatureService.getPlacesDisponiblesParSpecialite(centreId);

            model.addAttribute("centre", centre);
            model.addAttribute("totalCandidatures", totalCandidatures);
            model.addAttribute("candidaturesValidees", candidaturesValidees);
            model.addAttribute("candidaturesEnAttente", candidaturesEnAttente);
            model.addAttribute("placesParSpecialite", placesParSpecialite);

            return "admin/candidatures/dashboard-centre";

        } catch (Exception e) {
            log.error("Erreur lors du chargement du dashboard centre " + centreId, e);
            model.addAttribute("error", "Erreur lors du chargement du dashboard");
            return "redirect:/admin/candidatures/list";
        }
    }

    /**
     * API pour récupérer les candidatures en JSON (pour AJAX)
     */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<Page<CandidatureDTO>> apiListeCandidatures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long centreId,
            @RequestParam(required = false) String statut) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CandidatureDTO> candidatures;

            if (centreId != null && statut != null) {
                StatutCandidature statutEnum = StatutCandidature.valueOf(statut);
                candidatures = candidatureService.getCandidaturesByCentreAndStatut(centreId, statutEnum, pageable);
            } else if (centreId != null) {
                candidatures = candidatureService.getCandidaturesByCentre(centreId, pageable);
            } else {
                candidatures = Page.empty();
            }

            return ResponseEntity.ok(candidatures);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des candidatures via API", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Gestion des erreurs
     */
    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException e, RedirectAttributes redirectAttributes) {
        log.warn("Erreur métier: {}", e.getMessage());
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/admin/candidatures/list";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, RedirectAttributes redirectAttributes) {
        log.error("Erreur inattendue dans le contrôleur candidatures", e);
        redirectAttributes.addFlashAttribute("error", "Une erreur inattendue s'est produite");
        return "redirect:/admin/candidatures/list";
    }
}