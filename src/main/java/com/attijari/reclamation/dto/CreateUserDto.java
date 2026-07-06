package com.attijari.reclamation.dto;

import com.attijari.reclamation.model.UserRole;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserDto {

    @NotBlank(message = "L'identifiant de l'utilisateur est requis")
    @JsonProperty("Id_User")
    private String idUser;

    @NotBlank(message = "Le nom est requis")
    private String nom;

    @NotBlank(message = "Le prénom est requis")
    private String prenom;

    @NotBlank(message = "L'adresse email est requise")
    @Email(message = "Format d'adresse email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est requis")
    @Size(min = 6, message = "Le mot de passe doit comporter au moins 6 caractères")
    private String password;

    private UserRole role = UserRole.USER;

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
