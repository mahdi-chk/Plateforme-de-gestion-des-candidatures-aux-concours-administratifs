package com.concours.config;

import com.concours.entity.RoleUtilisateur;
import com.concours.entity.Utilisateur;
import com.concours.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeUsers();
    }

    private void initializeUsers() {
        // Admin
        if (!utilisateurRepository.existsByUsername("admin")) {
            Utilisateur admin = new Utilisateur();
            admin.setUsername("admin");
            admin.setEmail("admin@mef.gov.ma");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(RoleUtilisateur.ROLE_ADMIN);
            admin.setEnabled(true);
            utilisateurRepository.save(admin);
            log.info("Utilisateur admin créé - Email: admin@mef.gov.ma, Mot de passe: admin123");
        }

        // Gestionnaire Global
        if (!utilisateurRepository.existsByUsername("gestionnaire.global")) {
            Utilisateur gestionnaire = new Utilisateur();
            gestionnaire.setUsername("gestionnaire.global");
            gestionnaire.setEmail("gestionnaire.global@mef.gov.ma");
            gestionnaire.setPassword(passwordEncoder.encode("gest123"));
            gestionnaire.setRole(RoleUtilisateur.ROLE_GESTIONNAIRE_GLOBAL);
            gestionnaire.setEnabled(true);
            utilisateurRepository.save(gestionnaire);
            log.info("Gestionnaire global créé - Email: gestionnaire.global@mef.gov.ma, Mot de passe: gest123");
        }

        // Gestionnaire Local
        if (!utilisateurRepository.existsByUsername("gestionnaire.local")) {
            Utilisateur gestionnaireLocal = new Utilisateur();
            gestionnaireLocal.setUsername("gestionnaire.local");
            gestionnaireLocal.setEmail("gestionnaire.local@mef.gov.ma");
            gestionnaireLocal.setPassword(passwordEncoder.encode("local123"));
            gestionnaireLocal.setRole(RoleUtilisateur.ROLE_GESTIONNAIRE_LOCAL);
            gestionnaireLocal.setEnabled(true);
            utilisateurRepository.save(gestionnaireLocal);
            log.info("Gestionnaire local créé - Email: gestionnaire.local@mef.gov.ma, Mot de passe: local123");
        }

        // Candidat de test
//        if (!utilisateurRepository.existsByUsername("candidat.test")) {
//            Utilisateur candidat = new Utilisateur();
//            candidat.setUsername("candidat.test");
//            candidat.setEmail("candidat.test@gmail.com");
//            candidat.setPassword(passwordEncoder.encode("candidat123"));
//            candidat.setRole(RoleUtilisateur.ROLE_CANDIDAT);
//            candidat.setEnabled(true);
//            utilisateurRepository.save(candidat);
//            log.info("Candidat test créé - Email: candidat.test@gmail.com, Mot de passe: candidat123");
//        }
    }
}