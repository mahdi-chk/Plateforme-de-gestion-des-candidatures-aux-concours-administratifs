package com.concours.controller;

import com.concours.dto.*;
import com.concours.entity.StatutCandidature;
import com.concours.exception.BusinessException;
import com.concours.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/gestionnaire-global")
@PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE_GLOBAL')")
public class GestionnaireGlobalController {

    @Autowired
    private StatistiquesService statistiquesService;

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CentreExamenService centreExamenService;

    @Autowired
    private ConcoursService concoursService;

    @Autowired
    private ExportService exportService;

    @Autowired
    private SpecialiteService specialiteService;

    @Autowired
    private VilleService villeService;

    @Autowired
    private CandidatureService candidatureService;

    @Autowired
    private DocumentService documentService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            StatistiquesDTO stats = statistiquesService.getStatistiquesAdmin();
            model.addAttribute("stats", stats);
            return "gestionnaire-global/dashboard";
        } catch (Exception e) {
            log.error("Erreur lors du chargement du dashboard", e);
            model.addAttribute("error", "Erreur lors du chargement des statistiques");
            return "gestionnaire-global/dashboard";
        }
    }

    @GetMapping("/gestion-concours/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE_GLOBAL')")    public String listeConcours(Model model,
                                @RequestParam(required = false) String search,
                                @RequestParam(required = false) String statut,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ConcoursDTO> concoursPage;

            if (search != null && !search.isEmpty()) {
                concoursPage = concoursService.searchConcours(search, pageable);
            } else if (statut != null && !statut.isEmpty()) {
                boolean isPublished = "true".equals(statut);
                concoursPage = concoursService.findByPublie(isPublished, pageable);
            } else {
                concoursPage = concoursService.getAllConcours(pageable);
            }

            model.addAttribute("concoursList", concoursPage.getContent());
            model.addAttribute("currentPage", concoursPage.getNumber() + 1);
            model.addAttribute("totalPages", concoursPage.getTotalPages());
            model.addAttribute("totalElements", concoursPage.getTotalElements());
            model.addAttribute("pageSize", size);
            model.addAttribute("search", search);
            model.addAttribute("statut", statut);

            return "gestionnaire-global/gestion-concours/list";
        } catch (Exception e) {
            log.error("Erreur lors du chargement de la liste des concours", e);
            model.addAttribute("error", "Erreur lors du chargement des concours");
            return "gestionnaire-global/gestion-concours/list";
        }
    }

    @GetMapping("/gestion-concours/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE_GLOBAL')")
    public String ajouterConcours(Model model) {
        model.addAttribute("concours", new ConcoursDTO());
        model.addAttribute("specialites", specialiteService.listerToutesLesSpecialites());
        model.addAttribute("centres", centreExamenService.listerTousLesCentres());
        model.addAttribute("conditionsPredefinies", Arrays.asList(
                "Être âgé de moins de 40 ans",
                "Avoir un diplôme de niveau BAC+3",
                "Avoir au moins 3 ans d'expérience",
                "Être de nationalité marocaine"
        ));
        return "gestionnaire-global/gestion-concours/add";
    }

    @PostMapping("/gestion-concours/save")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE_GLOBAL')")
    public String sauvegarderConcours(@Valid @ModelAttribute("concours") ConcoursDTO concoursDTO,
                                      BindingResult result,
                                      RedirectAttributes redirectAttributes,
                                      Model model) {

        // Réinjecter les données nécessaires pour le formulaire en cas d'erreur
        model.addAttribute("specialites", specialiteService.listerToutesLesSpecialites());
        model.addAttribute("centres", centreExamenService.listerTousLesCentres());
        model.addAttribute("conditionsPredefinies", Arrays.asList(
                "Être âgé de moins de 40 ans",
                "Avoir un diplôme de niveau BAC+3",
                "Avoir au moins 3 ans d'expérience",
                "Être de nationalité marocaine"
        ));

        if (result.hasErrors()) {
            log.error("Erreurs de validation: {}", result.getAllErrors());
            return "gestionnaire-global/gestion-concours/add";
        }

        try {
            concoursService.creerConcours(concoursDTO);
            redirectAttributes.addFlashAttribute("success", "Concours créé avec succès !");
            return "redirect:/gestionnaire-global/gestion-concours/list";
        } catch (Exception e) {
            log.error("Erreur lors de la création du concours", e);
            model.addAttribute("error", "Erreur lors de la création : " + e.getMessage());
            return "gestionnaire-global/gestion-concours/add";
        }
    }

    @GetMapping("/gestion-concours/edit/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE_GLOBAL')")
    public String modifierConcours(@PathVariable Long id, Model model) {
        try {
            ConcoursDTO concoursDTO = concoursService.getConcoursById(id);
            model.addAttribute("concours", concoursDTO);
            model.addAttribute("specialites", specialiteService.listerToutesLesSpecialites());
            model.addAttribute("centres", centreExamenService.listerTousLesCentres());
            model.addAttribute("conditionsPredefinies", Arrays.asList(
                    "Être âgé de moins de 40 ans",
                    "Avoir un diplôme de niveau BAC+3",
                    "Avoir au moins 3 ans d'expérience",
                    "Être de nationalité marocaine"
            ));
            return "gestionnaire-global/gestion-concours/edit";
        } catch (BusinessException e) {
            log.error("Concours non trouvé avec l'ID: {}", id, e);
            return "redirect:/gestionnaire-global/gestion-concours/list";
        }
    }

    @PostMapping("/gestion-concours/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE_GLOBAL')")
    public String updateConcours(@PathVariable Long id,
                                 @ModelAttribute("concours") ConcoursDTO concoursDTO,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {

        // Réinjecter les données nécessaires pour le formulaire en cas d'erreur
        model.addAttribute("specialites", specialiteService.listerToutesLesSpecialites());
        model.addAttribute("centres", centreExamenService.listerTousLesCentres());
        model.addAttribute("conditionsPredefinies", Arrays.asList(
                "Être âgé de moins de 40 ans",
                "Avoir un diplôme de niveau BAC+3",
                "Avoir au moins 3 ans d'expérience",
                "Être de nationalité marocaine"
        ));

        if (result.hasErrors()) {
            return "gestionnaire-global/gestion-concours/edit";
        }

        try {
            concoursService.modifierConcours(id, concoursDTO);
            redirectAttributes.addFlashAttribute("success", "Concours modifié avec succès !");
            return "redirect:/gestionnaire-global/gestion-concours/list";
        } catch (Exception e) {
            log.error("Erreur lors de la modification du concours", e);
            model.addAttribute("error", "Erreur lors de la modification : " + e.getMessage());
            model.addAttribute("concours", concoursDTO);
            return "gestionnaire-global/gestion-concours/edit";
        }
    }

    @PostMapping("/gestion-concours/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE_GLOBAL')")
    public String supprimerConcours(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            concoursService.supprimerConcours(id);
            redirectAttributes.addFlashAttribute("success", "Concours supprimé avec succès !");
        } catch (BusinessException e) {
            log.error("Erreur lors de la suppression du concours", e);
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression : " + e.getMessage());
        }
        return "redirect:/gestionnaire-global/gestion-concours/list";
    }

    @GetMapping("/gestion-concours/details/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE_GLOBAL')")
    public String detailsConcours(@PathVariable Long id, Model model) {
        try {
            ConcoursDTO concoursDTO = concoursService.getConcoursById(id);
            model.addAttribute("concours", concoursDTO);
            model.addAttribute("centres", centreExamenService.listerTousLesCentres());
            return "gestionnaire-global/gestion-concours/details";
        } catch (BusinessException e) {
            log.error("Concours non trouvé avec l'ID: {}", id, e);
            return "redirect:/gestionnaire-global/gestion-concours/list";
        }
    }


    @GetMapping("/gestion-centres/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE_GLOBAL')")
    public String listeCentres(Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) Long ville,
                               @RequestParam(required = false) Long specialite) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CentreExamenDTO> centres = centreExamenService.rechercherCentres(ville, specialite, pageable);

            model.addAttribute("centres", centres);
            model.addAttribute("villes", villeService.listerToutesLesVilles());
            model.addAttribute("specialites", specialiteService.listerToutesLesSpecialites());
            model.addAttribute("currentPage", page);
            model.addAttribute("villeId", ville);
            model.addAttribute("specialiteId", specialite);

        } catch (Exception e) {
            log.error("Erreur lors du chargement des centres", e);
            model.addAttribute("error", "Erreur lors du chargement des centres : " + e.getMessage());
            model.addAttribute("centres", Page.empty());
            model.addAttribute("villes", List.of());
            model.addAttribute("specialites", List.of());
        }

        return "gestionnaire-global/gestion-centres/list";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        try {
            String username = principal.getName();
            Long userId = utilisateurService.getUserIdByUsername(username);
            UtilisateurDTO utilisateur = utilisateurService.getUtilisateurById(userId);
            model.addAttribute("utilisateur", utilisateur);
            return "gestionnaire-global/profile";
        } catch (BusinessException e) {
            log.error("Utilisateur non trouvé", e);
            return "redirect:/login?error=userNotFound";
        }
    }

    @PostMapping("/profile/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE_GLOBAL')")
    public String updateProfile(@Valid @ModelAttribute UtilisateurDTO utilisateurDTO,
                                BindingResult bindingResult,
                                @RequestParam String currentPassword,
                                @RequestParam(required = false) String newPassword,
                                @RequestParam(required = false) String confirmPassword,
                                RedirectAttributes redirectAttributes,
                                Principal principal,
                                HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Données du formulaire invalides");
            return "redirect:/gestionnaire-global/profile";
        }

        try {
            // Récupérer l'utilisateur actuel par ID plutôt que par username
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();

            // Récupérer l'utilisateur avec l'ancien nom avant la modification
            UtilisateurDTO currentUser = utilisateurService.getUtilisateurByUsername(currentUsername);

            // Vérifier le mot de passe actuel
            String encodedPassword = utilisateurService.getEncodedPassword(currentUser.getId());
            if (!passwordEncoder.matches(currentPassword, encodedPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mot de passe actuel incorrect");
                return "redirect:/gestionnaire-global/profile";
            }

            // Vérifier la correspondance des nouveaux mots de passe
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                if (!newPassword.equals(confirmPassword)) {
                    redirectAttributes.addFlashAttribute("error", "Les nouveaux mots de passe ne correspondent pas");
                    return "redirect:/gestionnaire-global/profile";
                }
                currentUser.setPassword(newPassword);
            }

            // Mettre à jour seulement les champs autorisés
            currentUser.setEmail(utilisateurDTO.getEmail());
            currentUser.setUsername(utilisateurDTO.getUsername());

            utilisateurService.modifierUtilisateur(currentUser.getId(), currentUser);
            redirectAttributes.addFlashAttribute("success", "Profil mis à jour avec succès");

            // Si le nom d'utilisateur a changé, forcer une nouvelle authentification
            if (!currentUsername.equals(utilisateurDTO.getUsername())) {
                new SecurityContextLogoutHandler().logout(request, null, null);
                return "redirect:/login?profileUpdated=true";
            }

        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du profil", e);
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour: " + e.getMessage());
        }
        return "redirect:/gestionnaire-global/profile";
    }

    @GetMapping("/reporting/statistiques")
    public String statistiques(Model model) {
        StatistiquesDTO stats = statistiquesService.getStatistiquesGlobales();
        model.addAttribute("stats", stats);

        // Préparer les données pour les graphiques
        model.addAttribute("concoursLabels", new ArrayList<>(stats.getCandidaturesParConcours().keySet()));
        model.addAttribute("concoursData", new ArrayList<>(stats.getCandidaturesParConcours().values()));

        model.addAttribute("specialiteLabels", new ArrayList<>(stats.getCandidaturesParSpecialite().keySet()));
        model.addAttribute("specialiteData", new ArrayList<>(stats.getCandidaturesParSpecialite().values()));

        model.addAttribute("moisLabels", new ArrayList<>(stats.getCandidaturesParMois().keySet()));
        model.addAttribute("moisData", new ArrayList<>(stats.getCandidaturesParMois().values()));

        return "gestionnaire-global/reporting/statistiques";
    }

    @GetMapping("/reporting/statistiques/refresh")
    public String refreshStatistiques(RedirectAttributes redirectAttributes) {
        statistiquesService.invaliderCacheStatistiques();
        redirectAttributes.addFlashAttribute("success", "Statistiques actualisées avec succès");
        return "redirect:/gestionnaire-global/reporting/statistiques";
    }

    @GetMapping("/reporting/statistiques/export/excel")
    public ResponseEntity<Resource> exportExcel() throws IOException {
        StatistiquesDTO stats = statistiquesService.getStatistiquesGlobales();
        ByteArrayInputStream in = exportService.exportToExcel(stats);

        String filename = "statistiques_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/reporting/statistiques/export/pdf")
    public ResponseEntity<Resource> exportPdf() throws IOException {
        StatistiquesDTO stats = statistiquesService.getStatistiquesGlobales();
        ByteArrayInputStream in = exportService.exportToPdf(stats);

        String filename = "statistiques_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")) + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(in));
    }

    @ExceptionHandler(IOException.class)
    public String handleExportException(IOException e, RedirectAttributes redirectAttributes) {
        log.error("Erreur lors de l'export des statistiques", e);
        redirectAttributes.addFlashAttribute("error", "Erreur lors de l'export: " + e.getMessage());
        return "redirect:/gestionnaire-global/reporting/statistiques";
    }

    // Méthodes utilitaires pour la navigation depuis le dashboard
    @GetMapping("/concours")
    public String concours(Model model) {
        return "redirect:/gestionnaire-global/gestion-concours/list";
    }


    @GetMapping("/centres")
    public String centres(Model model) {
        return "redirect:/gestionnaire-global/gestion-centres/list";
    }

    @GetMapping("/statistiques")
    public String statistiquesRedirect(Model model) {
        return "redirect:/gestionnaire-global/reporting/statistiques";
    }

    @GetMapping("/gestion-centres/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE_GLOBAL')")
    public String ajouterCentre(Model model) {
        model.addAttribute("centre", new CentreExamenDTO());
        model.addAttribute("villes", villeService.listerToutesLesVilles());
        model.addAttribute("specialites", specialiteService.listerToutesLesSpecialites());
        return "gestionnaire-global/gestion-centres/add";
    }

    @PostMapping("/gestion-centres/save")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE_GLOBAL')")
    public String sauvegarderCentre(@Valid @ModelAttribute("centre") CentreExamenDTO centreDTO,
                                    BindingResult result,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {

        // Réinjecter les données nécessaires pour le formulaire en cas d'erreur
        model.addAttribute("villes", villeService.listerToutesLesVilles());
        model.addAttribute("specialites", specialiteService.listerToutesLesSpecialites());

        if (result.hasErrors()) {
            return "gestionnaire-global/gestion-centres/add";
        }

        try {
            centreExamenService.creerCentre(centreDTO);
            redirectAttributes.addFlashAttribute("success", "Centre créé avec succès !");
            return "redirect:/gestionnaire-global/gestion-centres/list";
        } catch (Exception e) {
            log.error("Erreur lors de la création du centre", e);
            model.addAttribute("error", "Erreur lors de la création : " + e.getMessage());
            return "gestionnaire-global/gestion-centres/add";
        }
    }

    @GetMapping("/gestion-centres/edit/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE_GLOBAL')")
    public String modifierCentre(@PathVariable Long id, Model model) {
        try {
            CentreExamenDTO centreDTO = centreExamenService.getCentreExamenById(id);
            model.addAttribute("centre", centreDTO);
            model.addAttribute("villes", villeService.listerToutesLesVilles());
            model.addAttribute("specialites", specialiteService.listerToutesLesSpecialites());
            return "gestionnaire-global/gestion-centres/edit";
        } catch (BusinessException e) {
            log.error("Centre non trouvé avec l'ID: {}", id, e);
            return "redirect:/gestionnaire-global/gestion-centres/list";
        }
    }

    @PostMapping("/gestion-centres/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE_GLOBAL')")
    public String updateCentre(@PathVariable Long id,
                               @Valid @ModelAttribute("centre") CentreExamenDTO centreDTO,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        // Réinjecter les données nécessaires pour le formulaire en cas d'erreur
        model.addAttribute("villes", villeService.listerToutesLesVilles());
        model.addAttribute("specialites", specialiteService.listerToutesLesSpecialites());

        if (result.hasErrors()) {
            return "gestionnaire-global/gestion-centres/edit";
        }

        try {
            centreExamenService.modifierCentre(id, centreDTO);
            redirectAttributes.addFlashAttribute("success", "Centre modifié avec succès !");
            return "redirect:/gestionnaire-global/gestion-centres/list";
        } catch (Exception e) {
            log.error("Erreur lors de la modification du centre", e);
            model.addAttribute("error", "Erreur lors de la modification : " + e.getMessage());
            model.addAttribute("centre", centreDTO);
            return "gestionnaire-global/gestion-centres/edit";
        }
    }

    @PostMapping("/gestion-centres/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE_GLOBAL')")
    public String supprimerCentre(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            centreExamenService.supprimerCentre(id);
            redirectAttributes.addFlashAttribute("success", "Centre supprimé avec succès !");
        } catch (BusinessException e) {
            log.error("Erreur lors de la suppression du centre", e);
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression : " + e.getMessage());
        }
        return "redirect:/gestionnaire-global/gestion-centres/list";
    }

    /**
     * Liste des candidatures avec filtres pour gestionnaire global
     */
    @GetMapping("/gestion-candidatures/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE_GLOBAL')")
    public String listeCandidatures(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    @RequestParam(required = false) Long concoursId,
                                    @RequestParam(required = false) Long specialiteId,
                                    @RequestParam(required = false) Long centreId,
                                    @RequestParam(required = false) String statut,
                                    @RequestParam(required = false) String diplome,
                                    Model model,
                                    Authentication authentication) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CandidatureDTO> candidatures;

            log.info("Filtres - concoursId: {}, specialiteId: {}, centreId: {}, statut: {}, diplome: {}",
                    concoursId, specialiteId, centreId, statut, diplome);

            // Utiliser la méthode de service avec tous les filtres
            candidatures = candidatureService.getCandidaturesWithFilters(
                    concoursId, specialiteId, centreId, statut, diplome, pageable);

            log.info("Candidatures trouvées: {}", candidatures.getTotalElements());

            // Charger les données pour les filtres
            model.addAttribute("candidatures", candidatures);
            model.addAttribute("concoursList", concoursService.getAllConcours(PageRequest.of(0, 100)).getContent());
            model.addAttribute("specialites", specialiteService.listerToutesLesSpecialites());
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

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des candidatures", e);
            model.addAttribute("error", "Erreur lors du chargement des candidatures");
            model.addAttribute("candidatures", Page.empty());
        }

        return "gestionnaire-global/gestion-candidatures/list";
    }

    /**
     * Détails d'une candidature
     */
    @GetMapping("/gestion-candidatures/details/{numero}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE_GLOBAL')")
    public String detailsCandidature(@PathVariable String numero, Model model) {
        try {
            CandidatureDTO candidature = candidatureService.getCandidatureByNumero(numero);
            model.addAttribute("candidature", candidature);
            return "gestionnaire-global/gestion-candidatures/detail";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/gestionnaire-global/gestion-candidatures/list";
        }
    }

    /**
     * Formulaire d'édition d'une candidature
     */
    @GetMapping("/gestion-candidatures/edit/{numero}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE_GLOBAL')")
    public String editCandidature(@PathVariable String numero, Model model) {
        try {
            CandidatureDTO candidature = candidatureService.getCandidatureByNumero(numero);

            model.addAttribute("candidature", candidature);
            model.addAttribute("statuts", StatutCandidature.values());

            return "gestionnaire-global/gestion-candidatures/edit";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/gestionnaire-global/gestion-candidatures/list";
        }
    }

    /**
     * Validation d'une candidature
     */
    @PostMapping("/gestion-candidatures/valider/{numero}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE_GLOBAL')")
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

        return "redirect:/gestionnaire-global/gestion-candidatures/details/" + numero;
    }

    /**
     * Rejet d'une candidature
     */
    @PostMapping("/gestion-candidatures/rejeter/{numero}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE_GLOBAL')")
    public String rejeterCandidature(@PathVariable String numero,
                                     @RequestParam String motif,
                                     RedirectAttributes redirectAttributes,
                                     Authentication authentication) {
        try {
            if (motif == null || motif.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Le motif de rejet est obligatoire");
                return "redirect:/gestionnaire-global/gestion-candidatures/details/" + numero;
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

        return "redirect:/gestionnaire-global/gestion-candidatures/details/" + numero;
    }

    /**
     * Téléchargement d'un document
     */
    @GetMapping("/gestion-candidatures/documents/download/{documentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE_GLOBAL')")
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
    @GetMapping("/gestion-candidatures/documents/view/{documentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE_GLOBAL')")
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
    @PostMapping("/gestion-candidatures/delete/{numero}")
    @PreAuthorize("hasRole('ADMIN')")
    public String supprimerCandidature(@PathVariable String numero,
                                       RedirectAttributes redirectAttributes) {
        try {
            candidatureService.supprimerCandidature(numero);
            redirectAttributes.addFlashAttribute("success",
                    "Candidature " + numero + " supprimée avec succès");

        } catch (Exception e) {
            log.error("Erreur lors de la suppression de la candidature " + numero, e);
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la suppression : " + e.getMessage());
        }

        return "redirect:/gestionnaire-global/gestion-candidatures/list";
    }

    /**
     * Dashboard des candidatures pour un centre
     */
    @GetMapping("/gestion-candidatures/dashboard/{centreId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE_GLOBAL')")
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

            return "gestionnaire-global/gestion-candidatures/dashboard-centre";

        } catch (Exception e) {
            log.error("Erreur lors du chargement du dashboard centre " + centreId, e);
            model.addAttribute("error", "Erreur lors du chargement du dashboard");
            return "redirect:/gestionnaire-global/gestion-candidatures/list";
        }
    }
}