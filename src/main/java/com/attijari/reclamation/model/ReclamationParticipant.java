package com.attijari.reclamation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "RECLAMATION_PARTICIPANTS",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_RECLAMATION_PARTICIPANT",
                columnNames = {"RECLAMATION_ID", "USER_ID"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class ReclamationParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PARTICIPANT", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "RECLAMATION_ID", referencedColumnName = "ID", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Reclamation reclamation;

    @ManyToOne(optional = false)
    @JoinColumn(name = "USER_ID", referencedColumnName = "ID_USER", nullable = false)
    @ToString.Exclude
    private User user;

    @Column(name = "ROLE_IN_CHAT", length = 30, nullable = false)
    private String roleInChat;

    @Column(name = "ADDED_AT", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    @PrePersist
    public void initializeAddedAt() {
        if (addedAt == null) addedAt = LocalDateTime.now();
    }
}
