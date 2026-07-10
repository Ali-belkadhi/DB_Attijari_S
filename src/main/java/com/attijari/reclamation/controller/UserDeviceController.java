package com.attijari.reclamation.controller;

import com.attijari.reclamation.dto.RegisterDeviceTokenDto;
import com.attijari.reclamation.model.UserDevice;
import com.attijari.reclamation.service.UserDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/devices")
@Tag(name = "Devices", description = "Enregistrement des appareils pour les notifications push")
public class UserDeviceController {

    private final UserDeviceService userDeviceService;

    public UserDeviceController(UserDeviceService userDeviceService) {
        this.userDeviceService = userDeviceService;
    }

    @PostMapping("/register-token")
    @Operation(summary = "Enregistrer ou réactiver un token FCM")
    public ResponseEntity<UserDevice> registerToken(@Valid @RequestBody RegisterDeviceTokenDto dto, Principal principal) {
        String currentUserId = principal != null ? principal.getName() : dto.getUserId();
        return ResponseEntity.ok(userDeviceService.registerToken(dto, currentUserId));
    }
}
