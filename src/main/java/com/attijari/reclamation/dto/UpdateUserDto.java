package com.attijari.reclamation.dto;

import com.attijari.reclamation.model.UserRole;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserDto {

    private String nom;

    private String prenom;

    @Email(message = "Format d'adresse email invalide")
    private String email;

    @Size(min = 6, message = "Le mot de passe doit comporter au moins 6 caractères")
    private String password;

    private UserRole role;

    private String telephone;

    private String cin;

    @JsonProperty("idEquipe")
    @JsonAlias({"equipeId", "id_equipe", "ID_EQUIPE"})
    private Long idEquipe;

    private String agence;

    @JsonAlias({"id_agence", "ID_AGENCE"})
    private Long agenceId;

    @JsonAlias({"group_id", "groupID", "GROUP_ID"})
    private String groupId;

    private String image;
}
