package com.concours.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HistoriqueCandidatureDTO {
    private Long id;
    private String action;
    private String utilisateur;
    private LocalDateTime dateAction;
    private String details;
}