package com.attijari.reclamation.controller;

import com.attijari.reclamation.dto.RegisterDeviceTokenDto;
import com.attijari.reclamation.model.UserDevice;
import com.attijari.reclamation.service.UserDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * Endpoint REST pour l'enregistrement des tokens FCM.
 *
 * POST /devices/register-token
 *   Corps: { "userId": "...", "fcmToken": "...", "deviceType": "ANDROID" }
 *   Réponse: 200 OK avec l'entité UserDevice sauvegardée
 */
@RestController
@RequestMapping("/devices")
@Tag(name = "Devices", description = "Enregistrement des appareils pour les notifications push FCM")
@Slf4j
public class UserDeviceController {

    private final UserDeviceService userDeviceService;

    public UserDeviceController(UserDeviceService userDeviceService) {
        this.userDeviceService = userDeviceService;
    }

    @PostMapping("/register-token")
    @Operation(
        summary = "Enregistrer ou réactiver un token FCM",
        description = "Associe un token Firebase Cloud Messaging à un utilisateur. " +
                      "Si le token existe déjà, il est réactivé. Sinon, il est créé."
    )
    public ResponseEntity<UserDevice> registerToken(
            @Valid @RequestBody RegisterDeviceTokenDto dto,
            Principal principal) {

        // L'userId authentifié (via Principal) est prioritaire sur celui du DTO
        String currentUserId = (principal != null && principal.getName() != null)
                ? principal.getName()
                : dto.getUserId();

        log.info("[FCM-Token] POST /devices/register-token - userId={}, deviceType={}",
                currentUserId, dto.getDeviceType());

        UserDevice saved = userDeviceService.registerToken(dto, currentUserId);

        log.info("[FCM-Token] ✅ Token enregistré avec succès pour userId={}", currentUserId);
        return ResponseEntity.ok(saved);
    }
}
