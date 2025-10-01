package com.concours.service;

import com.concours.entity.Utilisateur;
import com.concours.entity.RoleUtilisateur;
import com.concours.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public Utilisateur creerUtilisateur(String username, String password, RoleUtilisateur role, Long centreId) {
        if (utilisateurRepository.existsByUsername(username)) {
            throw new RuntimeException("Nom d'utilisateur déjà utilisé");
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setUsername(username);
        utilisateur.setPassword(passwordEncoder.encode(password));
        utilisateur.setRole(role);
        utilisateur.setEnabled(true);

        return utilisateurRepository.save(utilisateur);
    }

    public void modifierMotDePasse(Long userId, String nouveauMotDePasse) {
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        utilisateur.setPassword(passwordEncoder.encode(nouveauMotDePasse));
        utilisateurRepository.save(utilisateur);
    }
}