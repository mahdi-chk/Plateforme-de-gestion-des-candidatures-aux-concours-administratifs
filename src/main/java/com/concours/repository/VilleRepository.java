package com.concours.repository;

import com.concours.entity.Ville;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VilleRepository extends JpaRepository<Ville, Long> {
    Optional<Ville> findByNom(String nom);
    List<Ville> findByNomContainingIgnoreCase(String nom);
}