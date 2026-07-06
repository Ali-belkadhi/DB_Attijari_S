package com.attijari.reclamation.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
public class CreateReclamationDto {

    @NotBlank(message = "L'expéditeur est requis")
    @JsonAlias("clientId")
    private String senderId;

    @NotBlank(message = "L'objet de la réclamation est requis")
    @Size(max = 255, message = "L'objet ne doit pas dépasser 255 caractères")
    @JsonAlias("titre")
    private String objet;

    @NotBlank(message = "Le type de réclamation est requis")
    @Size(max = 100, message = "Le type ne doit pas dépasser 100 caractères")
    private String type;

    @Size(max = 50, message = "Le statut ne doit pas dépasser 50 caractères")
    private String statut;

    @Size(max = 50, message = "La priorité ne doit pas dépasser 50 caractères")
    private String priorite;

    @NotBlank(message = "La description est requise")
    private String description;

    @NotBlank(message = "Le type de destination USER ou EQUIPE est requis")
    private String destinationType;

    private Set<String> receiverIds = new LinkedHashSet<>();

    private Set<Long> destinationIds = new LinkedHashSet<>();

    private Long agenceId;
}
