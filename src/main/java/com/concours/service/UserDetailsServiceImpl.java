package com.concours.service;

import com.concours.entity.Utilisateur;
import com.concours.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Tentative de connexion avec: {}", username);
        
        Utilisateur utilisateur = utilisateurRepository.findByUsername(username)
                .or(() -> utilisateurRepository.findByEmail(username))
                .orElseThrow(() -> {
                    log.error("Utilisateur non trouvé: {}", username);
                    return new UsernameNotFoundException("Utilisateur non trouvé: " + username);
                });
        
        log.info("Utilisateur trouvé: {} (enabled: {})", utilisateur.getUsername(), utilisateur.isEnabled());
        return User.builder()
                .username(utilisateur.getUsername())
                .password(utilisateur.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(utilisateur.getRole().name())))
                .accountExpired(false)
                .accountLocked(!utilisateur.isEnabled())
                .credentialsExpired(false)
                .disabled(!utilisateur.isEnabled())
                .build();
    }
}