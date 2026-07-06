package com.attijari.reclamation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LoginDto {

    @NotBlank(message = "L'adresse email est requise")
    @Email(message = "Format d'adresse email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est requis")
    @Size(min = 6, message = "Le mot de passe doit comporter au moins 6 caractères")
    private String password;

}
