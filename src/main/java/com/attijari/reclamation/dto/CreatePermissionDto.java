package com.attijari.reclamation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePermissionDto {

    @NotBlank(message = "Le code de la permission est requis")
    @Size(max = 100, message = "Le code ne doit pas dépasser 100 caractères")
    private String code;

    @NotBlank(message = "Le nom de la permission est requis")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String nom;

    @Size(max = 255, message = "La description ne doit pas dépasser 255 caractères")
    private String description;
}
