package com.attijari.reclamation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InviteParticipantsDto {

    @NotBlank(message = "L'identifiant de l'invitant est requis")
    private String inviterId;

    @NotBlank(message = "Le type de cible USER ou EQUIPE est requis")
    private String targetType;

    private String userId;

    private Long equipeId;
}
