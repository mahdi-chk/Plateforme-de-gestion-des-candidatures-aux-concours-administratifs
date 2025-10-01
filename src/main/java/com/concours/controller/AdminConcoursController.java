package com.concours.controller;


import com.concours.dto.CentreExamenDTO;
import com.concours.dto.ConcoursDTO;
import com.concours.entity.CentreExamen;
import com.concours.entity.Concours;
import com.concours.exception.BusinessException;
import com.concours.repository.ConcoursRepository;
import com.concours.service.CentreExamenService;
import com.concours.service.ConcoursService;
import com.concours.service.SpecialiteService;
import com.concours.service.VilleService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/admin/concours")
@RequiredArgsConstructor
public class AdminConcoursController {

    private final ConcoursService concoursService;
    private final SpecialiteService specialiteService;
    private final VilleService villeService;
    private final CentreExamenService centreExamenService;
    private final ConcoursRepository concoursRepository;

    @GetMapping("/list")
    public String listeConcours(Model model,
                                @RequestParam(required = false) String search,
                                @RequestParam(required = false) String statut,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) {

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


        return "admin/gestion-concours/list";
    }

    @GetMapping("/add")
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
        return "admin/gestion-concours/add";
    }


    @PostMapping("/save")
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
            System.out.println(result);
            return "admin/gestion-concours/add";
        }

        try {
            concoursService.creerConcours(concoursDTO);
            redirectAttributes.addFlashAttribute("success", "Concours créé avec succès !");
            return "redirect:/admin/concours/list";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de la création : " + e.getMessage());
            return "admin/gestion-concours/add";
        }
    }

    public List<Concours> searchConcours(String searchTerm, Boolean publie) {
        if (publie != null) {
            return concoursRepository.findByTitreContainingOrReferenceContainingAndPublie(searchTerm, searchTerm, publie);
        } else {
            return concoursRepository.findByTitreContainingOrReferenceContaining(searchTerm, searchTerm);
        }
    }

    public List<Concours> findByPublie(Boolean publie) {
        return concoursRepository.findByPublie(publie);
    }

    @PostMapping("/delete/{id}")
    public String supprimerConcours(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            concoursService.supprimerConcours(id);
            redirectAttributes.addFlashAttribute("success", "Concours supprimé avec succès !");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression : " + e.getMessage());
        }
        return "redirect:/admin/concours/list";
    }


    @GetMapping("/details/{id}")
    public String detailsConcours(@PathVariable Long id, Model model) {
        try {
            ConcoursDTO concoursDTO = concoursService.getConcoursById(id);
            model.addAttribute("concours", concoursDTO);
            model.addAttribute("centres", centreExamenService.listerTousLesCentres()); // Cette ligne est cruciale
            return "admin/gestion-concours/details";
        } catch (BusinessException e) {
            return "redirect:/admin/concours/list";
        }
    }

    @GetMapping("/edit/{id}")
    public String modifierConcours(@PathVariable Long id, Model model) {
        try {
            // Récupérer l'entité Concours au lieu du DTO
            Concours concours = concoursService.getConcoursEntityById(id);

            // Convertir manuellement les centres d'examen en IDs pour le template
            List<Long> centreIds = new ArrayList<>();
            if (concours.getCentresExamen() != null) {
                centreIds = concours.getCentresExamen().stream()
                        .map(CentreExamen::getId)
                        .collect(Collectors.toList());
            }

            // Créer un ConcoursDTO pour le formulaire avec les IDs des centres
            ConcoursDTO concoursDTO = new ConcoursDTO();
            concoursDTO.setId(concours.getId());
            concoursDTO.setTitre(concours.getTitre());
            concoursDTO.setReference(concours.getReference());
            concoursDTO.setDateOuverture(concours.getDateOuverture());
            concoursDTO.setDateCloture(concours.getDateCloture());
            concoursDTO.setDateConcours(concours.getDateConcours());
            concoursDTO.setNbPostes(concours.getNbPostes());
            concoursDTO.setConditions(concours.getConditions());
            concoursDTO.setPublie(concours.isPublie());

            // Convertir les IDs en string séparés par des virgules
            if (!centreIds.isEmpty()) {
                String centresIdsStr = centreIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                concoursDTO.setCentresExamenIds(centresIdsStr);
            }

            model.addAttribute("concours", concoursDTO);
            model.addAttribute("specialites", specialiteService.listerToutesLesSpecialites());
            model.addAttribute("centres", centreExamenService.listerTousLesCentres());
            model.addAttribute("conditionsPredefinies", Arrays.asList(
                    "Être âgé de moins de 40 ans",
                    "Avoir un diplôme de niveau BAC+3",
                    "Avoir au moins 3 ans d'expérience",
                    "Être de nationalité marocaine"
            ));
            return "admin/gestion-concours/edit";
        } catch (BusinessException e) {
            return "redirect:/admin/concours/list";
        }
    }

    @PostMapping("/update/{id}")
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
            return "admin/gestion-concours/edit";
        }

        try {
            concoursService.modifierConcours(id, concoursDTO);
            redirectAttributes.addFlashAttribute("success", "Concours modifié avec succès !");
            return "redirect:/admin/concours/list";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de la modification : " + e.getMessage());
            model.addAttribute("concours", concoursDTO);
            return "admin/gestion-concours/edit";
        }
    }

    // Méthode utilitaire pour convertir Concours en ConcoursDTO
    private ConcoursDTO convertToDTO(Concours concours) {
        ConcoursDTO dto = new ConcoursDTO();
        dto.setId(concours.getId());
        dto.setTitre(concours.getTitre());
        dto.setReference(concours.getReference());
        dto.setDateOuverture(concours.getDateOuverture());
        dto.setDateCloture(concours.getDateCloture());
        dto.setDateConcours(concours.getDateConcours());
        dto.setNbPostes(concours.getNbPostes());
        dto.setConditions(concours.getConditions());
        dto.setPublie(concours.isPublie());

        // Convertir les centres d'examen en IDs
        if (concours.getCentresExamen() != null && !concours.getCentresExamen().isEmpty()) {
            String centresIds = concours.getCentresExamen().stream()
                    .map(centre -> centre.getId().toString())
                    .collect(Collectors.joining(","));
            dto.setCentresExamenIds(centresIds);
        }

        return dto;
    }

}

