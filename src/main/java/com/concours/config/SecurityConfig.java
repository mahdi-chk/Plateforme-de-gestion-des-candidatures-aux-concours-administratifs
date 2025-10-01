package com.concours.config;

import com.concours.security.JwtAuthenticationEntryPoint;
import com.concours.security.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/public/candidature",
                                "/public/suivi",
                                "/auth/**",
                                "/api/auth/**",
                                "/documents/public/**"  // Ajout pour les documents publics
                        )
                )
                .authorizeHttpRequests(authz -> authz
                        // Pages publiques - accessible sans authentification
                        .requestMatchers("/", "/public/**", "/auth/**", "/api/auth/**").permitAll()
                        // Documents publics (pour le suivi des candidatures)
                        .requestMatchers("/documents/public/**").permitAll()
                        // Endpoints spécifiques sans CSRF
                        .requestMatchers("/public/candidature", "/public/suivi").permitAll()
                        // Ressources statiques
                        .requestMatchers(
                                "/error",
                                "/uploads/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/static/**"
                        ).permitAll()
                        // Documents pour utilisateurs authentifiés
                        .requestMatchers("/documents/**").authenticated()
                        // Pages d'administration avec rôles spécifiques
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/gestionnaire-global/**").hasAnyRole("ADMIN", "GESTIONNAIRE_GLOBAL")
                        .requestMatchers("/gestionnaire-local/**").hasAnyRole("ADMIN", "GESTIONNAIRE_GLOBAL", "GESTIONNAIRE_LOCAL")
                        // Dashboard et autres pages nécessitent une authentification
                        .requestMatchers("/dashboard/**").authenticated()
                        // Candidatures admin
                        .requestMatchers("/admin/candidatures/**").hasAnyRole("ADMIN", "GESTIONNAIRE_GLOBAL", "GESTIONNAIRE_LOCAL")
                        // Toutes les autres requêtes nécessitent une authentification
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/auth/login?error")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/public/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                );

        // Ajout du filtre JWT pour l'authentification par token
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}