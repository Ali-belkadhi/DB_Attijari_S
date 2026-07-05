package com.attijari.reclamation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
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
@Table(name = "GROUPS_DROITS")
@Getter
@Setter
@NoArgsConstructor
public class GroupeDroit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "GROUP_ID", length = 36, nullable = false, updatable = false)
    private String groupId;

    @Column(name = "NOM", length = 100, nullable = false, unique = true)
    private String nom;

    @Column(name = "DESCRIPTION", length = 255)
    private String description;

    @Column(name = "ACTIF", nullable = false)
    private Boolean actif = true;

    @Column(name = "DATE_CREATION", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "GROUP_PERMISSIONS",
            joinColumns = @JoinColumn(name = "GROUP_ID", referencedColumnName = "GROUP_ID"),
            inverseJoinColumns = @JoinColumn(name = "ID_PERMISSION", referencedColumnName = "ID_PERMISSION")
    )
    @ToString.Exclude
    private Set<Permission> permissions = new LinkedHashSet<>();

    @PrePersist
    public void initializeDefaults() {
        if (actif == null) {
            actif = true;
        }
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }
}
