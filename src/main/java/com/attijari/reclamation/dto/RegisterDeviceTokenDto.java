package com.attijari.reclamation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Association d'un token Firebase avec un utilisateur")
public class RegisterDeviceTokenDto {

    @Size(max = 100)
    @Schema(example = "3")
    private String userId;

    @NotBlank
    @Size(max = 512)
    @Schema(description = "Token FCM retourné par Firebase Messaging")
    private String fcmToken;

    @NotBlank
    @Size(max = 30)
    @Schema(example = "ANDROID", allowableValues = {"ANDROID", "IOS", "WEB"})
    private String deviceType;
}
