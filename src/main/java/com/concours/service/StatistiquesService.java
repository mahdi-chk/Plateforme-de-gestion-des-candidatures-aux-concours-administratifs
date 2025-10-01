package com.concours.service;

import com.concours.dto.StatistiquesDTO;
import com.concours.entity.StatutCandidature;
import com.concours.repository.CandidatureRepository;
import com.concours.repository.CentreExamenRepository;
import com.concours.repository.ConcoursRepository;
import com.concours.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StatistiquesService {

    private final CandidatureRepository candidatureRepository;
    private final ConcoursRepository concoursRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CentreExamenRepository centreExamenRepository;

    /**
     * Statistiques optimisées pour la page d'accueil publique
     */
    public StatistiquesDTO getStatistiquesAccueil() {
        StatistiquesDTO stats = new StatistiquesDTO();

        try {
            // Utiliser des count optimisés
            Long nbCandidaturesValidees = candidatureRepository.countByStatut(StatutCandidature.VALIDEE);
            stats.setNbCandidatures(nbCandidaturesValidees != null ? nbCandidaturesValidees : 0L);
            stats.setTotalCandidatures(stats.getNbCandidatures()); // Synchronisation

            log.debug("Statistiques d'accueil calculées: {} candidatures validées", stats.getNbCandidatures());

        } catch (Exception e) {
            log.error("Erreur lors du calcul des statistiques d'accueil", e);
            stats.setNbCandidatures(0L);
            stats.setTotalCandidatures(0L);
        }

        return stats;
    }

    /**
     * Statistiques globales pour le dashboard admin (avec cache pour performance)
     */
    @Cacheable(value = "statistiques", key = "'globales'")
    public StatistiquesDTO getStatistiquesGlobales() {
        StatistiquesDTO stats = new StatistiquesDTO();

        try {
            log.info("Calcul des statistiques globales...");

            // Calculs statistiques de base
            long totalCandidatures = candidatureRepository.count();
            long candidaturesValidees = candidatureRepository.countByStatut(StatutCandidature.VALIDEE);
            long candidaturesEnAttente = candidatureRepository.countByStatut(StatutCandidature.EN_ATTENTE);
            long candidaturesRejetees = candidatureRepository.countByStatut(StatutCandidature.REJETEE);
            long nbConcours = concoursRepository.count();
            long nbUtilisateurs = utilisateurRepository.count();
            long nbCentres = centreExamenRepository.count();

            // Assignation des valeurs
            stats.setTotalCandidatures(totalCandidatures);
            stats.setNbCandidatures(totalCandidatures); // Synchronisation
            stats.setCandidaturesValidees(candidaturesValidees);
            stats.setCandidaturesEnAttente(candidaturesEnAttente);
            stats.setCandidaturesRejetees(candidaturesRejetees);
            stats.setNbConcours(nbConcours);
            stats.setNbUtilisateurs(nbUtilisateurs);
            stats.setNbCentres(nbCentres);

            // Calculer les statistiques détaillées avec gestion d'erreur
            stats.setCandidaturesParConcours(getCandidaturesParConcoursSecure());
            stats.setCandidaturesParSpecialite(getCandidaturesParSpecialiteSecure());
            stats.setCandidaturesParCentre(getCandidaturesParCentreSecure());
            stats.setCandidaturesParMois(getCandidaturesParMoisSecure());

            log.info("Statistiques globales calculées avec succès: {} candidatures, {} concours",
                    totalCandidatures, nbConcours);

        } catch (Exception e) {
            log.error("Erreur lors du calcul des statistiques globales", e);
            initDefaultStats(stats);
        }

        return stats;
    }

    /**
     * Statistiques rapides pour l'administration
     */
    public StatistiquesDTO getStatistiquesAdmin() {
        StatistiquesDTO stats = new StatistiquesDTO();

        try {
            long nbConcours = concoursRepository.count();
            long nbCandidatures = candidatureRepository.count();
            long nbUtilisateurs = utilisateurRepository.count();
            long nbCentres = centreExamenRepository.count();

            stats.setNbConcours(nbConcours);
            stats.setNbCandidatures(nbCandidatures);
            stats.setTotalCandidatures(nbCandidatures); // Synchronisation
            stats.setNbUtilisateurs(nbUtilisateurs);
            stats.setNbCentres(nbCentres);

            // Statistiques par statut
            stats.setCandidaturesValidees(candidatureRepository.countByStatut(StatutCandidature.VALIDEE));
            stats.setCandidaturesEnAttente(candidatureRepository.countByStatut(StatutCandidature.EN_ATTENTE));
            stats.setCandidaturesRejetees(candidatureRepository.countByStatut(StatutCandidature.REJETEE));

        } catch (Exception e) {
            log.error("Erreur lors du calcul des statistiques admin", e);
            initDefaultStats(stats);
        }

        return stats;
    }

    // Méthodes privées sécurisées pour calculer les statistiques détaillées

    private Map<String, Long> getCandidaturesParConcoursSecure() {
        try {
            List<Map<String, Object>> results = candidatureRepository.getCandidaturesParConcours();
            if (results == null || results.isEmpty()) {
                log.debug("Aucune donnée trouvée pour candidatures par concours");
                return generateDefaultConcoursData();
            }

            Map<String, Long> resultMap = new LinkedHashMap<>();
            for (Map<String, Object> result : results) {
                String concours = (String) result.get("concours");
                Object countObj = result.get("count");

                if (concours != null && countObj != null) {
                    Long count = convertToLong(countObj);
                    resultMap.put(concours, count);
                }
            }

            return resultMap.isEmpty() ? generateDefaultConcoursData() : resultMap;

        } catch (Exception e) {
            log.warn("Erreur lors du calcul des candidatures par concours", e);
            return generateDefaultConcoursData();
        }
    }

    private Map<String, Long> getCandidaturesParSpecialiteSecure() {
        try {
            List<Map<String, Object>> results = candidatureRepository.getCandidaturesParSpecialite();
            if (results == null || results.isEmpty()) {
                log.debug("Aucune donnée trouvée pour candidatures par spécialité");
                return generateDefaultSpecialiteData();
            }

            Map<String, Long> resultMap = new LinkedHashMap<>();
            for (Map<String, Object> result : results) {
                String specialite = (String) result.get("specialite");
                Object countObj = result.get("count");

                if (specialite != null && countObj != null) {
                    Long count = convertToLong(countObj);
                    resultMap.put(specialite, count);
                }
            }

            return resultMap.isEmpty() ? generateDefaultSpecialiteData() : resultMap;

        } catch (Exception e) {
            log.warn("Erreur lors du calcul des candidatures par spécialité", e);
            return generateDefaultSpecialiteData();
        }
    }

    private Map<String, Long> getCandidaturesParCentreSecure() {
        try {
            List<Map<String, Object>> results = candidatureRepository.getCandidaturesParCentre();
            if (results == null || results.isEmpty()) {
                log.debug("Aucune donnée trouvée pour candidatures par centre");
                return generateDefaultCentreData();
            }

            Map<String, Long> resultMap = new LinkedHashMap<>();
            for (Map<String, Object> result : results) {
                String centre = (String) result.get("centre");
                Object countObj = result.get("count");

                if (centre != null && countObj != null) {
                    Long count = convertToLong(countObj);
                    resultMap.put(centre, count);
                }
            }

            return resultMap.isEmpty() ? generateDefaultCentreData() : resultMap;

        } catch (Exception e) {
            log.warn("Erreur lors du calcul des candidatures par centre", e);
            return generateDefaultCentreData();
        }
    }

    private Map<String, Long> getCandidaturesParMoisSecure() {
        try {
            List<Map<String, Object>> results = candidatureRepository.getCandidaturesParMois();
            if (results == null || results.isEmpty()) {
                log.debug("Aucune donnée trouvée pour candidatures par mois");
                return generateDefaultMoisData();
            }

            Map<String, Long> resultMap = new LinkedHashMap<>();
            for (Map<String, Object> result : results) {
                String mois = (String) result.get("mois");
                Object countObj = result.get("count");

                if (mois != null && countObj != null) {
                    Long count = convertToLong(countObj);
                    resultMap.put(formatMoisLabel(mois), count);
                }
            }

            return resultMap.isEmpty() ? generateDefaultMoisData() : resultMap;

        } catch (Exception e) {
            log.warn("Erreur lors du calcul des candidatures par mois", e);
            return generateDefaultMoisData();
        }
    }

    // Méthodes utilitaires

    private Long convertToLong(Object obj) {
        if (obj instanceof Long) {
            return (Long) obj;
        } else if (obj instanceof Integer) {
            return ((Integer) obj).longValue();
        } else if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        return 0L;
    }

    private String formatMoisLabel(String moisStr) {
        try {
            String[] parts = moisStr.split("-");
            if (parts.length == 2) {
                int mois = Integer.parseInt(parts[0]);
                String annee = parts[1];
                String[] moisNoms = {"Jan", "Fév", "Mar", "Avr", "Mai", "Jun",
                        "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc"};
                if (mois >= 1 && mois <= 12) {
                    return moisNoms[mois - 1] + " " + annee;
                }
            }
        } catch (Exception e) {
            log.debug("Erreur lors du formatage du mois: {}", moisStr);
        }
        return moisStr;
    }

    private Map<String, Long> generateDefaultConcoursData() {
        Map<String, Long> defaultData = new LinkedHashMap<>();
        defaultData.put("Aucun concours disponible", 0L);
        return defaultData;
    }

    private Map<String, Long> generateDefaultSpecialiteData() {
        Map<String, Long> defaultData = new LinkedHashMap<>();
        defaultData.put("Aucune spécialité disponible", 0L);
        return defaultData;
    }

    private Map<String, Long> generateDefaultCentreData() {
        Map<String, Long> defaultData = new LinkedHashMap<>();
        defaultData.put("Aucun centre disponible", 0L);
        return defaultData;
    }

    private Map<String, Long> generateDefaultMoisData() {
        Map<String, Long> defaultData = new LinkedHashMap<>();
        defaultData.put("Aucune donnée mensuelle", 0L);
        return defaultData;
    }

    private void initDefaultStats(StatistiquesDTO stats) {
        stats.setNbConcours(0);
        stats.setNbCandidatures(0);
        stats.setTotalCandidatures(0);
        stats.setNbUtilisateurs(0);
        stats.setNbCentres(0L);
        stats.setCandidaturesValidees(0);
        stats.setCandidaturesEnAttente(0);
        stats.setCandidaturesRejetees(0);
        stats.setNbPostes(0);
        stats.setCandidaturesParConcours(generateDefaultConcoursData());
        stats.setCandidaturesParSpecialite(generateDefaultSpecialiteData());
        stats.setCandidaturesParCentre(generateDefaultCentreData());
        stats.setCandidaturesParMois(generateDefaultMoisData());
    }

    /**
     * Méthode pour invalider le cache des statistiques
     */
    @CacheEvict(value = "statistiques", allEntries = true)
    public void invaliderCacheStatistiques() {
        log.info("Cache des statistiques invalidé");
    }
}