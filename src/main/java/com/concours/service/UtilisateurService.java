package com.concours.service;

import com.concours.dto.CentreExamenDTO;
import com.concours.dto.UtilisateurDTO;
import com.concours.entity.CentreExamen;
import com.concours.entity.Utilisateur;
import com.concours.entity.RoleUtilisateur;
import com.concours.exception.BusinessException;
import com.concours.mapper.CentreExamenMapper;
import com.concours.mapper.UtilisateurMapper;
import com.concours.repository.CentreExamenRepository;
import com.concours.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurMapper utilisateurMapper;
    private final PasswordEncoder passwordEncoder;
    private final CentreExamenRepository centreExamenRepository;
    private final CentreExamenMapper centreExamenMapper;

    public UtilisateurDTO creerUtilisateur(UtilisateurDTO utilisateurDTO) {
        // Vérification de l'unicité
        if (utilisateurRepository.existsByUsername(utilisateurDTO.getUsername())) {
            throw new BusinessException("Ce nom d'utilisateur existe déjà");
        }

        if (utilisateurRepository.existsByEmail(utilisateurDTO.getEmail())) {
            throw new BusinessException("Cet email existe déjà");
        }

        Utilisateur utilisateur = utilisateurMapper.toEntity(utilisateurDTO);
        utilisateur.setPassword(passwordEncoder.encode(utilisateurDTO.getPassword()));

        // Gérer les centres affectés pour les gestionnaires locaux
        if (utilisateurDTO.getRole() == RoleUtilisateur.ROLE_GESTIONNAIRE_LOCAL &&
                utilisateurDTO.getSelectedCentres() != null) {
            List<CentreExamen> centres = centreExamenRepository.findAllById(utilisateurDTO.getSelectedCentres());
            utilisateur.setCentresAffectes(centres);
        }

        utilisateur = utilisateurRepository.save(utilisateur);
        return utilisateurMapper.toDTO(utilisateur);
    }

    @Transactional(readOnly = true)
    public Page<UtilisateurDTO> getAllUtilisateurs(Pageable pageable) {
        return utilisateurRepository.findAll(pageable)
                .map(this::toDTOWithCentres);
    }

    @Transactional(readOnly = true)
    public List<UtilisateurDTO> getUtilisateursByRole(RoleUtilisateur role) {
        return utilisateurRepository.findByRole(role)
                .stream()
                .map(utilisateurMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public UtilisateurDTO getUtilisateurById(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));
        return toDTOWithCentres(utilisateur);
    }

    public UtilisateurDTO modifierUtilisateur(Long id, UtilisateurDTO utilisateurDTO) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));

        // Vérification de l'unicité (exclure l'utilisateur actuel)
        if (!utilisateur.getUsername().equals(utilisateurDTO.getUsername()) &&
                utilisateurRepository.existsByUsername(utilisateurDTO.getUsername())) {
            throw new BusinessException("Ce nom d'utilisateur existe déjà");
        }

        if (!utilisateur.getEmail().equals(utilisateurDTO.getEmail()) &&
                utilisateurRepository.existsByEmail(utilisateurDTO.getEmail())) {
            throw new BusinessException("Cet email existe déjà");
        }

        // Mise à jour des champs
        utilisateur.setUsername(utilisateurDTO.getUsername());
        utilisateur.setEmail(utilisateurDTO.getEmail());
        utilisateur.setRole(utilisateurDTO.getRole());
        utilisateur.setEnabled(utilisateurDTO.isEnabled());

        // Mise à jour du mot de passe seulement s'il est fourni et non vide
        if (utilisateurDTO.getPassword() != null && !utilisateurDTO.getPassword().trim().isEmpty()) {
            utilisateur.setPassword(passwordEncoder.encode(utilisateurDTO.getPassword()));
        }

        // Gérer les centres affectés pour les gestionnaires locaux
        if (utilisateurDTO.getRole() == RoleUtilisateur.ROLE_GESTIONNAIRE_LOCAL) {
            if (utilisateurDTO.getSelectedCentres() != null && !utilisateurDTO.getSelectedCentres().isEmpty()) {
                List<CentreExamen> centres = centreExamenRepository.findAllById(utilisateurDTO.getSelectedCentres());
                utilisateur.setCentresAffectes(centres);
            } else {
                // Vider les centres si aucun n'est sélectionné
                utilisateur.getCentresAffectes().clear();
            }
        } else {
            // Vider les centres si le rôle change
            utilisateur.getCentresAffectes().clear();
        }

        utilisateur = utilisateurRepository.save(utilisateur);
        return toDTOWithCentres(utilisateur);
    }

    @Transactional(readOnly = true)
    public Page<UtilisateurDTO> rechercherUtilisateurs(String username, String role, Pageable pageable) {
        Page<Utilisateur> utilisateurs;

        if ((username != null && !username.trim().isEmpty()) && (role != null && !role.trim().isEmpty())) {
            utilisateurs = utilisateurRepository.findByUsernameContainingIgnoreCaseAndRole(
                    username.trim(), RoleUtilisateur.valueOf(role), pageable);
        } else if (username != null && !username.trim().isEmpty()) {
            utilisateurs = utilisateurRepository.findByUsernameContainingIgnoreCase(username.trim(), pageable);
        } else if (role != null && !role.trim().isEmpty()) {
            utilisateurs = utilisateurRepository.findByRole(RoleUtilisateur.valueOf(role), pageable);
        } else {
            utilisateurs = utilisateurRepository.findAll(pageable);
        }

        return utilisateurs.map(this::toDTOWithCentres);
    }

    public void supprimerUtilisateur(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));
        
        // Vérifications avant suppression
        if (utilisateur.getRole() == RoleUtilisateur.ROLE_ADMIN) {
            long nbAdmins = utilisateurRepository.countByRole(RoleUtilisateur.ROLE_ADMIN);
            if (nbAdmins <= 1) {
                throw new BusinessException("Impossible de supprimer le dernier administrateur");
            }
        }
        
        utilisateurRepository.delete(utilisateur);
    }

    public void activerUtilisateur(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));

        utilisateur.setEnabled(true);
        utilisateurRepository.save(utilisateur);
    }

    public void desactiverUtilisateur(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));

        utilisateur.setEnabled(false);
        utilisateurRepository.save(utilisateur);
    }

    @Transactional(readOnly = true)
    public UtilisateurDTO getUtilisateurByUsername(String username) {
        Utilisateur utilisateur = utilisateurRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));
        return utilisateurMapper.toDTO(utilisateur);
    }

    public String getEncodedPassword(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));
        return utilisateur.getPassword();
    }


    // Méthode pour récupérer l'ID à partir du nom d'utilisateur
    @Transactional(readOnly = true)
    public Long getUserIdByUsername(String username) {
        return utilisateurRepository.findByUsername(username)
                .map(Utilisateur::getId)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));
    }

    private UtilisateurDTO toDTOWithCentres(Utilisateur utilisateur) {
        UtilisateurDTO dto = utilisateurMapper.toDTO(utilisateur);

        // Mapper les centres affectés
        if (utilisateur.getCentresAffectes() != null) {
            List<CentreExamenDTO> centresDTO = utilisateur.getCentresAffectes().stream()
                    .map(centreExamenMapper::toDTO)
                    .collect(Collectors.toList());
            dto.setCentresAffectes(centresDTO);
        }

        return dto;
    }

    /**
     * Récupère l'ID du centre principal d'un gestionnaire local
     */
    @Transactional(readOnly = true)
    public Long getCentrePrincipalId(Long utilisateurId) {
        try {
            UtilisateurDTO utilisateur = getUtilisateurById(utilisateurId);
            if (utilisateur.getCentresAffectes() != null && !utilisateur.getCentresAffectes().isEmpty()) {
                return utilisateur.getCentresAffectes().get(0).getId();
            }
            return null;
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du centre principal", e);
            return null;
        }
    }

    /**
     * Vérifie si un utilisateur a accès à un centre
     */
    @Transactional(readOnly = true)
    public boolean hasAccessToCentre(Long utilisateurId, Long centreId) {
        try {
            UtilisateurDTO utilisateur = getUtilisateurById(utilisateurId);
            if (utilisateur.getRole() == RoleUtilisateur.ROLE_ADMIN || utilisateur.getRole() == RoleUtilisateur.ROLE_GESTIONNAIRE_GLOBAL) {
                return true; // Admin et gestionnaires globaux ont accès à tous les centres
            }

            if (utilisateur.getCentresAffectes() != null) {
                return utilisateur.getCentresAffectes().stream()
                        .anyMatch(centre -> centre.getId().equals(centreId));
            }

            return false;
        } catch (Exception e) {
            log.error("Erreur lors de la vérification d'accès au centre", e);
            return false;
        }
    }
}