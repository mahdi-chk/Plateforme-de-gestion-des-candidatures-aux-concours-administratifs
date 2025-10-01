package com.concours.repository;

import com.concours.entity.Specialite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpecialiteRepository extends JpaRepository<Specialite, Long> {
    Optional<Specialite> findByCode(String code);
    List<Specialite> findByLibelleContainingIgnoreCase(String libelle);
}