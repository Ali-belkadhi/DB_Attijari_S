package com.attijari.reclamation.service;

import com.attijari.reclamation.model.UserDevice;
import com.attijari.reclamation.repository.UserDeviceRepository;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service Firebase Cloud Messaging (FCM) pour l'envoi de notifications push.
 *
 * Flux d'initialisation :
 *   1. Charge le fichier firebase-service-account.json depuis le classpath (src/main/resources)
 *   2. Crée les GoogleCredentials à partir du service account
 *   3. Initialise FirebaseApp avec ces credentials
 *   4. Stocke l'instance FirebaseMessaging pour les envois ultérieurs
 *
 * Flux d'envoi :
 *   1. Récupère les tokens FCM actifs des utilisateurs cibles depuis la BDD Oracle
 *   2. Envoie des MulticastMessage par lots de 500 tokens (limite FCM)
 *   3. Journalise succès / échecs / tokens invalides
 */
@Service
@Slf4j
public class FirebaseNotificationService {

    // FCM permet au maximum 500 tokens par appel sendEachForMulticast
    private static final int FCM_BATCH_SIZE = 500;

    private final UserDeviceRepository userDeviceRepository;
    private FirebaseMessaging firebaseMessaging;

    public FirebaseNotificationService(UserDeviceRepository userDeviceRepository) {
        this.userDeviceRepository = userDeviceRepository;
    }

    @Value("${firebase.service-account-path:firebase-service-account.json}")
    private String serviceAccountPath;

    // ─────────────────────────────────────────────────────────────────
    // Initialisation Firebase (exécutée au démarrage du bean Spring)
    // ─────────────────────────────────────────────────────────────────
    @PostConstruct
    public void initializeFirebase() {
        log.info("[FCM] Initialisation de Firebase Admin SDK...");
        log.info("[FCM] Chargement du fichier de compte de service: {}", serviceAccountPath);

        try {
            FirebaseApp app;

            if (FirebaseApp.getApps().isEmpty()) {
                // ── Chargement des credentials depuis le classpath ──
                GoogleCredentials credentials;
                ClassPathResource resource = new ClassPathResource(serviceAccountPath);

                if (!resource.exists()) {
                    log.error("[FCM] ❌ CRITIQUE: Fichier '{}' introuvable dans le classpath (src/main/resources/).",
                            serviceAccountPath);
                    log.error("[FCM] ❌ Vérifiez que firebase-service-account.json est bien dans src/main/resources/");
                    firebaseMessaging = null;
                    return;
                }

                log.info("[FCM] Fichier de compte de service trouvé, chargement des credentials...");
                try (InputStream is = resource.getInputStream()) {
                    credentials = GoogleCredentials.fromStream(is);
                }
                log.info("[FCM] ✅ Credentials Google chargés avec succès");

                // ── Construction des options Firebase ──
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();

                // ── Initialisation de FirebaseApp ──
                app = FirebaseApp.initializeApp(options);
                log.info("[FCM] ✅ FirebaseApp initialisé: {}", app.getName());

            } else {
                app = FirebaseApp.getInstance();
                log.info("[FCM] ℹ️ FirebaseApp déjà initialisé, réutilisation de l'instance existante");
            }

            // ── Récupération de l'instance FirebaseMessaging ──
            firebaseMessaging = FirebaseMessaging.getInstance(app);
            log.info("[FCM] ✅ Firebase Admin SDK prêt pour l'envoi de notifications push");

        } catch (IOException e) {
            firebaseMessaging = null;
            log.error("[FCM] ❌ ERREUR IOException lors du chargement du fichier de compte de service: {}", e.getMessage());
            log.error("[FCM] ❌ Vérifiez que le fichier '{}' est valide et non corrompu.", serviceAccountPath);
        } catch (RuntimeException e) {
            firebaseMessaging = null;
            log.error("[FCM] ❌ ERREUR RuntimeException lors de l'initialisation Firebase: {}", e.getMessage());
            log.error("[FCM] ❌ Stack trace:", e);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Envoi de notifications à plusieurs utilisateurs
    // ─────────────────────────────────────────────────────────────────
    public void sendToUsers(Set<String> userIds, String title, String body, Map<String, String> data) {
        if (firebaseMessaging == null) {
            log.warn("[FCM] ⚠️ Firebase non initialisé. Notification non envoyée. Titre: {}", title);
            return;
        }

        if (userIds == null || userIds.isEmpty()) {
            log.warn("[FCM] ⚠️ Aucun userId fourni pour l'envoi de notification. Titre: {}", title);
            return;
        }

        log.info("[FCM] Recherche des tokens FCM actifs pour {} utilisateur(s)...", userIds.size());

        // Récupération des tokens FCM actifs depuis Oracle
        List<String> tokens = userDeviceRepository
                .findByUser_IdUserInAndActiveTrue(userIds)
                .stream()
                .map(UserDevice::getFcmToken)
                .distinct()
                .toList();

        log.info("[FCM] {} token(s) FCM trouvé(s) pour {} utilisateur(s)", tokens.size(), userIds.size());

        if (tokens.isEmpty()) {
            log.warn("[FCM] ⚠️ Aucun token FCM actif trouvé pour les utilisateurs: {}", userIds);
            return;
        }

        // Envoi par lots de 500 tokens (limite FCM)
        int totalBatches = (int) Math.ceil((double) tokens.size() / FCM_BATCH_SIZE);
        log.info("[FCM] Envoi en {} lot(s) de {} tokens max", totalBatches, FCM_BATCH_SIZE);

        for (int start = 0; start < tokens.size(); start += FCM_BATCH_SIZE) {
            int end = Math.min(start + FCM_BATCH_SIZE, tokens.size());
            int batchNumber = (start / FCM_BATCH_SIZE) + 1;
            log.info("[FCM] Envoi du lot {}/{} ({} tokens)...", batchNumber, totalBatches, end - start);
            sendBatch(new ArrayList<>(tokens.subList(start, end)), title, body, data);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Envoi d'un lot de notifications
    // ─────────────────────────────────────────────────────────────────
    private void sendBatch(List<String> tokens, String title, String body, Map<String, String> data) {
        if (tokens.isEmpty()) return;

        log.info("[FCM] Construction du message. Titre='{}', Corps='{}', Data={}", title, body, data);

        // Construction du message FCM (compatible Android + iOS)
        MulticastMessage message = MulticastMessage.builder()
                .setNotification(
                        Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build()
                )
                .putAllData(data != null ? data : Map.of())
                // Priorité HIGH pour que la notification arrive même si l'appareil est en veille
                .setAndroidConfig(
                        AndroidConfig.builder()
                                .setPriority(AndroidConfig.Priority.HIGH)
                                .build()
                )
                .addAllTokens(tokens)
                .build();

        try {
            log.info("[FCM] Envoi du message FCM à {} destinataire(s)...", tokens.size());
            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);

            log.info("[FCM] ✅ Réponse FCM: {} succès, {} échec(s) sur {} envoi(s)",
                    response.getSuccessCount(),
                    response.getFailureCount(),
                    tokens.size());

            // Journaliser les échecs individuels
            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    SendResponse sendResponse = responses.get(i);
                    if (!sendResponse.isSuccessful()) {
                        FirebaseMessagingException exception = sendResponse.getException();
                        String errorCode = exception != null
                                ? (exception.getMessagingErrorCode() != null
                                        ? exception.getMessagingErrorCode().name()
                                        : exception.getMessage())
                                : "INCONNU";

                        log.warn("[FCM] ❌ Échec pour le token [{}...]: code={}, message={}",
                                tokens.get(i).substring(0, Math.min(20, tokens.get(i).length())),
                                errorCode,
                                exception != null ? exception.getMessage() : "null");

                        // Si le token est invalide/expiré, le désactiver en BDD
                        if (exception != null && isTokenInvalid(exception)) {
                            log.info("[FCM] Token invalide détecté, désactivation en base...");
                            deactivateToken(tokens.get(i));
                        }
                    }
                }
            }

        } catch (FirebaseMessagingException e) {
            log.error("[FCM] ❌ Erreur FCM lors de l'envoi du lot: code={}, message={}",
                    e.getMessagingErrorCode(),
                    e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────

    /** Vérifie si l'erreur FCM indique un token invalide ou expiré */
    private boolean isTokenInvalid(FirebaseMessagingException e) {
        MessagingErrorCode code = e.getMessagingErrorCode();
        return code == MessagingErrorCode.REGISTRATION_TOKEN_NOT_REGISTERED
                || code == MessagingErrorCode.INVALID_ARGUMENT;
    }

    /** Désactive un token FCM invalide en base de données Oracle */
    private void deactivateToken(String token) {
        try {
            userDeviceRepository.findByFcmToken(token).ifPresent(device -> {
                device.setActive(false);
                userDeviceRepository.save(device);
                log.info("[FCM] Token désactivé en BDD: {}...", token.substring(0, 20));
            });
        } catch (Exception e) {
            log.warn("[FCM] Impossible de désactiver le token en BDD: {}", e.getMessage());
        }
    }
}
