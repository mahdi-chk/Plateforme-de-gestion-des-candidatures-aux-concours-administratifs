package com.concours.controller;

import com.concours.dto.CentreExamenDTO;
import com.concours.dto.SpecialiteDTO;
import com.concours.service.CentreExamenService;
import com.concours.service.VilleService;
import com.concours.service.SpecialiteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;


import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/centres")
public class CentreController {

    private static final Logger log = LoggerFactory.getLogger(CentreController.class);

    @Autowired
    private CentreExamenService centreService;
    
    @Autowired
    private VilleService villeService;
    
    @Autowired
    private SpecialiteService specialiteService;

    @GetMapping("/list")
    public String listCentres(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) Long ville,
                             @RequestParam(required = false) Long specialite,
                             Model model) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CentreExamenDTO> centres = centreService.rechercherCentres(ville, specialite, pageable);
            
            model.addAttribute("centres", centres);
            model.addAttribute("villes", villeService.listerToutesLesVilles());
            model.addAttribute("specialites", specialiteService.listerToutesLesSpecialites());
            model.addAttribute("currentPage", page);
            model.addAttribute("villeId", ville);
            model.addAttribute("specialiteId", specialite);
            
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du chargement des centres : " + e.getMessage());
            model.addAttribute("centres", Page.empty());
            model.addAttribute("villes", List.of());
            model.addAttribute("specialites", List.of());
        }
        
        return "admin/gestion-centres/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("centre", new CentreExamenDTO());
        model.addAttribute("villes", villeService.listerToutesLesVilles());
        model.addAttribute("specialites", specialiteService.listerToutesLesSpecialites());
        return "admin/gestion-centres/add";
    }

    @PostMapping("/save")
    public String saveCentre(@Valid @ModelAttribute CentreExamenDTO centreDTO,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        try {
            // Vérifier les erreurs de validation
            if (bindingResult.hasErrors()) {
                model.addAttribute("centre", centreDTO);
                model.addAttribute("villes", villeService.listerToutesLesVilles());
                model.addAttribute("specialites", specialiteService.listerToutesLesSpecialites());
                return "admin/gestion-centres/add";
            }

            centreService.creerCentreExamen(centreDTO);
            redirectAttributes.addFlashAttribute("success", "Centre créé avec succès");
            return "redirect:/admin/centres/list";
            
        } catch (Exception e) {
            log.error("Erreur lors de la création du centre", e);
            model.addAttribute("error", "Erreur lors de la création : " + e.getMessage());
            model.addAttribute("centre", centreDTO);
            model.addAttribute("villes", villeService.listerToutesLesVilles());
            model.addAttribute("specialites", specialiteService.listerToutesLesSpecialites());
            return "admin/gestion-centres/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            CentreExamenDTO centre = centreService.obtenirCentreParId(id);
            
            // Préparer les IDs des spécialités pour le formulaire
            if (centre.getSpecialites() != null) {
                List<Long> specialiteIds = centre.getSpecialites().stream()
                        .map(SpecialiteDTO::getId)
                        .collect(Collectors.toList());
                centre.setSpecialiteIds(specialiteIds);
            }
            
            model.addAttribute("centre", centre);
            model.addAttribute("villes", villeService.listerToutesLesVilles());
            model.addAttribute("specialites", specialiteService.listerToutesLesSpecialites());
            return "admin/gestion-centres/edit";
        } catch (Exception e) {
            model.addAttribute("error", "Centre non trouvé");
            return "redirect:/admin/centres/list";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateCentre(@PathVariable Long id,
                              @ModelAttribute CentreExamenDTO centreDTO,
                              RedirectAttributes redirectAttributes) {
        try {
            centreService.modifierCentre(id, centreDTO);
            redirectAttributes.addFlashAttribute("success", "Centre modifié avec succès");
            return "redirect:/admin/centres/list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la modification : " + e.getMessage());
            return "redirect:/admin/centres/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteCentre(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            centreService.supprimerCentre(id);
            redirectAttributes.addFlashAttribute("success", "Centre supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression : " + e.getMessage());
        }
        return "redirect:/admin/centres/list";
    }
}