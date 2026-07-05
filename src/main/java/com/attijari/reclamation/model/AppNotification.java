package com.attijari.reclamation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "APP_NOTIFICATIONS")
@Getter
@Setter
@NoArgsConstructor
public class AppNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_NOTIFICATION", length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "USER_ID", referencedColumnName = "ID_USER", nullable = false)
    @ToString.Exclude
    private User recipient;

    @ManyToOne(optional = false)
    @JoinColumn(name = "RECLAMATION_ID", referencedColumnName = "ID", nullable = false)
    @ToString.Exclude
    private Reclamation reclamation;

    @Column(name = "NOTIFICATION_TYPE", length = 50, nullable = false)
    private String type;

    @Column(name = "TITLE", length = 255, nullable = false)
    private String title;

    @Lob
    @Column(name = "BODY", nullable = false)
    private String body;

    @Column(name = "READ_FLAG", nullable = false)
    private Boolean read = false;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void initializeDefaults() {
        if (read == null) read = false;
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
