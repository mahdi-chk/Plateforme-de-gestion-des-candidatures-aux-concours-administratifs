package com.concours.service;

import com.concours.dto.CandidatureDTO;
import com.concours.dto.CandidatureCreateDTO;
import com.concours.dto.PlacesSpecialiteDTO;
import com.concours.entity.*;
import com.concours.exception.BusinessException;
import com.concours.mapper.CandidatureMapper;
import com.concours.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CandidatureService {

    private final CandidatureRepository candidatureRepository;
    private final CandidatRepository candidatRepository;
    private final ConcoursRepository concoursRepository;
    private final SpecialiteRepository specialiteRepository;
    private final CentreExamenRepository centreExamenRepository;
    private final VilleRepository villeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CandidatureMapper candidatureMapper;
    private final DocumentService documentService;
    private final EmailService emailService;
    private final DocumentRepository documentRepository;

    public String soumettreCandiature(CandidatureCreateDTO candidatureDTO) {
        log.info("Début de soumission candidature pour CIN: {}", candidatureDTO.getCin());

        try {
            // Vérification des doublons
            verifierDoublons(candidatureDTO);

            // Création ou récupération du candidat
            Candidat candidat = creerOuRecupererCandidat(candidatureDTO);

            // Récupération des entités
            Concours concours = concoursRepository.findById(candidatureDTO.getConcoursId())
                    .orElseThrow(() -> new BusinessException("Concours non trouvé"));

            Specialite specialite = specialiteRepository.findById(candidatureDTO.getSpecialiteId())
                    .orElseThrow(() -> new BusinessException("Spécialité non trouvée"));

            CentreExamen centreExamen = centreExamenRepository.findById(candidatureDTO.getCentreExamenId())
                    .orElseThrow(() -> new BusinessException("Centre d'examen non trouvé"));

            // Vérification des conditions
            verifierConditionsCandidature(concours, candidat);

            // Génération du numéro de candidature
            String numeroCandidature = genererNumeroCandidature();

            // Création de la candidature
            Candidature candidature = new Candidature();
            candidature.setNumero(numeroCandidature);
            candidature.setCandidat(candidat);
            candidature.setConcours(concours);
            candidature.setSpecialite(specialite);
            candidature.setCentreExamen(centreExamen);
            candidature.setNotifications(candidatureDTO.getNotifications());
            candidature.setAccepter(candidatureDTO.isAccepter());
            candidature.setDateDepot(LocalDate.now());
            candidature.setStatut(StatutCandidature.EN_ATTENTE);

            // Sauvegarde de la candidature AVANT les documents
            candidature = candidatureRepository.save(candidature);
            log.info("Candidature créée avec numéro: {}", numeroCandidature);

            // Création des documents statiques (sans fichiers réels)
            creerDocumentsStatiques(candidature, candidat);

            // Envoi de notification
            try {
                emailService.envoyerConfirmationCandidature(candidat.getEmail(), candidature.getNumero());
                log.info("Email de confirmation envoyé à: {}", candidat.getEmail());
            } catch (Exception e) {
                log.warn("Erreur lors de l'envoi de l'email de confirmation", e);
                // On ne fait pas échouer la candidature pour un problème d'email
            }

            return candidature.getNumero();

        } catch (Exception e) {
            log.error("Erreur lors de la soumission de candidature pour CIN: {}", candidatureDTO.getCin(), e);
            throw e; // Re-lancer l'exception pour le rollback
        }
    }

    public Page<CandidatureDTO> getCandidaturesWithFilters(Long concoursId, Long specialiteId,
                                                           Long centreId, String statutStr,
                                                           String diplome, Pageable pageable) {

        // Conversion du statut string vers enum
        StatutCandidature statut = null;
        if (statutStr != null && !statutStr.isEmpty()) {
            try {
                statut = StatutCandidature.valueOf(statutStr);
            } catch (IllegalArgumentException e) {
                log.warn("Statut invalide: {}", statutStr);
                // Vous pouvez choisir de retourner une page vide ou de logger l'erreur
            }
        }

        Page<Candidature> candidatures = candidatureRepository.findByFilters(
                concoursId, specialiteId, centreId, statut, diplome, pageable);

        return candidatures.map(candidatureMapper::toDTO);
    }

    private String genererNumeroCandidature() {
        // Format: CAND-YYYYMMDD-XXXX (ex: CAND-20250827-A1B2)
        String datePart = LocalDate.now().toString().replace("-", "");
        String randomPart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return String.format("CAND-%s-%s", datePart, randomPart);
    }

    private void verifierDoublons(CandidatureCreateDTO candidatureDTO) {
        if (candidatRepository.existsByCin(candidatureDTO.getCin())) {
            Candidat candidatExistant = candidatRepository.findByCin(candidatureDTO.getCin()).get();
            Concours concours = concoursRepository.findById(candidatureDTO.getConcoursId()).get();

            List<Candidature> candidaturesExistantes = candidatureRepository
                    .findByCandidatAndConcours(candidatExistant, concours);

            if (!candidaturesExistantes.isEmpty()) {
                throw new BusinessException("Une candidature existe déjà pour ce candidat et ce concours");
            }
        }
    }

    private Candidat creerOuRecupererCandidat(CandidatureCreateDTO candidatureDTO) {
        return candidatRepository.findByCin(candidatureDTO.getCin())
                .orElseGet(() -> {
                    log.info("Création d'un nouveau candidat avec CIN: {}", candidatureDTO.getCin());

                    Candidat nouveauCandidat = candidatureMapper.toCandidat(candidatureDTO);

                    Ville lieuNaissance = villeRepository.findById(candidatureDTO.getLieuNaissanceId())
                            .orElseThrow(() -> new BusinessException("Lieu de naissance non trouvé"));

                    Ville villeResidence = villeRepository.findById(candidatureDTO.getVilleResidenceId())
                            .orElseThrow(() -> new BusinessException("Ville de résidence non trouvée"));

                    nouveauCandidat.setLieuNaissance(lieuNaissance);
                    nouveauCandidat.setVilleResidence(villeResidence);

                    return candidatRepository.save(nouveauCandidat);
                });
    }

    private void verifierConditionsCandidature(Concours concours, Candidat candidat) {
        LocalDate maintenant = LocalDate.now();

        if (maintenant.isBefore(concours.getDateOuverture()) ||
                maintenant.isAfter(concours.getDateCloture())) {
            throw new BusinessException("Les candidatures pour ce concours ne sont pas ouvertes");
        }

        if (!concours.isPublie()) {
            throw new BusinessException("Ce concours n'est pas encore publié");
        }
    }

    /**
     * Sauvegarde les documents directement en base de données
     */
    private void sauvegarderDocumentsEnBD(Candidature candidature,
                                          MultipartFile cvFile,
                                          MultipartFile cinFile,
                                          MultipartFile diplomeFile) {
        try {
            log.info("Sauvegarde des documents en base pour candidature: {}", candidature.getNumero());

            // Validation préalable des fichiers
            validateRequiredFiles(cvFile, cinFile, diplomeFile);

            // Upload des documents via le DocumentService
            documentService.uploadDocuments(candidature, cvFile, cinFile, diplomeFile);

            // Calcul de la taille totale des documents
            long tailleTotal = documentService.getTotalDocumentsSize(candidature);
            log.info("Documents sauvegardés avec succès. Taille totale: {} bytes ({})",
                    tailleTotal, formatTaille(tailleTotal));

        } catch (Exception e) {
            log.error("Erreur lors de la sauvegarde des documents en base", e);
            throw new BusinessException("Erreur lors de la sauvegarde des documents: " + e.getMessage());
        }
    }

    /**
     * Valide la présence des fichiers requis
     */
    private void validateRequiredFiles(MultipartFile cvFile, MultipartFile cinFile, MultipartFile diplomeFile) {
        if (cvFile == null || cvFile.isEmpty()) {
            throw new BusinessException("Le CV est obligatoire");
        }
        if (cinFile == null || cinFile.isEmpty()) {
            throw new BusinessException("La copie CIN est obligatoire");
        }
        if (diplomeFile == null || diplomeFile.isEmpty()) {
            throw new BusinessException("Le diplôme est obligatoire");
        }
    }

    /**
     * Formate une taille en octets en format lisible
     */
    private String formatTaille(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    @Transactional(readOnly = true)
    public CandidatureDTO getCandidatureByNumero(String numero) {
        Candidature candidature = candidatureRepository.findById(numero)
                .orElseThrow(() -> new BusinessException("Candidature non trouvée"));
        return candidatureMapper.toDTO(candidature);
    }

    @Transactional(readOnly = true)
    public Page<CandidatureDTO> getCandidaturesByCentre(Long centreId, Pageable pageable) {
        CentreExamen centre = centreExamenRepository.findById(centreId)
                .orElseThrow(() -> new BusinessException("Centre non trouvé"));

        return candidatureRepository.findByCentreExamen(centre, pageable)
                .map(candidatureMapper::toDTO);
    }

    public void validerCandidature(String numero, Long utilisateurId) {
        Candidature candidature = candidatureRepository.findById(numero)
                .orElseThrow(() -> new BusinessException("Candidature non trouvée"));

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));

        candidature.setStatut(StatutCandidature.VALIDEE);
        candidature.setUtilisateurTraitant(utilisateur);

        candidatureRepository.save(candidature);

        try {
            emailService.envoyerNotificationValidation(candidature.getCandidat().getEmail(), numero);
        } catch (Exception e) {
            log.warn("Erreur lors de l'envoi de l'email de validation", e);
        }
    }

    public void rejeterCandidature(String numero, Long utilisateurId, String motif) {
        Candidature candidature = candidatureRepository.findById(numero)
                .orElseThrow(() -> new BusinessException("Candidature non trouvée"));

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));

        candidature.setStatut(StatutCandidature.REJETEE);
        candidature.setUtilisateurTraitant(utilisateur);

        candidatureRepository.save(candidature);

        try {
            emailService.envoyerNotificationRejet(candidature.getCandidat().getEmail(), numero, motif);
        } catch (Exception e) {
            log.warn("Erreur lors de l'envoi de l'email de rejet", e);
        }
    }

    @Transactional(readOnly = true)
    public long countCandidaturesValides() {
        return candidatureRepository.countByStatut(StatutCandidature.VALIDEE);
    }

    /**
     * Récupère les candidatures d'un centre avec filtres
     */
    @Transactional(readOnly = true)
    public Page<CandidatureDTO> getCandidaturesByCentreWithFilters(Long centreId,
                                                                   Long concoursId,
                                                                   Long specialiteId,
                                                                   String statut,
                                                                   String diplome,
                                                                   Pageable pageable) {
        try {
            CentreExamen centre = centreExamenRepository.findById(centreId)
                    .orElseThrow(() -> new BusinessException("Centre non trouvé"));

            // Conversion du statut string vers enum
            StatutCandidature statutEnum = null;
            if (statut != null && !statut.isEmpty()) {
                try {
                    statutEnum = StatutCandidature.valueOf(statut);
                } catch (IllegalArgumentException e) {
                    log.warn("Statut invalide: {}", statut);
                }
            }

            return candidatureRepository.findByCentreWithFilters(
                            centre.getId(), concoursId, specialiteId, statutEnum, diplome, pageable)
                    .map(candidature -> mapToDTO(candidature));

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des candidatures avec filtres", e);
            throw new BusinessException("Erreur lors de la récupération des candidatures");
        }
    }

    /**
     * Récupère les candidatures d'un centre par statut
     */
    @Transactional(readOnly = true)
    public Page<CandidatureDTO> getCandidaturesByCentreAndStatut(Long centreId,
                                                                 StatutCandidature statut,
                                                                 Pageable pageable) {
        try {
            return candidatureRepository.findByCentreExamenIdAndStatut(centreId, statut, pageable)
                    .map(candidature -> mapToDTO(candidature));

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des candidatures par statut", e);
            throw new BusinessException("Erreur lors de la récupération des candidatures");
        }
    }

    /**
     * Compte le nombre de candidatures par centre
     */
    @Transactional(readOnly = true)
    public long countCandidaturesByCentre(Long centreId) {
        try {
            return candidatureRepository.countByCentreExamenId(centreId);
        } catch (Exception e) {
            log.error("Erreur lors du comptage des candidatures par centre", e);
            return 0;
        }
    }

    /**
     * Compte le nombre de candidatures validées par centre
     */
    @Transactional(readOnly = true)
    public long countCandidaturesValideesByCentre(Long centreId) {
        try {
            return candidatureRepository.countByCentreExamenIdAndStatut(centreId, StatutCandidature.VALIDEE);
        } catch (Exception e) {
            log.error("Erreur lors du comptage des candidatures validées par centre", e);
            return 0;
        }
    }



    @Transactional(readOnly = true)
    public long countCandidaturesRejeteesByCentre(Long centreId) {
        try {
            return candidatureRepository.countByCentreExamenIdAndStatut(centreId, StatutCandidature.REJETEE);
        } catch (Exception e) {
            log.error("Erreur lors du comptage des candidatures validées par centre", e);
            return 0;
        }
    }

    /**
     * Compte le nombre de candidatures en attente par centre
     */
    @Transactional(readOnly = true)
    public long countCandidaturesEnAttenteByCentre(Long centreId) {
        try {
            return candidatureRepository.countByCentreExamenIdAndStatut(centreId, StatutCandidature.EN_ATTENTE);
        } catch (Exception e) {
            log.error("Erreur lors du comptage des candidatures en attente par centre", e);
            return 0;
        }
    }

    /**
     * Récupère les places disponibles par spécialité pour un centre
     */
    @Transactional(readOnly = true)
    public List<PlacesSpecialiteDTO> getPlacesDisponiblesParSpecialite(Long centreId) {
        try {
            CentreExamen centre = centreExamenRepository.findById(centreId)
                    .orElseThrow(() -> new BusinessException("Centre non trouvé"));

            List<PlacesSpecialiteDTO> result = new ArrayList<>();

            // Récupérer toutes les spécialités disponibles dans le centre
            for (Specialite specialite : centre.getSpecialites()) {
                PlacesSpecialiteDTO places = new PlacesSpecialiteDTO();
                places.setSpecialiteId(specialite.getId());
                places.setSpecialiteLibelle(specialite.getLibelle());
                places.setSpecialiteCode(specialite.getCode());
                places.setNbPostesTotaux(specialite.getNbPostes());

                // Compter les candidatures validées pour cette spécialité dans ce centre
                long candidaturesValidees = candidatureRepository
                        .countByCentreExamenIdAndSpecialiteIdAndStatut(centreId, specialite.getId(), StatutCandidature.VALIDEE);
                places.setNbCandidaturesValidees(candidaturesValidees);

                // Compter les candidatures en attente pour cette spécialité dans ce centre
                long candidaturesEnAttente = candidatureRepository
                        .countByCentreExamenIdAndSpecialiteIdAndStatut(centreId, specialite.getId(), StatutCandidature.EN_ATTENTE);
                places.setNbCandidaturesEnAttente(candidaturesEnAttente);

                result.add(places);
            }

            return result;

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des places par spécialité", e);
            throw new BusinessException("Erreur lors de la récupération des places par spécialité");
        }
    }

    /**
     * Mapping manuel vers DTO (compatible avec votre structure)
     */
    private CandidatureDTO mapToDTO(Candidature candidature) {
        CandidatureDTO dto = new CandidatureDTO();

        // Informations de base
        dto.setNumero(candidature.getNumero());
        dto.setStatut(candidature.getStatut());
        dto.setDateDepot(candidature.getDateDepot());
        dto.setNotifications(candidature.getNotifications());
        dto.setAccepter(candidature.isAccepter());

        // Informations candidat (dénormalisées)
        if (candidature.getCandidat() != null) {
            dto.setCandidatNom(candidature.getCandidat().getNom());
            dto.setCandidatPrenom(candidature.getCandidat().getPrenom());
            dto.setCandidatCin(candidature.getCandidat().getCin());
            dto.setCandidatEmail(candidature.getCandidat().getEmail());
            dto.setCandidatTelephone(candidature.getCandidat().getTelephone());
            dto.setCandidatDiplome(candidature.getCandidat().getDiplome());
        }

        // Informations concours
        if (candidature.getConcours() != null) {
            dto.setConcoursTitre(candidature.getConcours().getTitre());
            dto.setConcoursReference(candidature.getConcours().getReference());
            dto.setConcoursId(candidature.getConcours().getId());
        }

        // Informations spécialité
        if (candidature.getSpecialite() != null) {
            dto.setSpecialiteLibelle(candidature.getSpecialite().getLibelle());
            dto.setSpecialiteCode(candidature.getSpecialite().getCode());
            dto.setSpecialiteId(candidature.getSpecialite().getId());
        }

        // Informations centre
        if (candidature.getCentreExamen() != null) {
            dto.setCentreCode(candidature.getCentreExamen().getCode());
            dto.setCentreVille(candidature.getCentreExamen().getVille().getNom());
            dto.setCentreId(candidature.getCentreExamen().getId());
        }

        // Utilisateur traitant
        if (candidature.getUtilisateurTraitant() != null) {
            dto.setUtilisateurTraitant(candidature.getUtilisateurTraitant().getUsername());
            dto.setUtilisateurTraitantId(candidature.getUtilisateurTraitant().getId());
        }

        return dto;
    }
    public Page<CandidatureDTO> getAllCandidatures(Pageable pageable) {
        return candidatureRepository.findAll(pageable)
                .map(candidatureMapper::toDTO);
    }
    /**
     * Supprime une candidature et ses documents associés
     */
    @Transactional
    public void supprimerCandidature(String numero) throws BusinessException {
        try {
            Candidature candidature = candidatureRepository.findById(numero)
                    .orElseThrow(() -> new BusinessException("Candidature non trouvée avec le numéro: " + numero));

            // Supprimer d'abord les documents associés
            documentRepository.deleteAll(candidature.getDocuments());

            // Puis supprimer la candidature
            candidatureRepository.delete(candidature);

            log.info("Candidature {} supprimée avec succès", numero);
        } catch (DataAccessException e) {
            log.error("Erreur lors de la suppression de la candidature {}", numero, e);
            throw new BusinessException("Erreur technique lors de la suppression de la candidature");
        }
    }

    /**
     * Crée des documents statiques en base sans contenu binaire réel
     */
    private void creerDocumentsStatiques(Candidature candidature, Candidat candidat) {
        try {
            log.info("Création des documents statiques pour candidature: {}", candidature.getNumero());

            // Contenu PDF statique minimal (header PDF)
            byte[] contenuPdfStatique = hexStringToByteArray("255044462D312E340A");

            // Document CV
            Document cv = new Document();
            cv.setType(TypeDocument.CV);
            cv.setNom(String.format("CV_%s_%s.pdf", candidat.getNom(), candidat.getPrenom()).replace(" ", "_"));
            cv.setContentType("application/pdf");
            cv.setContenu(contenuPdfStatique);
            cv.setTaille((long) contenuPdfStatique.length);
            cv.setCandidature(candidature);
            cv.setDateUpload(java.time.LocalDateTime.now());
            documentRepository.save(cv);

            // Document CIN
            Document cin = new Document();
            cin.setType(TypeDocument.CIN);
            cin.setNom(String.format("CIN_%s.pdf", candidat.getCin()));
            cin.setContentType("application/pdf");
            cin.setContenu(contenuPdfStatique);
            cin.setTaille((long) contenuPdfStatique.length);
            cin.setCandidature(candidature);
            cin.setDateUpload(java.time.LocalDateTime.now());
            documentRepository.save(cin);

            // Document Diplôme
            Document diplome = new Document();
            diplome.setType(TypeDocument.DIPLOME);
            diplome.setNom(String.format("Diplome_%s.pdf", candidat.getDiplome().replace(" ", "_")));
            diplome.setContentType("application/pdf");
            diplome.setContenu(contenuPdfStatique);
            diplome.setTaille((long) contenuPdfStatique.length);
            diplome.setCandidature(candidature);
            diplome.setDateUpload(java.time.LocalDateTime.now());
            documentRepository.save(diplome);

            log.info("Documents statiques créés avec succès pour candidature: {}", candidature.getNumero());

        } catch (Exception e) {
            log.error("Erreur lors de la création des documents statiques", e);
            throw new BusinessException("Erreur lors de la création des documents: " + e.getMessage());
        }
    }

    /**
     * Convertit une chaîne hexadécimale en tableau de bytes
     */
    private byte[] hexStringToByteArray(String hex) {
        int length = hex.length();
        byte[] data = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}