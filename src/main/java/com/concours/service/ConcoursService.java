package com.concours.service;

import com.concours.dto.ConcoursDTO;
import com.concours.entity.CentreExamen;
import com.concours.entity.Concours;
import com.concours.exception.BusinessException;
import com.concours.mapper.ConcoursMapper;
import com.concours.repository.CentreExamenRepository;
import com.concours.repository.ConcoursRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ConcoursService {

    private final ConcoursRepository concoursRepository;
    private final ConcoursMapper concoursMapper;
    private final CentreExamenRepository centreExamenRepository;


    public ConcoursDTO creerConcours(ConcoursDTO concoursDTO) {
        // Validation des dates
        if (concoursDTO.getDateCloture().isBefore(concoursDTO.getDateOuverture())) {
            throw new BusinessException("La date de clôture doit être après la date d'ouverture");
        }

        if (concoursDTO.getDateConcours().isBefore(concoursDTO.getDateCloture())) {
            throw new BusinessException("La date du concours doit être après la date de clôture");
        }

        // Validation des centres
        if (concoursDTO.getCentresExamenIds() == null || concoursDTO.getCentresExamenIds().isEmpty()) {
            throw new BusinessException("Au moins un centre d'examen doit être sélectionné");
        }

        Concours concours = concoursMapper.toEntity(concoursDTO);

        // Gestion des centres d'examen
        List<Long> centreIds = Arrays.stream(concoursDTO.getCentresExamenIds().split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        List<CentreExamen> centres = centreExamenRepository.findAllById(centreIds);
        concours.getCentresExamen().addAll(centres);

        // Générer une référence unique
        concours.setReference(genererReferenceUnique());

        concours = concoursRepository.save(concours);
        return concoursMapper.toDTO(concours);
    }

    // Méthode améliorée pour générer une référence unique
    private String genererReferenceUnique() {
        String reference;
        int attempts = 0;
        do {
            if (attempts > 5) {
                throw new BusinessException("Impossible de générer une référence unique");
            }
            reference = "CONC-" + System.currentTimeMillis() + "-" +
                    ThreadLocalRandom.current().nextInt(1000, 9999);
            attempts++;
        } while (concoursRepository.findByReference(reference).isPresent());

        return reference;
    }


    @Transactional(readOnly = true)
    public List<ConcoursDTO> getConcoursOuverts() {
        return concoursRepository.findConcoursOuverts()
                .stream()
                .map(concoursMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ConcoursDTO> getAllConcours(Pageable pageable) {
        return concoursRepository.findAll(pageable)
                .map(concoursMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public ConcoursDTO getConcoursById(Long id) {
        Concours concours = concoursRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Concours non trouvé"));
        return concoursMapper.toDTO(concours);
    }

    public ConcoursDTO modifierConcours(Long id, ConcoursDTO concoursDTO) {
        Concours concours = concoursRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Concours non trouvé"));

        // SUPPRIMER LA RESTRICTION qui empêche la modification des concours ouverts
        // if (LocalDate.now().isAfter(concours.getDateOuverture())) {
        //     throw new BusinessException("Impossible de modifier un concours déjà ouvert");
        // }

        // Mettre à jour les propriétés de base
        concours.setTitre(concoursDTO.getTitre());
        concours.setDateOuverture(concoursDTO.getDateOuverture());
        concours.setDateCloture(concoursDTO.getDateCloture());
        concours.setDateConcours(concoursDTO.getDateConcours());
        concours.setNbPostes(concoursDTO.getNbPostes());
        concours.setConditions(concoursDTO.getConditions());
        concours.setPublie(concoursDTO.isPublie());

        // Mettre à jour les centres d'examen
        if (concoursDTO.getCentresExamenIds() != null && !concoursDTO.getCentresExamenIds().isEmpty()) {
            List<Long> centreIds = Arrays.stream(concoursDTO.getCentresExamenIds().split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

            List<CentreExamen> centres = centreExamenRepository.findAllById(centreIds);
            concours.getCentresExamen().clear();
            concours.getCentresExamen().addAll(centres);
        }

        concours = concoursRepository.save(concours);
        return concoursMapper.toDTO(concours);
    }

    // Ajouter cette méthode pour récupérer l'entité Concours
    public Concours getConcoursEntityById(Long id) {
        return concoursRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Concours non trouvé avec l'ID : " + id));
    }



    public void publierConcours(Long id) {
        Concours concours = concoursRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Concours non trouvé"));

        if (concours.isPublie()) {
            throw new BusinessException("Ce concours est déjà publié");
        }

        concours.setPublie(true);
        concoursRepository.save(concours);
    }

    public void supprimerConcours(Long id) {
        Concours concours = concoursRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Concours non trouvé"));

        // Vérifier s'il y a des candidatures associées
        if (concours.getCandidatures() != null && !concours.getCandidatures().isEmpty()) {
            throw new BusinessException("Impossible de supprimer un concours avec des candidatures existantes. " +
                    "Veuillez d'abord supprimer ou transférer les candidatures associées.");
        }

        // Supprimer les relations many-to-many d'abord
        concours.getCentresExamen().clear();
        concours.getSpecialites().clear();
        concours.getOrganisateurs().clear();

        // Sauvegarder pour supprimer les relations
        concoursRepository.save(concours);

        // Maintenant supprimer le concours
        concoursRepository.delete(concours);
    }

    @Transactional(readOnly = true)
    public Page<ConcoursDTO> searchConcours(String searchTerm, Pageable pageable) {
        return concoursRepository.searchConcours(searchTerm, pageable)
                .map(concoursMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<ConcoursDTO> searchConcoursOuverts(String searchTerm) {
        return concoursRepository.searchConcoursOuverts(searchTerm)
                .stream()
                .map(concoursMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ConcoursDTO> getAllConcoursPublies(Pageable pageable) {
        return concoursRepository.findAllPublishedConcours(pageable)
                .map(concoursMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ConcoursDTO> getConcoursFermes(Pageable pageable) {
        return concoursRepository.findConcoursFermes(pageable)
                .map(concoursMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<ConcoursDTO> getConcoursParSpecialite(Long specialiteId) {
        return concoursRepository.findBySpecialitesIdAndPublieTrue(specialiteId)
                .stream()
                .map(concoursMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ConcoursDTO> findByPublie(boolean publie, Pageable pageable) {
        return concoursRepository.findByPublie(publie, pageable)
                .map(concoursMapper::toDTO);
    }

    /**
     * Récupère les concours actifs (ouverts aux candidatures)
     */
    @Transactional(readOnly = true)
    public List<ConcoursDTO> getConcoursActifs() {
        try {
            LocalDate today = LocalDate.now();
            return concoursRepository.findByDateOuvertureBeforeAndDateClotureAfterAndPublieTrue(today, today)
                    .stream()
                    .map(concoursMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des concours actifs", e);
            return new ArrayList<>();
        }
    }
}