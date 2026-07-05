package com.attijari.reclamation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "APP_USERS")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "ID_USER", length = 100, nullable = false, unique = true)
    @JsonProperty("Id_User")
    private String idUser;

    @Column(name = "NOM", length = 100, nullable = false)
    private String nom;

    @Column(name = "PRENOM", length = 100, nullable = false)
    private String prenom;

    @Column(name = "EMAIL", length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "PASSWORD_HASH", length = 100, nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE", length = 30, nullable = false)
    private UserRole role = UserRole.USER;

    @Column(name = "TELEPHONE", length = 30)
    private String telephone;

    @Column(name = "CIN", length = 50)
    private String cin;

    @Column(name = "DATE_CREATED", nullable = false, updatable = false)
    private LocalDateTime dateCreated;

    @Column(name = "DEPARTEMENT_ID", length = 100)
    @JsonProperty("departement_id")
    private String departementId;

    @ManyToOne
    @JoinColumn(name = "ID_AGENCE", referencedColumnName = "ID_AGENCE")
    @JsonIgnore
    @ToString.Exclude
    private Agence agence;

    @ManyToOne
    @JoinColumn(name = "GROUP_ID", referencedColumnName = "GROUP_ID")
    @JsonIgnore
    @ToString.Exclude
    private GroupeDroit groupeDroit;

    @Column(name = "IMAGE", length = 1000)
    private String image;

    @PrePersist
    public void initializeDefaults() {
        if (dateCreated == null) dateCreated = LocalDateTime.now();
        if (role == null) role = UserRole.USER;
    }

    @JsonProperty("agence")
    public String getAgenceNom() {
        return agence == null ? null : agence.getNom();
    }

    @JsonProperty("agenceId")
    public Long getAgenceId() {
        return agence == null ? null : agence.getIdAgence();
    }

    @JsonProperty("groupId")
    public String getGroupId() {
        return groupeDroit == null ? null : groupeDroit.getGroupId();
    }
}
