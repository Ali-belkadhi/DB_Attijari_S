package com.attijari.reclamation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
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
@Table(name = "PERMISSIONS")
@Getter
@Setter
@NoArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PERMISSION", nullable = false, updatable = false)
    private Long idPermission;

    @Column(name = "CODE", length = 100, nullable = false, unique = true)
    private String code;

    @Column(name = "NOM", length = 100, nullable = false)
    private String nom;

    @Column(name = "DESCRIPTION", length = 255)
    private String description;

    @Column(name = "DATE_CREATION", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @ManyToMany(mappedBy = "permissions")
    @JsonIgnore
    @ToString.Exclude
    private Set<GroupeDroit> groupes = new LinkedHashSet<>();

    @PrePersist
    public void initializeCreationDate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }
}
