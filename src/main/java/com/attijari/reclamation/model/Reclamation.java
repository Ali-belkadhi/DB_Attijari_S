package com.attijari.reclamation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "RECLAMATIONS")
@Getter
@Setter
@NoArgsConstructor
public class Reclamation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID", length = 36, nullable = false, updatable = false)
    @JsonProperty("_id")
    private String idReclamation;

    @Column(name = "ID_RECLAMATION", length = 30, nullable = false, updatable = false)
    @JsonIgnore
    private String reference;

    @Column(name = "OBJET", length = 255, nullable = false)
    private String objet;

    @Column(name = "TITRE", length = 255, nullable = false)
    @JsonIgnore
    private String titre;

    @Lob
    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    @Column(name = "RECLAMATION_TYPE", length = 100, nullable = false)
    private String type;

    @Column(name = "PRIORITE", length = 50, nullable = false)
    @JsonIgnore
    private String priorite = "NORMALE";

    @Column(name = "STATUT", length = 50, nullable = false)
    private String statut = "NOUVELLE";

    @Column(name = "CLIENT_ACCESS_DISCUSSION", nullable = false)
    private Boolean clientAccessDiscussion = false;

    @Column(name = "DESTINATION_TYPE", length = 10, nullable = false)
    private String destinationType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "SENDER_ID", referencedColumnName = "ID_USER", nullable = false)
    @ToString.Exclude
    private User sender;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "RECLAMATION_RECEIVERS",
            joinColumns = @JoinColumn(name = "RECLAMATION_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "USER_ID", referencedColumnName = "ID_USER")
    )
    @ToString.Exclude
    private Set<User> receivers = new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "RECLAMATION_DESTINATIONS",
            joinColumns = @JoinColumn(name = "RECLAMATION_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "ID_EQUIPE", referencedColumnName = "ID_EQUIPE")
    )
    @ToString.Exclude
    private Set<Equipe> destinations = new LinkedHashSet<>();

    @ManyToOne
    @JoinColumn(name = "ID_AGENCE", referencedColumnName = "ID_AGENCE")
    @ToString.Exclude
    private Agence agence;

    @OneToMany(mappedBy = "reclamation", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    private List<Message> messages = new ArrayList<>();

    @OneToMany(mappedBy = "reclamation", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    private List<ReclamationParticipant> participants = new ArrayList<>();

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void initializeDefaults() {
        LocalDateTime now = LocalDateTime.now();
        if (idReclamation == null) {
            idReclamation = "REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (reference == null || reference.isBlank()) reference = idReclamation;
        if (titre == null || titre.isBlank()) titre = objet;
        if (priorite == null || priorite.isBlank()) priorite = "NORMALE";
        if (clientAccessDiscussion == null) clientAccessDiscussion = false;
        if (statut == null || statut.isBlank()) statut = "NOUVELLE";
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void updateModificationDate() {
        updatedAt = LocalDateTime.now();
    }
}
