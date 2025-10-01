package com.concours.service;

import com.concours.dto.CentreExamenDTO;
import com.concours.entity.CentreExamen;
import com.concours.entity.Specialite;
import com.concours.entity.Ville;
import com.concours.exception.BusinessException;
import com.concours.mapper.CentreExamenMapper;
import com.concours.repository.CentreExamenRepository;
import com.concours.repository.VilleRepository;
import com.concours.repository.SpecialiteRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CentreExamenService {

    private final CentreExamenRepository centreExamenRepository;
    private final VilleRepository villeRepository;
    private final SpecialiteRepository specialiteRepository;
    private final CentreExamenMapper centreExamenMapper;

    public CentreExamenDTO creerCentreExamen(CentreExamenDTO centreExamenDTO) {
        // Vérification de l'unicité du code
        if (centreExamenRepository.findByCode(centreExamenDTO.getCode()).isPresent()) {
            throw new BusinessException("Ce code de centre existe déjà");
        }

        Ville ville = villeRepository.findById(centreExamenDTO.getVilleId())
                .orElseThrow(() -> new BusinessException("Ville non trouvée"));

        CentreExamen centreExamen = centreExamenMapper.toEntity(centreExamenDTO);
        centreExamen.setVille(ville);
        
        // Gérer les spécialités par IDs
        if (centreExamenDTO.getSpecialiteIds() != null && !centreExamenDTO.getSpecialiteIds().isEmpty()) {
            List<Specialite> specialites = specialiteRepository.findAllById(centreExamenDTO.getSpecialiteIds());
            centreExamen.setSpecialites(specialites);
        }

        centreExamen = centreExamenRepository.save(centreExamen);
        return centreExamenMapper.toDTO(centreExamen);
    }

    @Transactional(readOnly = true)
    public Page<CentreExamenDTO> getAllCentresExamen(Pageable pageable) {
        return centreExamenRepository.findAll(pageable)
                .map(centreExamenMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<CentreExamenDTO> getCentresActifs() {
        return centreExamenRepository.findByActifTrue()
                .stream()
                .map(centreExamenMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CentreExamenDTO> getCentresParVille(Long villeId) {
        Ville ville = villeRepository.findById(villeId)
                .orElseThrow(() -> new BusinessException("Ville non trouvée"));

        return centreExamenRepository.findByVilleAndActifTrue(ville)
                .stream()
                .map(centreExamenMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public CentreExamenDTO getCentreExamenById(Long id) {
        CentreExamen centreExamen = centreExamenRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Centre d'examen non trouvé"));
        return centreExamenMapper.toDTO(centreExamen);
    }

    public CentreExamenDTO modifierCentreExamen(Long id, CentreExamenDTO centreExamenDTO) {
        CentreExamen centreExamen = centreExamenRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Centre d'examen non trouvé"));

        // Vérification de l'unicité du code (exclure le centre actuel)
        if (!centreExamen.getCode().equals(centreExamenDTO.getCode()) &&
                centreExamenRepository.findByCode(centreExamenDTO.getCode()).isPresent()) {
            throw new BusinessException("Ce code de centre existe déjà");
        }

        Ville ville = villeRepository.findById(centreExamenDTO.getVilleId())
                .orElseThrow(() -> new BusinessException("Ville non trouvée"));

        centreExamen.setCode(centreExamenDTO.getCode());
        centreExamen.setCapacite(centreExamenDTO.getCapacite());
        centreExamen.setActif(centreExamenDTO.isActif());
        centreExamen.setVille(ville);

        centreExamen = centreExamenRepository.save(centreExamen);
        return centreExamenMapper.toDTO(centreExamen);
    }

    public void activerCentreExamen(Long id) {
        CentreExamen centreExamen = centreExamenRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Centre d'examen non trouvé"));

        centreExamen.setActif(true);
        centreExamenRepository.save(centreExamen);
    }

    public void desactiverCentreExamen(Long id) {
        CentreExamen centreExamen = centreExamenRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Centre d'examen non trouvé"));

        centreExamen.setActif(false);
        centreExamenRepository.save(centreExamen);
    }

    public void supprimerCentreExamen(Long id) {
        CentreExamen centreExamen = centreExamenRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Centre d'examen non trouvé"));

        // Vérifier s'il y a des candidatures associées
        if (!centreExamen.getCandidatures().isEmpty()) {
            throw new BusinessException("Impossible de supprimer un centre ayant des candidatures");
        }

        centreExamenRepository.delete(centreExamen);
    }

    @Transactional(readOnly = true)
    public Page<CentreExamenDTO> rechercherCentres(Long villeId, Long specialiteId, Pageable pageable) {
        if (villeId != null && specialiteId != null) {
            // Recherche par ville et spécialité
            Ville ville = villeRepository.findById(villeId)
                    .orElseThrow(() -> new BusinessException("Ville non trouvée"));
            return centreExamenRepository.findByVilleAndSpecialites_Id(ville, specialiteId, pageable)
                    .map(centreExamenMapper::toDTO);
        } else if (villeId != null) {
            // Recherche par ville uniquement
            Ville ville = villeRepository.findById(villeId)
                    .orElseThrow(() -> new BusinessException("Ville non trouvée"));
            return centreExamenRepository.findByVille(ville, pageable)
                    .map(centreExamenMapper::toDTO);
        } else if (specialiteId != null) {
            // Recherche par spécialité uniquement
            return centreExamenRepository.findBySpecialites_Id(specialiteId, pageable)
                    .map(centreExamenMapper::toDTO);
        } else {
            // Retourner tous les centres
            return centreExamenRepository.findAll(pageable)
                    .map(centreExamenMapper::toDTO);
        }
    }

    public CentreExamenDTO creerCentre(CentreExamenDTO centreExamenDTO) {
        return creerCentreExamen(centreExamenDTO);
    }

    public CentreExamenDTO obtenirCentreParId(Long id) {
        return getCentreExamenById(id);
    }

    public CentreExamenDTO modifierCentre(Long id, CentreExamenDTO centreExamenDTO) {
        return modifierCentreExamen(id, centreExamenDTO);
    }

    public void supprimerCentre(Long id) {
        supprimerCentreExamen(id);
    }

    public CentreExamen findById(Long id) {
        return centreExamenRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Centre d'examen non trouvé"));
    }

    public List<CentreExamenDTO> listerTousLesCentres() {
        return centreExamenRepository.findAll().stream()
                .map(centreExamenMapper::toDTO)
                .toList();
    }

    public List<CentreExamen> findAll() {
        return centreExamenRepository.findAll();
    }

    /**
     * Compte le nombre de spécialités disponibles dans un centre
     */
    @Transactional(readOnly = true)
    public int countSpecialitesByCentre(Long centreExamenId) {
        try {
            CentreExamen centre = centreExamenRepository.findById(centreExamenId)
                    .orElseThrow(() -> new BusinessException("Centre d'examen non trouvé"));

            return centre.getSpecialites() != null ? centre.getSpecialites().size() : 0;

        } catch (Exception e) {
            log.error("Erreur lors du comptage des spécialités par centre", e);
            return 0;
        }
    }
}
