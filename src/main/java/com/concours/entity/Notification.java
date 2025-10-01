package com.concours.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeNotification type;

    @Column(nullable = false)
    private String contenu;

    @Column(name = "date_envoi", nullable = false)
    private LocalDateTime dateEnvoi;

    private boolean success;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidature_id")
    private Candidature candidature;
}
