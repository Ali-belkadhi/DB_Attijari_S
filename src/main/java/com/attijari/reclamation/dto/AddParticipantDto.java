package com.attijari.reclamation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddParticipantDto {

    @NotBlank(message = "L'identifiant de l'utilisateur est requis")
    private String userId;

    @NotBlank(message = "Le rôle dans la discussion est requis")
    @Size(max = 30, message = "Le rôle ne doit pas dépasser 30 caractères")
    private String roleInChat;
}
