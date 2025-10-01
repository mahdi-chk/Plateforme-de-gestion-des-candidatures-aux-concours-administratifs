package com.concours.repository;

import com.concours.entity.Concours;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConcoursRepository extends JpaRepository<Concours, Long> {
    Optional<Concours> findByReference(String reference);
    List<Concours> findByPublieTrue();
    List<Concours> findByDateOuvertureBeforeAndDateClotureAfter(LocalDate date1, LocalDate date2);

    @Query("SELECT c FROM Concours c WHERE c.dateOuverture <= CURRENT_DATE AND c.dateCloture >= CURRENT_DATE AND c.publie = true")
    List<Concours> findConcoursOuverts();

    @Query("SELECT c FROM Concours c WHERE " +
            "(LOWER(c.titre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.reference) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND c.publie = true")
    Page<Concours> searchConcours(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT c FROM Concours c WHERE " +
            "(LOWER(c.titre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.reference) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND c.dateOuverture <= CURRENT_DATE AND c.dateCloture >= CURRENT_DATE AND c.publie = true")
    List<Concours> searchConcoursOuverts(@Param("searchTerm") String searchTerm);

    @Query("SELECT c FROM Concours c WHERE c.publie = true ORDER BY c.dateOuverture DESC")
    Page<Concours> findAllPublishedConcours(Pageable pageable);

    @Query("SELECT c FROM Concours c WHERE c.dateCloture < CURRENT_DATE AND c.publie = true")
    Page<Concours> findConcoursFermes(Pageable pageable);

    List<Concours> findBySpecialitesIdAndPublieTrue(Long specialiteId);

    @Query("SELECT COUNT(c) FROM Concours c WHERE c.publie = true")
    long countPublishedConcours();

    @Query("SELECT COUNT(c) FROM Concours c WHERE c.dateOuverture <= CURRENT_DATE AND c.dateCloture >= CURRENT_DATE AND c.publie = true")
    long countOpenConcours();

    // Nouvelle méthode pour trouver les concours par date
    List<Concours> findByDateConcoursBetween(LocalDate start, LocalDate end);
    List<Concours> findByTitreContainingOrReferenceContaining(String titre, String reference);
    List<Concours> findByTitreContainingOrReferenceContainingAndPublie(String titre, String reference, Boolean publie);
    List<Concours> findByPublie(Boolean publie);
    Page<Concours> findByPublie(boolean publie, Pageable pageable);


    /**
     * Trouve les concours actifs (entre date d'ouverture et de clôture)
     */
    @Query("""
    SELECT c FROM Concours c 
    WHERE c.dateOuverture <= :today 
    AND c.dateCloture >= :today 
    AND c.publie = true
    ORDER BY c.dateCloture ASC
    """)
    List<Concours> findByDateOuvertureBeforeAndDateClotureAfterAndPublieTrue(
            @Param("today") LocalDate dateOuverture,
            @Param("today") LocalDate dateCloture);
}