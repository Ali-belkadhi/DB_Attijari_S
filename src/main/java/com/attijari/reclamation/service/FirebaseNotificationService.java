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
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class FirebaseNotificationService {

    private static final int FCM_BATCH_SIZE = 500;

    private final UserDeviceRepository userDeviceRepository;
    private FirebaseMessaging firebaseMessaging;

    public FirebaseNotificationService(UserDeviceRepository userDeviceRepository) {
        this.userDeviceRepository = userDeviceRepository;
    }

    @PostConstruct
    public void initializeFirebase() {
        try {
            FirebaseApp app;
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.getApplicationDefault())
                        .build();
                app = FirebaseApp.initializeApp(options);
            } else {
                app = FirebaseApp.getInstance();
            }
            firebaseMessaging = FirebaseMessaging.getInstance(app);
            log.info("Firebase Admin initialisé pour les notifications push");
        } catch (IOException | RuntimeException exception) {
            firebaseMessaging = null;
            log.warn("Firebase Admin non configuré : les notifications restent enregistrées en base, sans push. "
                    + "Définissez GOOGLE_APPLICATION_CREDENTIALS.");
        }
    }

    public void sendToUsers(Set<String> userIds, String title, String body, Map<String, String> data) {
        if (firebaseMessaging == null || userIds.isEmpty()) return;

        List<String> tokens = userDeviceRepository.findByUser_IdUserInAndActiveTrue(userIds).stream()
                .map(UserDevice::getFcmToken)
                .distinct()
                .toList();

        for (int start = 0; start < tokens.size(); start += FCM_BATCH_SIZE) {
            int end = Math.min(start + FCM_BATCH_SIZE, tokens.size());
            sendBatch(new ArrayList<>(tokens.subList(start, end)), title, body, data);
        }
    }

    private void sendBatch(List<String> tokens, String title, String body, Map<String, String> data) {
        if (tokens.isEmpty()) return;

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .putAllData(data)
                .setAndroidConfig(AndroidConfig.builder().setPriority(AndroidConfig.Priority.HIGH).build())
                .addAllTokens(tokens)
                .build();

        try {
            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
            log.info("Push FCM envoyé : {} succès, {} échecs",
                    response.getSuccessCount(), response.getFailureCount());
        } catch (FirebaseMessagingException exception) {
            log.warn("Échec d'envoi FCM : {}", exception.getMessage());
        }
    }
}
