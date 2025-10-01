package com.concours.repository;

import com.concours.entity.CentreExamen;
import com.concours.entity.Ville;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CentreExamenRepository extends JpaRepository<CentreExamen, Long> {
    Optional<CentreExamen> findByCode(String code);
    List<CentreExamen> findByVille(Ville ville);
    Page<CentreExamen> findByVille(Ville ville, Pageable pageable);
    List<CentreExamen> findByActifTrue();
    List<CentreExamen> findByVilleAndActifTrue(Ville ville);

    @Query("SELECT c FROM CentreExamen c JOIN c.specialites s WHERE s.id = :specialiteId")
    Page<CentreExamen> findBySpecialites_Id(@Param("specialiteId") Long specialiteId, Pageable pageable);

    @Query("SELECT c FROM CentreExamen c JOIN c.specialites s WHERE c.ville = :ville AND s.id = :specialiteId")
    Page<CentreExamen> findByVilleAndSpecialites_Id(@Param("ville") Ville ville, @Param("specialiteId") Long specialiteId, Pageable pageable);

    long count();
}
