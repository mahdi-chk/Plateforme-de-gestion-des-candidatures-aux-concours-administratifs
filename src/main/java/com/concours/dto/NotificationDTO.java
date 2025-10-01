package com.concours.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationDTO {
    private Long id;
    private String titre;
    private String message;
    private LocalDateTime dateCreation;
    private boolean lue;
    private String type;
    private Long referenceId; // ID de la candidature ou autre référence
}