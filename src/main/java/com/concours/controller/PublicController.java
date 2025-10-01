package com.concours.controller;

import com.concours.dto.*;
import com.concours.entity.CentreExamen;
import com.concours.entity.Concours;
import com.concours.entity.Specialite;
import com.concours.entity.Ville;
import com.concours.exception.BusinessException;
import com.concours.mapper.CentreExamenMapper;
import com.concours.service.CandidatureService;
import com.concours.service.CentreExamenService;
import com.concours.service.ConcoursService;
import com.concours.service.SpecialiteService;
import com.concours.service.StatistiquesService;
import com.concours.service.VilleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import java.util.*;

@Controller
@RequestMapping("/public")
@RequiredArgsConstructor
@Slf4j
public class PublicController {

    private final ConcoursService concoursService;
    private final CandidatureService candidatureService;
    private final VilleService villeService;
    private final SpecialiteService specialiteService;
    private final CentreExamenService centreExamenService;
    private final StatistiquesService statistiquesService;
    private final CentreExamenMapper centreExamenMapper;

    @GetMapping({"", "/"})
    public String accueil(Model model) {
        try {
            // Récupération des concours ouverts (limités à 6 pour l'affichage)
            List<ConcoursDTO> concoursOuverts = concoursService.getConcoursOuverts();
            List<ConcoursDTO> concoursAffiches = concoursOuverts.size() > 6 ?
                    concoursOuverts.subList(0, 6) : concoursOuverts;

            // Utiliser le service de statistiques
            StatistiquesDTO stats = statistiquesService.getStatistiquesAdmin();

            // Compléter avec les données spécifiques à la page d'accueil
            stats.setNbConcours(concoursOuverts.size()); // Nombre de concours ouverts

            // Calcul du nombre de postes disponibles pour les concours ouverts
            int totalPostes = concoursOuverts.stream()
                    .mapToInt(ConcoursDTO::getNbPostes)
                    .sum();
            stats.setNbPostes(totalPostes);

            model.addAttribute("concours", concoursAffiches);
            model.addAttribute("stats", stats);
            model.addAttribute("pageTitle", "Accueil - Portail des Concours");

            log.info("Page d'accueil chargée avec {} concours ouverts", concoursOuverts.size());

        } catch (Exception e) {
            log.error("Erreur lors du chargement de la page d'accueil", e);
            model.addAttribute("concours", List.of());
            // Fournir des statistiques par défaut en cas d'erreur
            StatistiquesDTO defaultStats = new StatistiquesDTO();
            defaultStats.setNbConcours(0);
            defaultStats.setNbCandidatures(0);
            defaultStats.setNbPostes(0);
            model.addAttribute("stats", defaultStats);
            model.addAttribute("error", "Erreur lors du chargement des données");
        }

        return "public/index";
    }

    @GetMapping("/concours")
    public String listeConcours(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "tous") String statut,
            Model model) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ConcoursDTO> concours = null;

            if (search != null && !search.trim().isEmpty()) {
                concours = concoursService.searchConcours(search.trim(), pageable);
                model.addAttribute("search", search);
            } else if ("ouverts".equals(statut)) {
                // Filtrer uniquement les concours ouverts
                List<ConcoursDTO> concoursOuverts = concoursService.getConcoursOuverts();
                // Simuler une page pour les concours ouverts
                int start = Math.min(page * size, concoursOuverts.size());
                int end = Math.min(start + size, concoursOuverts.size());
                List<ConcoursDTO> concoursPagines = concoursOuverts.subList(start, end);

                // Créer une page manuelle (approximation)
                model.addAttribute("concours", concoursPagines);
                model.addAttribute("currentPage", page);
                model.addAttribute("totalPages", (int) Math.ceil((double) concoursOuverts.size() / size));
                model.addAttribute("totalElements", concoursOuverts.size());
            } else {
                concours = concoursService.getAllConcoursPublies(pageable);
                model.addAttribute("concours", concours.getContent());
                model.addAttribute("currentPage", concours.getNumber());
                model.addAttribute("totalPages", concours.getTotalPages());
                model.addAttribute("totalElements", concours.getTotalElements());
            }

            if (!"ouverts".equals(statut)) {
                model.addAttribute("concours", concours.getContent());
                model.addAttribute("currentPage", concours.getNumber());
                model.addAttribute("totalPages", concours.getTotalPages());
                model.addAttribute("totalElements", concours.getTotalElements());
            }

            model.addAttribute("statut", statut);
            model.addAttribute("pageTitle", "Liste des Concours");

        } catch (Exception e) {
            log.error("Erreur lors du chargement des concours", e);
            model.addAttribute("concours", List.of());
            model.addAttribute("error", "Erreur lors du chargement des concours");
        }

        return "public/concours/list";
    }

    @GetMapping("/concours/{id}")
    public String detailsConcours(@PathVariable Long id, Model model) {
        try {
            ConcoursDTO concours = concoursService.getConcoursById(id);

            model.addAttribute("concours", concours);
            model.addAttribute("pageTitle", concours.getTitre());

        } catch (BusinessException e) {
            log.error("Concours non trouvé: {}", id);
            model.addAttribute("error", "Concours non trouvé");
            return "redirect:/public/concours";
        } catch (Exception e) {
            log.error("Erreur lors du chargement des détails du concours {}", id, e);
            model.addAttribute("error", "Erreur lors du chargement du concours");
            return "redirect:/public/concours";
        }

        return "public/concours/details";
    }

    @GetMapping("/candidature")
    public String formulaireCandidature(Model model) {
        try {
            // Charger les données nécessaires pour le formulaire
            List<Ville> villes = villeService.getAllVilles();
            List<ConcoursDTO> concours = concoursService.getConcoursOuverts();

            // Récupérer les spécialités et centres uniquement des concours ouverts
            Set<SpecialiteDTO> specialitesSet = new HashSet<>();
            Set<CentreExamenDTO> centresSet = new HashSet<>();

            for (ConcoursDTO concoursDTO : concours) {
                // Ajouter les spécialités du concours
                if (concoursDTO.getSpecialites() != null) {
                    specialitesSet.addAll(concoursDTO.getSpecialites());
                }

                // Ajouter les centres du concours (via l'entité pour accéder aux centres)
                try {
                    Concours concoursEntity = concoursService.getConcoursEntityById(concoursDTO.getId());
                    if (concoursEntity.getCentresExamen() != null) {
                        for (CentreExamen centre : concoursEntity.getCentresExamen()) {
                            if (centre.isActif()) { // Ne prendre que les centres actifs
                                centresSet.add(centreExamenMapper.toDTO(centre));
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Erreur lors du chargement des centres pour le concours {}", concoursDTO.getId(), e);
                }
            }

            // Convertir en listes
            List<SpecialiteDTO> specialites = new ArrayList<>(specialitesSet);
            List<CentreExamenDTO> centres = new ArrayList<>(centresSet);

            // Trier les listes pour un affichage cohérent
            specialites.sort((s1, s2) -> s1.getLibelle().compareTo(s2.getLibelle()));
            centres.sort((c1, c2) -> c1.getCode().compareTo(c2.getCode()));

            model.addAttribute("candidatureDto", new CandidatureCreateDTO());
            model.addAttribute("villes", villes);
            model.addAttribute("concoursList", concours);
            model.addAttribute("centres", centres);
            model.addAttribute("specialites", specialites);
            model.addAttribute("pageTitle", "Formulaire de Candidature");

            log.info("Formulaire chargé avec {} concours, {} spécialités, {} centres",
                    concours.size(), specialites.size(), centres.size());

        } catch (Exception e) {
            log.error("Erreur lors du chargement du formulaire de candidature", e);
            model.addAttribute("error", "Erreur lors du chargement du formulaire");
        }

        return "public/candidature/form";
    }

    /**
     * Version modifiée : plus de gestion de fichiers
     * Seules les informations du formulaire sont traitées
     */
    @PostMapping("/candidature")
    public String soumettreCandidature(
            @Valid @ModelAttribute("candidatureDto") CandidatureCreateDTO candidatureDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        log.info("=== DÉBUT TRAITEMENT CANDIDATURE (SANS FICHIERS) ===");
        log.info("CIN: {}, Nom: {}, Prénom: {}", candidatureDto.getCin(), candidatureDto.getNom(), candidatureDto.getPrenom());

        try {
            // Validation du formulaire
            if (bindingResult.hasErrors()) {
                log.warn("Erreurs de validation: {}", bindingResult.getAllErrors());
                chargerDonneesFormulaire(model);
                return "public/candidature/form";
            }

            // Soumission de la candidature (version sans fichiers)
            String numeroCandidature = candidatureService.soumettreCandiature(candidatureDto);

            log.info("Candidature créée sans fichiers: {}", numeroCandidature);

            redirectAttributes.addFlashAttribute("success",
                    "Candidature soumise avec succès. Numéro: " + numeroCandidature);
            redirectAttributes.addFlashAttribute("numero", numeroCandidature);

            return "redirect:/public/candidature/success";

        } catch (BusinessException e) {
            log.warn("Erreur métier: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            chargerDonneesFormulaire(model);
            return "public/candidature/form";

        } catch (Exception e) {
            log.error("Erreur technique", e);
            model.addAttribute("error", "Erreur technique: " + e.getMessage());
            chargerDonneesFormulaire(model);
            return "public/candidature/form";
        }
    }

    @GetMapping("/candidature/success")
    public String successCandidature(Model model) {
        model.addAttribute("pageTitle", "Candidature Soumise");
        return "public/candidature/success";
    }

    @GetMapping("/suivi")
    public String suiviCandidature(Model model) {
        model.addAttribute("pageTitle", "Suivi de Candidature");
        return "public/candidature/suivi-form";
    }

    @PostMapping("/suivi")
    public String rechercherCandidature(
            @RequestParam String numero,
            @RequestParam String cin,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            if (numero == null || numero.trim().isEmpty() ||
                    cin == null || cin.trim().isEmpty()) {
                model.addAttribute("error", "Le numéro de candidature et le CIN sont obligatoires");
                return "public/candidature/suivi-form";
            }

            CandidatureDTO candidature = candidatureService.getCandidatureByNumero(numero.trim());

            // Vérifier que le CIN correspond
            if (!candidature.getCandidatCin().equalsIgnoreCase(cin.trim())) {
                model.addAttribute("error", "Numéro de candidature ou CIN incorrect");
                return "public/candidature/suivi-form";
            }

            model.addAttribute("candidature", candidature);
            model.addAttribute("pageTitle", "Détails de la Candidature");
            return "public/candidature/suivi-resultat";

        } catch (BusinessException e) {
            log.warn("Candidature non trouvée: {}", numero);
            model.addAttribute("error", "Candidature non trouvée");
            return "public/candidature/suivi-form";

        } catch (Exception e) {
            log.error("Erreur lors de la recherche de candidature", e);
            model.addAttribute("error", "Erreur lors de la recherche");
            return "public/candidature/suivi-form";
        }
    }

    // === Méthodes utilitaires privées ===

    private void chargerDonneesFormulaire(Model model) {
        try {
            List<Ville> villes = villeService.getAllVilles();
            List<ConcoursDTO> concours = concoursService.getConcoursOuverts();
            List<SpecialiteDTO> specialites = specialiteService.getAllSpecialites();
            List<CentreExamen> centres = centreExamenService.findAll();

            model.addAttribute("villes", villes);
            model.addAttribute("concoursList", concours);
            model.addAttribute("specialites", specialites);
            model.addAttribute("centres", centres);
        } catch (Exception e) {
            log.error("Erreur lors du chargement des données du formulaire", e);
        }
    }

    // === API endpoints pour AJAX ===

    @GetMapping("/api/centres/ville/{villeId}")
    @ResponseBody
    public List<CentreExamenDTO> getCentresParVille(@PathVariable Long villeId) {
        try {
            return centreExamenService.getCentresParVille(villeId);
        } catch (Exception e) {
            log.error("Erreur lors du chargement des centres pour la ville {}", villeId, e);
            return List.of();
        }
    }

    @GetMapping("/api/specialites/{concoursId}")
    @ResponseBody
    public List<SpecialiteDTO> getSpecialitesParConcours(@PathVariable Long concoursId) {
        try {
            ConcoursDTO concours = concoursService.getConcoursById(concoursId);
            return concours.getSpecialites();
        } catch (Exception e) {
            log.error("Erreur lors du chargement des spécialités pour le concours {}", concoursId, e);
            return List.of();
        }
    }

    @GetMapping("/api/centres/concours/{concoursId}")
    @ResponseBody
    public List<CentreExamenDTO> getCentresParConcours(@PathVariable Long concoursId) {
        try {
            Concours concours = concoursService.getConcoursEntityById(concoursId);
            return concours.getCentresExamen().stream()
                    .filter(CentreExamen::isActif) // Filtrer les centres actifs
                    .map(centreExamenMapper::toDTO)
                    .toList();
        } catch (Exception e) {
            log.error("Erreur lors du chargement des centres pour le concours {}", concoursId, e);
            return List.of();
        }
    }

    @GetMapping("/candidature/{numero}")
    public String detailsCandidature(@PathVariable String numero, Model model) {
        try {
            CandidatureDTO candidature = candidatureService.getCandidatureByNumero(numero);
            model.addAttribute("candidature", candidature);
            model.addAttribute("pageTitle", "Détails de la Candidature");
            return "public/candidature/details";
        } catch (BusinessException e) {
            log.warn("Candidature non trouvée: {}", numero);
            return "redirect:/public/suivi";
        } catch (Exception e) {
            log.error("Erreur lors du chargement des détails de candidature {}", numero, e);
            return "redirect:/public/suivi";
        }
    }
    @GetMapping("/candidature/recu/{numero}")
    public void telechargerRecu(@PathVariable String numero, HttpServletResponse response) {
        try {
            CandidatureDTO candidature = candidatureService.getCandidatureByNumero(numero);

            // Configurer la réponse HTTP
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=recu-candidature-" + numero + ".pdf");

            // Générer un PDF simple avec les informations de la candidature
            // (Vous devrez implémenter cette logique ou utiliser une bibliothèque comme iText)
            String contenu = "Récépissé de Candidature\n\n" +
                    "Numéro: " + candidature.getNumero() + "\n" +
                    "Nom: " + candidature.getCandidatNom() + " " + candidature.getCandidatPrenom() + "\n" +
                    "CIN: " + candidature.getCandidatCin() + "\n" +
                    "Concours: " + candidature.getConcoursTitre() + "\n" +
                    "Date de dépôt: " + candidature.getDateDepot() + "\n\n" +
                    "Ce document atteste du dépôt de votre candidature.";

            response.getOutputStream().write(contenu.getBytes());
            response.getOutputStream().flush();

        } catch (Exception e) {
            log.error("Erreur lors de la génération du récépissé", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}