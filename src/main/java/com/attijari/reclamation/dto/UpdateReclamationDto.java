package com.attijari.reclamation.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UpdateReclamationDto {

    @JsonAlias("clientId")
    private String senderId;

    @JsonAlias("titre")
    @Size(max = 255, message = "L'objet ne doit pas dépasser 255 caractères")
    private String objet;

    @Size(max = 100, message = "Le type ne doit pas dépasser 100 caractères")
    private String type;

    @Size(max = 50, message = "Le statut ne doit pas dépasser 50 caractères")
    private String statut;

    private String description;

    private Set<Long> destinationIds;

    private Long agenceId;
}
