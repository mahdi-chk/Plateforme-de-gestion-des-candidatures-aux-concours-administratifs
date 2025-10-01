package com.concours.repository;

import com.concours.entity.Candidat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CandidatRepository extends JpaRepository<Candidat, Long> {
    Optional<Candidat> findByCin(String cin);
    Optional<Candidat> findByEmail(String email);
    boolean existsByCin(String cin);
    boolean existsByEmail(String email);
}