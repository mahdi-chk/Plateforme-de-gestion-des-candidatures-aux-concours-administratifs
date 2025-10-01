package com.concours.service;

import com.concours.dto.AuthRequest;
import com.concours.dto.AuthResponse;
import com.concours.entity.Utilisateur;
import com.concours.exception.BusinessException;
import com.concours.repository.UtilisateurRepository;
import com.concours.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UtilisateurRepository utilisateurRepository;

    public AuthResponse authentifier(AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
            String token = jwtUtil.generateToken(userDetails);

            Utilisateur utilisateur = utilisateurRepository.findByUsername(authRequest.getUsername())
                    .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));

            // Mise à jour de la dernière connexion
            utilisateur.setLastLogin(LocalDateTime.now());
            utilisateurRepository.save(utilisateur);

            return new AuthResponse(
                    token,
                    "Bearer",
                    utilisateur.getUsername(),
                    utilisateur.getRole().name()
            );

        } catch (Exception e) {
            throw new BusinessException("Identifiants incorrects");
        }
    }


}