package com.attijari.reclamation.dto;

import com.attijari.reclamation.model.UserRole;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginResponseDto {

    private String message;
    private UserSummary user;

    public LoginResponseDto() {
    }

    public LoginResponseDto(String message, UserSummary user) {
        this.message = message;
        this.user = user;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserSummary getUser() {
        return user;
    }

    public void setUser(UserSummary user) {
        this.user = user;
    }

    public static class UserSummary {
        private String id; // This will hold the Id_User custom ID
        private String nom;
        private String prenom;
        private String email;
        private UserRole role;
        private String telephone;
        private String cin;
        private String agence;
        
        @JsonProperty("departement_id")
        private String departementId;
        
        private String image;

        public UserSummary() {
        }

        public UserSummary(String id, String nom, String prenom, String email, UserRole role, String telephone, String cin, String agence, String departementId, String image) {
            this.id = id;
            this.nom = nom;
            this.prenom = prenom;
            this.email = email;
            this.role = role;
            this.telephone = telephone;
            this.cin = cin;
            this.agence = agence;
            this.departementId = departementId;
            this.image = image;
        }

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNom() {
            return nom;
        }

        public void setNom(String nom) {
            this.nom = nom;
        }

        public String getPrenom() {
            return prenom;
        }

        public void setPrenom(String prenom) {
            this.prenom = prenom;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public UserRole getRole() {
            return role;
        }

        public void setRole(UserRole role) {
            this.role = role;
        }

        public String getTelephone() {
            return telephone;
        }

        public void setTelephone(String telephone) {
            this.telephone = telephone;
        }

        public String getCin() {
            return cin;
        }

        public void setCin(String cin) {
            this.cin = cin;
        }

        public String getAgence() {
            return agence;
        }

        public void setAgence(String agence) {
            this.agence = agence;
        }

        public String getDepartementId() {
            return departementId;
        }

        public void setDepartementId(String departementId) {
            this.departementId = departementId;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }
    }
}
