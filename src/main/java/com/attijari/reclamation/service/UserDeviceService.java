package com.attijari.reclamation.service;

import com.attijari.reclamation.dto.RegisterDeviceTokenDto;
import com.attijari.reclamation.model.User;
import com.attijari.reclamation.model.UserDevice;
import com.attijari.reclamation.repository.UserDeviceRepository;
import com.attijari.reclamation.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Service de gestion des tokens FCM des appareils utilisateurs.
 *
 * Logique d'upsert :
 *   - Si le token existe déjà → mise à jour (userId, deviceType, active=true, lastActive)
 *   - Si le token est nouveau → création d'un nouvel enregistrement
 *
 * Le token est stocké dans la table USER_DEVICES en Oracle.
 */
@Service
@Slf4j
public class UserDeviceService {

    private final UserDeviceRepository userDeviceRepository;
    private final UserRepository userRepository;

    public UserDeviceService(UserDeviceRepository userDeviceRepository, UserRepository userRepository) {
        this.userDeviceRepository = userDeviceRepository;
        this.userRepository = userRepository;
    }

    /**
     * Enregistre ou met à jour un token FCM pour un utilisateur.
     *
     * @param dto           DTO contenant fcmToken, deviceType, userId (optionnel)
     * @param currentUserId ID de l'utilisateur authentifié (extrait du Principal)
     * @return L'entité UserDevice sauvegardée
     */
    @Transactional
    public UserDevice registerToken(RegisterDeviceTokenDto dto, String currentUserId) {
        // ── Validation de l'userId ──
        if (currentUserId == null || currentUserId.isBlank()) {
            log.error("[FCM-Token] ❌ Tentative d'enregistrement de token sans userId");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Utilisateur non identifié");
        }

        String userId = currentUserId.trim();
        log.info("[FCM-Token] Enregistrement du token FCM pour userId={}", userId);
        log.info("[FCM-Token] DeviceType={}", dto.getDeviceType());
        log.info("[FCM-Token] Token (20 premiers chars)={}...",
                dto.getFcmToken() != null && dto.getFcmToken().length() >= 20
                        ? dto.getFcmToken().substring(0, 20)
                        : dto.getFcmToken());

        // ── Résolution de l'utilisateur en Oracle ──
        User user = userRepository.findByIdUser(userId)
                .orElseThrow(() -> {
                    log.error("[FCM-Token] ❌ Utilisateur introuvable en base: userId={}", userId);
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Utilisateur introuvable: " + userId);
                });
        log.info("[FCM-Token] Utilisateur trouvé: {} {}", user.getPrenom(), user.getNom());

        // ── Upsert du token FCM (update si existant, insert sinon) ──
        String token = dto.getFcmToken().trim();
        UserDevice device = userDeviceRepository.findByFcmToken(token)
                .orElseGet(() -> {
                    log.info("[FCM-Token] Nouveau token FCM → création d'un nouvel enregistrement");
                    return new UserDevice();
                });

        boolean isUpdate = device.getId() != null;
        if (isUpdate) {
            log.info("[FCM-Token] Token FCM existant → mise à jour de l'enregistrement (id={})", device.getId());
        }

        device.setUser(user);
        device.setFcmToken(token);
        device.setDeviceType(dto.getDeviceType().trim().toUpperCase(Locale.ROOT));
        device.setActive(true);
        device.setLastActive(LocalDateTime.now());

        UserDevice saved = userDeviceRepository.save(device);
        log.info("[FCM-Token] ✅ Token FCM {} avec succès (id={})",
                isUpdate ? "mis à jour" : "enregistré",
                saved.getId());

        return saved;
    }
}
