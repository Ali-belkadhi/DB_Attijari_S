package com.attijari.reclamation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateMessageDto {

    @NotBlank(message = "L'identifiant de l'expéditeur est requis")
    private String senderId;

    @NotBlank(message = "Le contenu du message est requis")
    private String content;

    @Size(max = 30, message = "Le type de message ne doit pas dépasser 30 caractères")
    private String messageType = "TEXT";
}
