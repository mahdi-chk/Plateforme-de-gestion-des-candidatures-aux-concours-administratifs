package com.concours.repository;

import com.concours.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, String> {
    List<Candidature> findByCandidatAndConcours(Candidat candidat, Concours concours);
    List<Candidature> findByCentreExamen(CentreExamen centreExamen);
    List<Candidature> findByStatut(StatutCandidature statut);
    Page<Candidature> findByCentreExamen(CentreExamen centreExamen, Pageable pageable);

    @Query("SELECT c FROM Candidature c WHERE c.centreExamen IN :centres")
    List<Candidature> findByCentreExamenIn(@Param("centres") List<CentreExamen> centres);

    @Query("SELECT COUNT(c) FROM Candidature c WHERE c.concours = :concours")
    long countByConcours(@Param("concours") Concours concours);

    @Query("SELECT COUNT(c) FROM Candidature c WHERE c.concours = :concours AND c.statut = :statut")
    long countByConcoursAndStatut(@Param("concours") Concours concours, @Param("statut") StatutCandidature statut);

    long countByStatut(StatutCandidature statut);

    @Query("SELECT new map(c.concours.titre as concours, count(*) as count) " +
            "FROM Candidature c GROUP BY c.concours.titre")
    List<Map<String, Object>> getCandidaturesParConcours();

    @Query("SELECT c FROM Candidature c WHERE " +
            "(:concoursId IS NULL OR c.concours.id = :concoursId) AND " +
            "(:specialiteId IS NULL OR c.specialite.id = :specialiteId) AND " +
            "(:centreId IS NULL OR c.centreExamen.id = :centreId) AND " +
            "(:statut IS NULL OR c.statut = :statut) AND " +
            "(:diplome IS NULL OR LOWER(c.candidat.diplome) LIKE LOWER(CONCAT('%', :diplome, '%')))")
    Page<Candidature> findByFilters(@Param("concoursId") Long concoursId,
                                    @Param("specialiteId") Long specialiteId,
                                    @Param("centreId") Long centreId,
                                    @Param("statut") StatutCandidature statut,
                                    @Param("diplome") String diplome,
                                    Pageable pageable);

    @Query("SELECT new map(c.specialite.libelle as specialite, count(*) as count) " +
            "FROM Candidature c WHERE c.specialite IS NOT NULL GROUP BY c.specialite.libelle")
    List<Map<String, Object>> getCandidaturesParSpecialite();

    @Query("SELECT new map(c.centreExamen.code as centre, count(*) as count) " +
            "FROM Candidature c WHERE c.centreExamen IS NOT NULL GROUP BY c.centreExamen.code")
    List<Map<String, Object>> getCandidaturesParCentre();

    @Query("SELECT new map(FUNCTION('DATE_FORMAT', c.dateDepot, '%m-%Y') as mois, count(*) as count) " +
            "FROM Candidature c WHERE c.dateDepot IS NOT NULL GROUP BY FUNCTION('DATE_FORMAT', c.dateDepot, '%m-%Y')")
    List<Map<String, Object>> getCandidaturesParMois();

    /**
     * Trouve les candidatures par centre avec filtres
     */
    @Query("""
    SELECT c FROM Candidature c 
    WHERE c.centreExamen = :centre
    AND (:concoursId IS NULL OR c.concours.id = :concoursId)
    AND (:specialiteId IS NULL OR c.specialite.id = :specialiteId)
    AND (:statut IS NULL OR c.statut = :statut)
    AND (:diplome IS NULL OR LOWER(c.candidat.diplome) LIKE LOWER(CONCAT('%', :diplome, '%')))
    ORDER BY c.dateDepot DESC
    """)
    Page<Candidature> findByCentreWithFilters(@Param("centre") CentreExamen centre,
                                              @Param("concoursId") Long concoursId,
                                              @Param("specialiteId") Long specialiteId,
                                              @Param("statut") String statut,
                                              @Param("diplome") String diplome,
                                              Pageable pageable);

    /**
     * Compte les candidatures par centre
     */
    long countByCentreExamen(CentreExamen centreExamen);

    /**
     * Compte les candidatures par centre et statut
     */
    long countByCentreExamenAndStatut(CentreExamen centreExamen, StatutCandidature statut);

    /**
     * Trouve les candidatures par centre et statut avec pagination
     */
    Page<Candidature> findByCentreExamenAndStatut(CentreExamen centreExamen, StatutCandidature statut, Pageable pageable);

    /**
     * Compte les candidatures par centre, spécialité et statut
     */
    long countByCentreExamenAndSpecialiteAndStatut(CentreExamen centreExamen, Specialite specialite, StatutCandidature statut);

    /**
     * Trouve les candidatures par centre avec filtres (utilisant l'ID du centre)
     */
    @Query("""
    SELECT c FROM Candidature c 
    WHERE c.centreExamen.id = :centreId
    AND (:concoursId IS NULL OR c.concours.id = :concoursId)
    AND (:specialiteId IS NULL OR c.specialite.id = :specialiteId)
    AND (:statut IS NULL OR c.statut = :statut)
    AND (:diplome IS NULL OR LOWER(c.candidat.diplome) LIKE LOWER(CONCAT('%', :diplome, '%')))
    ORDER BY c.dateDepot DESC
    """)
    Page<Candidature> findByCentreWithFilters(@Param("centreId") Long centreId,
                                              @Param("concoursId") Long concoursId,
                                              @Param("specialiteId") Long specialiteId,
                                              @Param("statut") StatutCandidature statut,
                                              @Param("diplome") String diplome,
                                              Pageable pageable);

    /**
     * Compte les candidatures par centre ID
     */
    long countByCentreExamenId(Long centreExamenId);

    /**
     * Compte les candidatures par centre ID et statut
     */
    long countByCentreExamenIdAndStatut(Long centreExamenId, StatutCandidature statut);

    /**
     * Trouve les candidatures par centre ID et statut avec pagination
     */
    Page<Candidature> findByCentreExamenIdAndStatut(Long centreExamenId, StatutCandidature statut, Pageable pageable);

    /**
     * Compte les candidatures par centre ID, spécialité ID et statut
     */
    long countByCentreExamenIdAndSpecialiteIdAndStatut(Long centreExamenId, Long specialiteId, StatutCandidature statut);

    /**
     * Trouve toutes les candidatures d'un centre avec relations chargées
     */
    @Query("""
    SELECT c FROM Candidature c 
    LEFT JOIN FETCH c.candidat 
    LEFT JOIN FETCH c.concours 
    LEFT JOIN FETCH c.specialite 
    LEFT JOIN FETCH c.centreExamen ce
    LEFT JOIN FETCH ce.ville
    WHERE c.centreExamen.id = :centreId
    ORDER BY c.dateDepot DESC
    """)
    List<Candidature> findAllByCentreExamenIdWithFetch(@Param("centreId") Long centreId);

    /**
     * Statistiques par centre
     */
    @Query("""
    SELECT new map(
        c.statut as statut,
        COUNT(c) as count
    )
    FROM Candidature c 
    WHERE c.centreExamen.id = :centreId
    GROUP BY c.statut
    """)
    List<Map<String, Object>> getStatistiquesByCentre(@Param("centreId") Long centreId);


}