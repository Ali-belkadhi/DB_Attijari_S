package com.attijari.reclamation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
public class CreateGroupeDroitDto {

    @NotBlank(message = "Le nom du groupe est requis")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String nom;

    @Size(max = 255, message = "La description ne doit pas dépasser 255 caractères")
    private String description;

    private Boolean actif = true;

    private Set<Long> permissionIds = new LinkedHashSet<>();
}
