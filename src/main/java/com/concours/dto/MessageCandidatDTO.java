package com.concours.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageCandidatDTO {
    private Long id;
    private String sujet;
    private String contenu;
    private LocalDateTime dateEnvoi;
    private Long expediteurId;
    private String expediteurNom;
    private Long candidatureId;
}