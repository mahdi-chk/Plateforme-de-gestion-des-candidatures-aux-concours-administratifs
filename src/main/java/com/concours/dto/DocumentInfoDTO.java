package com.concours.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentInfoDTO {
    private Long id;
    private String nom;
    private String type;
    private String url;
    private String contentType;
    private long taille;
    private String tailleFormatee;
    private String dateUpload;

    public DocumentInfoDTO(Long id, com.concours.entity.TypeDocument typeDocument, String nom, String url, Long taille, java.time.LocalDateTime dateUpload) {
        this.id = id;
        this.type = typeDocument != null ? typeDocument.name() : null;
        this.nom = nom;
        this.url = url;
        this.taille = taille != null ? taille : 0L;
        this.dateUpload = dateUpload != null ? dateUpload.toString() : null;
    }
}