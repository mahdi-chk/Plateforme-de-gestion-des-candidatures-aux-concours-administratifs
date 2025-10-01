package com.concours.mapper;

import com.concours.dto.DocumentDTO;
import com.concours.entity.Document;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentMapper {

    @Mapping(source = "nom", target = "nom")
    @Mapping(source = "contentType", target = "contentType")
    @Mapping(source = "taille", target = "taille")
    @Mapping(source = "dateUpload", target = "dateUpload")
    @Mapping(target = "tailleFormatee", expression = "java(formatTaille(document.getTaille()))")
    DocumentDTO toDTO(Document document);

    @Mapping(target = "contenu", ignore = true) // Le contenu sera géré séparément
    @Mapping(target = "candidature", ignore = true) // La relation sera gérée par le service
    @Mapping(target = "dateUpload", expression = "java(java.time.LocalDateTime.now())")
    Document toEntity(DocumentDTO documentDTO);

    /**
     * Mapping sans le contenu binaire pour les listes (performance)
     */
    @Mapping(source = "nom", target = "nom")
    @Mapping(source = "contentType", target = "contentType")
    @Mapping(source = "taille", target = "taille")
    @Mapping(source = "dateUpload", target = "dateUpload")
    @Mapping(target = "tailleFormatee", expression = "java(formatTaille(document.getTaille()))")
    DocumentDTO toDTOWithoutContent(Document document);

    /**
     * Mapping d'une liste de documents sans contenu
     */
    default java.util.List<DocumentDTO> toDTOListWithoutContent(java.util.List<Document> documents) {
        if (documents == null) {
            return null;
        }

        return documents.stream()
                .map(this::toDTOWithoutContent)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Méthode utilitaire pour formater la taille
     */
    default String formatTaille(Long bytes) {
        if (bytes == null || bytes == 0) {
            return "0 B";
        }

        double b = bytes.doubleValue();
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;

        while (b >= 1024 && unitIndex < units.length - 1) {
            b /= 1024;
            unitIndex++;
        }

        return String.format("%.1f %s", b, units[unitIndex]);
    }

    /**
     * Mapping pour créer un Document depuis un MultipartFile
     * (à utiliser dans les services)
     */
    @Named("createFromMultipartFile")
    default Document createDocumentFromFile(org.springframework.web.multipart.MultipartFile file,
                                            com.concours.entity.TypeDocument type,
                                            com.concours.entity.Candidature candidature) throws java.io.IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        Document document = new Document();
        document.setType(type);
        document.setNom(file.getOriginalFilename());
        document.setContentType(file.getContentType());
        document.setContenu(file.getBytes());
        document.setTaille((long) file.getBytes().length);
        document.setCandidature(candidature);
        document.setDateUpload(java.time.LocalDateTime.now());

        return document;
    }
}