package com.concours.service;

import com.concours.dto.SpecialiteDTO;
import com.concours.entity.Specialite;
import com.concours.exception.BusinessException;
import com.concours.mapper.SpecialiteMapper;
import com.concours.repository.SpecialiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SpecialiteService {

    private final SpecialiteRepository specialiteRepository;
    private final SpecialiteMapper specialiteMapper;

    public SpecialiteDTO creerSpecialite(SpecialiteDTO specialiteDTO) {
        // Vérification de l'unicité du code
        if (specialiteRepository.findByCode(specialiteDTO.getCode()).isPresent()) {
            throw new BusinessException("Ce code de spécialité existe déjà");
        }

        Specialite specialite = specialiteMapper.toEntity(specialiteDTO);
        specialite = specialiteRepository.save(specialite);
        return specialiteMapper.toDTO(specialite);
    }

    @Transactional(readOnly = true)
    public Page<SpecialiteDTO> getAllSpecialites(Pageable pageable) {
        return specialiteRepository.findAll(pageable)
                .map(specialiteMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<SpecialiteDTO> getAllSpecialites() {
        return specialiteRepository.findAll()
                .stream()
                .map(specialiteMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public SpecialiteDTO getSpecialiteById(Long id) {
        Specialite specialite = specialiteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Spécialité non trouvée"));
        return specialiteMapper.toDTO(specialite);
    }

    public SpecialiteDTO modifierSpecialite(Long id, SpecialiteDTO specialiteDTO) {
        Specialite specialite = specialiteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Spécialité non trouvée"));

        // Vérification de l'unicité du code (exclure la spécialité actuelle)
        if (!specialite.getCode().equals(specialiteDTO.getCode()) &&
                specialiteRepository.findByCode(specialiteDTO.getCode()).isPresent()) {
            throw new BusinessException("Ce code de spécialité existe déjà");
        }

        specialite.setCode(specialiteDTO.getCode());
        specialite.setLibelle(specialiteDTO.getLibelle());
        specialite.setNbPostes(specialiteDTO.getNbPostes());

        specialite = specialiteRepository.save(specialite);
        return specialiteMapper.toDTO(specialite);
    }

    public void supprimerSpecialite(Long id) {
        Specialite specialite = specialiteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Spécialité non trouvée"));

        // Vérifier s'il y a des concours associés
        if (!specialite.getConcours().isEmpty()) {
            throw new BusinessException("Impossible de supprimer une spécialité associée à des concours");
        }

        specialiteRepository.delete(specialite);
    }

    @Transactional(readOnly = true)
    public List<SpecialiteDTO> rechercherSpecialites(String terme) {
        return specialiteRepository.findByLibelleContainingIgnoreCase(terme)
                .stream()
                .map(specialiteMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SpecialiteDTO> listerToutesLesSpecialites() {
        return getAllSpecialites();
    }
}
