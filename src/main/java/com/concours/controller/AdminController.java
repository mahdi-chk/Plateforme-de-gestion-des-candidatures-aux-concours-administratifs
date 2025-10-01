package com.concours.controller;

import com.concours.exception.BusinessException;
import com.concours.service.CentreExamenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.concours.service.StatistiquesService;
import com.concours.dto.StatistiquesDTO;
import com.concours.service.UtilisateurService;
import com.concours.dto.UtilisateurDTO;

import java.security.Principal;
import java.util.ArrayList;

import com.concours.service.ExportService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private StatistiquesService statistiquesService;

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CentreExamenService centreExamenService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        StatistiquesDTO stats = statistiquesService.getStatistiquesAdmin();
        model.addAttribute("stats", stats);
        return "admin/dashboard";
    }

    @GetMapping("/gestion-utilisateurs/list")
    public String listeUtilisateurs(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) String username,
                                   @RequestParam(required = false) String role,
                                   Model model) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<UtilisateurDTO> utilisateurs;
        
        if ((username != null && !username.trim().isEmpty()) || 
            (role != null && !role.trim().isEmpty())) {
            utilisateurs = utilisateurService.rechercherUtilisateurs(username, role, pageable);
        } else {
            utilisateurs = utilisateurService.getAllUtilisateurs(pageable);
        }
        
        model.addAttribute("utilisateurs", utilisateurs);
        model.addAttribute("username", username);
        model.addAttribute("role", role);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", utilisateurs.getTotalPages());
        
        return "admin/gestion-utilisateurs/list";
    }

    @GetMapping("/gestion-utilisateurs/add")
    public String addUtilisateur(Model model) {
        model.addAttribute("utilisateur", new UtilisateurDTO());
        // Ajouter la liste des centres pour la sélection
        model.addAttribute("centres", centreExamenService.getCentresActifs());
        return "admin/gestion-utilisateurs/add";
    }

    @PostMapping("/utilisateurs/add")
    public String createUtilisateur(@ModelAttribute UtilisateurDTO utilisateurDTO,
                                   RedirectAttributes redirectAttributes) {
        try {
            utilisateurService.creerUtilisateur(utilisateurDTO);
            redirectAttributes.addFlashAttribute("success", "Utilisateur créé avec succès");
            return "redirect:/admin/gestion-utilisateurs/list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création : " + e.getMessage());
            return "redirect:/admin/gestion-utilisateurs/add";
        }
    }

    @GetMapping("/gestion-concours/list")
    public String listeConcours(Model model) {
        return "admin/gestion-concours/list";
    }


//    @GetMapping("/gestion-candidatures/list")
//    public String listeCandidatures(Model model) {
//        return "admin/candidatures/list";
//    }

    @GetMapping("/gestion-centres/list")
    public String listeCentres(Model model) {
        return "admin/centres/list";
    }

    @GetMapping("/utilisateurs/edit/{id}")
    public String editUtilisateur(@PathVariable Long id, Model model) {
        UtilisateurDTO utilisateur = utilisateurService.getUtilisateurById(id);
        model.addAttribute("utilisateur", utilisateur);
        // Ajouter la liste des centres pour la sélection
        model.addAttribute("centres", centreExamenService.getCentresActifs());
        return "admin/gestion-utilisateurs/edit";
    }

    @PostMapping("/utilisateurs/delete/{id}")
    public String deleteUtilisateur(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            utilisateurService.supprimerUtilisateur(id);
            redirectAttributes.addFlashAttribute("success", "Utilisateur supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression : " + e.getMessage());
        }
        return "redirect:/admin/gestion-utilisateurs/list";
    }

    @PostMapping("/utilisateurs/edit/{id}")
    public String updateUtilisateur(@PathVariable Long id, 
                                   @ModelAttribute UtilisateurDTO utilisateurDTO,
                                   @RequestParam(required = false) String newPassword,
                                   RedirectAttributes redirectAttributes) {
        try {
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                utilisateurDTO.setPassword(newPassword);
            }
            utilisateurService.modifierUtilisateur(id, utilisateurDTO);
            redirectAttributes.addFlashAttribute("success", "Utilisateur modifié avec succès");
            return "redirect:/admin/gestion-utilisateurs/list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la modification : " + e.getMessage());
            return "redirect:/admin/utilisateurs/edit/" + id;
        }
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        try {
            String username = principal.getName();
            Long userId = utilisateurService.getUserIdByUsername(username);
            UtilisateurDTO utilisateur = utilisateurService.getUtilisateurById(userId);
            model.addAttribute("utilisateur", utilisateur);
            return "admin/profile";
        } catch (BusinessException e) {
            // Rediriger vers la page de login si l'utilisateur n'est pas trouvé
            return "redirect:/login?error=userNotFound";
        }
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute UtilisateurDTO utilisateurDTO,
                                @RequestParam String currentPassword,
                                @RequestParam(required = false) String newPassword,
                                @RequestParam(required = false) String confirmPassword,
                                RedirectAttributes redirectAttributes,
                                Principal principal,
                                HttpServletRequest request) {
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
                return "redirect:/admin/profile";
            }

            // Vérifier la correspondance des nouveaux mots de passe
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                if (!newPassword.equals(confirmPassword)) {
                    redirectAttributes.addFlashAttribute("error", "Les nouveaux mots de passe ne correspondent pas");
                    return "redirect:/admin/profile";
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
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour: " + e.getMessage());
        }
        return "redirect:/admin/profile";
    }


    @GetMapping("/reporting/statistiques")
    public String statistiques(Model model) {
        try {
            log.info("Chargement de la page des statistiques");

            // Récupérer les statistiques globales
            StatistiquesDTO stats = statistiquesService.getStatistiquesGlobales();
            model.addAttribute("stats", stats);

            // Préparer les données pour les graphiques avec gestion des valeurs nulles
            Map<String, Long> candidaturesParConcours = stats.getCandidaturesParConcours();
            if (candidaturesParConcours != null && !candidaturesParConcours.isEmpty()) {
                model.addAttribute("concoursLabels", candidaturesParConcours.keySet());
                model.addAttribute("concoursData", candidaturesParConcours.values());
            } else {
                model.addAttribute("concoursLabels", Arrays.asList("Aucun concours"));
                model.addAttribute("concoursData", Arrays.asList(0L));
            }

            Map<String, Long> candidaturesParSpecialite = stats.getCandidaturesParSpecialite();
            if (candidaturesParSpecialite != null && !candidaturesParSpecialite.isEmpty()) {
                model.addAttribute("specialiteLabels", candidaturesParSpecialite.keySet());
                model.addAttribute("specialiteData", candidaturesParSpecialite.values());
            } else {
                model.addAttribute("specialiteLabels", Arrays.asList("Aucune spécialité"));
                model.addAttribute("specialiteData", Arrays.asList(0L));
            }

            Map<String, Long> candidaturesParMois = stats.getCandidaturesParMois();
            if (candidaturesParMois != null && !candidaturesParMois.isEmpty()) {
                model.addAttribute("moisLabels", candidaturesParMois.keySet());
                model.addAttribute("moisData", candidaturesParMois.values());
            } else {
                model.addAttribute("moisLabels", Arrays.asList("Aucune donnée"));
                model.addAttribute("moisData", Arrays.asList(0L));
            }

            log.info("Statistiques chargées avec succès: {} candidatures, {} concours",
                    stats.getTotalCandidatures(), stats.getNbConcours());

        } catch (Exception e) {
            log.error("Erreur lors du chargement des statistiques", e);

            // Initialiser avec des valeurs par défaut en cas d'erreur
            StatistiquesDTO defaultStats = new StatistiquesDTO();
            defaultStats.setNbConcours(0);
            defaultStats.setTotalCandidatures(0);
            defaultStats.setNbUtilisateurs(0);
            defaultStats.setNbCentres(0L);
            defaultStats.setCandidaturesValidees(0);
            defaultStats.setCandidaturesEnAttente(0);
            defaultStats.setCandidaturesRejetees(0);

            model.addAttribute("stats", defaultStats);
            model.addAttribute("concoursLabels", Arrays.asList("Erreur de chargement"));
            model.addAttribute("concoursData", Arrays.asList(0L));
            model.addAttribute("specialiteLabels", Arrays.asList("Erreur de chargement"));
            model.addAttribute("specialiteData", Arrays.asList(0L));
            model.addAttribute("moisLabels", Arrays.asList("Erreur de chargement"));
            model.addAttribute("moisData", Arrays.asList(0L));

            model.addAttribute("error", "Erreur lors du chargement des statistiques: " + e.getMessage());
        }

        return "admin/reporting/statistiques";
    }

    private void initializeEmptyStats(StatistiquesDTO stats) {
        stats.setNbConcours(0L);
        stats.setTotalCandidatures(0L);
        stats.setNbUtilisateurs(0L);
        stats.setNbCentres(0L);
        stats.setCandidaturesValidees(0L);
        stats.setCandidaturesEnAttente(0L);
        stats.setCandidaturesRejetees(0L);
        stats.setCandidaturesParConcours(new HashMap<>());
        stats.setCandidaturesParSpecialite(new HashMap<>());
        stats.setCandidaturesParCentre(new HashMap<>());
        stats.setCandidaturesParMois(new HashMap<>());
    }

    @GetMapping("/reporting/statistiques/refresh")
    public String refreshStatistiques(RedirectAttributes redirectAttributes) {
        try {
            statistiquesService.invaliderCacheStatistiques();
            redirectAttributes.addFlashAttribute("success", "Statistiques actualisées avec succès");
        } catch (Exception e) {
            log.error("Erreur lors de l'actualisation des statistiques", e);
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'actualisation : " + e.getMessage());
        }
        return "redirect:/admin/reporting/statistiques";
    }

    @Autowired
    private ExportService exportService;

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
        return "redirect:/admin/reporting/statistiques";
    }
}
