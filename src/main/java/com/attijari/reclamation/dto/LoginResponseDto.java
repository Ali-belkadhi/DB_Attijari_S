package com.attijari.reclamation.dto;

import com.attijari.reclamation.model.Agence;
import com.attijari.reclamation.model.Equipe;
import com.attijari.reclamation.model.UserRole;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LoginResponseDto {

    private String message;
    private UserSummary user;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class UserSummary {
        private String id; // This will hold the Id_User custom ID
        private String nom;
        private String prenom;
        private String email;
        private UserRole role;
        private String telephone;
        private String cin;
        private Agence agence;

        // @JsonProperty("equipe")
        private Equipe equipe;

        private String image;

    }
}
