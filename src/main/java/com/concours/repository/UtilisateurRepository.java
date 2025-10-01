package com.concours.repository;

import com.concours.entity.Utilisateur;
import com.concours.entity.RoleUtilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByUsername(String username);
    Optional<Utilisateur> findByEmail(String email);
    List<Utilisateur> findByRole(RoleUtilisateur role);
    List<Utilisateur> findByEnabledTrue();
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    // Nouvelles méthodes pour la pagination et recherche
    Page<Utilisateur> findByRole(RoleUtilisateur role, Pageable pageable);
    Page<Utilisateur> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
    Page<Utilisateur> findByUsernameContainingIgnoreCaseAndRole(String username, RoleUtilisateur role, Pageable pageable);
    Page<Utilisateur> findByEmailContainingIgnoreCase(String email, Pageable pageable);
    
    // Compter par rôle
    long countByRole(RoleUtilisateur role);
    
    // Recherche avancée
    @Query("SELECT u FROM Utilisateur u WHERE " +
           "(:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))) AND " +
           "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:enabled IS NULL OR u.enabled = :enabled)")
    Page<Utilisateur> findByFilters(@Param("username") String username,
                                   @Param("email") String email,
                                   @Param("role") RoleUtilisateur role,
                                   @Param("enabled") Boolean enabled,
                                   Pageable pageable);
    
    // Statistiques
    @Query("SELECT COUNT(u) FROM Utilisateur u WHERE u.enabled = true")
    long countActiveUsers();
    
    @Query("SELECT COUNT(u) FROM Utilisateur u WHERE u.enabled = false")
    long countInactiveUsers();
}
