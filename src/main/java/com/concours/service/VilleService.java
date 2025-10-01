package com.concours.service;

import com.concours.entity.Ville;
import com.concours.exception.BusinessException;
import com.concours.repository.VilleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VilleService {

    private final VilleRepository villeRepository;

    public Ville creerVille(String nom) {
        if (villeRepository.findByNom(nom).isPresent()) {
            throw new BusinessException("Cette ville existe déjà");
        }

        Ville ville = new Ville();
        ville.setNom(nom);
        return villeRepository.save(ville);
    }

    @Transactional(readOnly = true)
    public List<Ville> getAllVilles() {
        return villeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Ville getVilleById(Long id) {
        return villeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Ville non trouvée"));
    }

    @Transactional(readOnly = true)
    public List<Ville> rechercherVilles(String terme) {
        return villeRepository.findByNomContainingIgnoreCase(terme);
    }

    public Ville modifierVille(Long id, String nouveauNom) {
        Ville ville = villeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Ville non trouvée"));

        if (!ville.getNom().equals(nouveauNom) &&
                villeRepository.findByNom(nouveauNom).isPresent()) {
            throw new BusinessException("Cette ville existe déjà");
        }

        ville.setNom(nouveauNom);
        return villeRepository.save(ville);
    }

    public void supprimerVille(Long id) {
        Ville ville = villeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Ville non trouvée"));

        // Vérifier s'il y a des centres d'examen ou des candidats associés
        if (!ville.getCentresExamens().isEmpty() ||
                !ville.getCandidatsNes().isEmpty() ||
                !ville.getCandidatsResidents().isEmpty()) {
            throw new BusinessException("Impossible de supprimer une ville ayant des références");
        }

        villeRepository.delete(ville);
    }

    @Transactional(readOnly = true)
    public List<Ville> listerToutesLesVilles() {
        return getAllVilles();
    }
}
