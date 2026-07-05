package com.attijari.reclamation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "RECLAMATION_MESSAGES")
@Getter
@Setter
@NoArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_MESSAGE", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "RECLAMATION_ID", referencedColumnName = "ID", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Reclamation reclamation;

    @ManyToOne(optional = false)
    @JoinColumn(name = "SENDER_ID", referencedColumnName = "ID_USER", nullable = false)
    @ToString.Exclude
    private User sender;

    @Lob
    @Column(name = "CONTENT", nullable = false)
    private String content;

    @Column(name = "MESSAGE_TYPE", length = 30, nullable = false)
    private String messageType = "TEXT";

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void initializeDefaults() {
        if (messageType == null || messageType.isBlank()) messageType = "TEXT";
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
