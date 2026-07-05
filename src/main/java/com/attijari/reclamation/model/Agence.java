package com.attijari.reclamation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@Table(name = "AGENCE")
public class Agence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_AGENCE", nullable = false, updatable = false)
    private Long idAgence;

    @Column(name = "CODE", length = 20, nullable = false, unique = true)
    private String code;

    @Column(name = "NOM", length = 100, nullable = false)
    private String nom;

    @Column(name = "ADRESSE", length = 255)
    private String adresse;

    @Column(name = "VILLE", length = 100)
    private String ville;

    @Column(name = "TELEPHONE", length = 20)
    private String telephone;

    @Column(name = "EMAIL", length = 100)
    private String email;

    @Column(name = "DIRECTEUR", length = 100)
    private String directeur;

    @Column(name = "DATE_CREATION", updatable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    public void initializeCreationDate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }

}
