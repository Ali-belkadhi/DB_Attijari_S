package com.attijari.reclamation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "EQUIPES")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Equipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_EQUIPE", nullable = false, updatable = false)
    private Long idEquipe;

    @Column(name = "CODE", length = 50, nullable = false, unique = true)
    private String code;

    @Column(name = "NOM", length = 100, nullable = false)
    private String nom;

    @Column(name = "DESCRIPTION", length = 255)
    private String description;

    @Column(name = "ACTIF", nullable = false)
    private Boolean actif = true;

    @OneToMany(mappedBy = "equipe", fetch = FetchType.EAGER)
    // @JsonIgnore
    @ToString.Exclude
    private Set<User> members = new LinkedHashSet<>();

    @Column(name = "DATE_CREATION", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    // @PrePersist
    // public void initializeDefaults() {
    // if (actif == null)
    // actif = true;
    // if (dateCreation == null)
    // dateCreation = LocalDateTime.now();
    // }
}
