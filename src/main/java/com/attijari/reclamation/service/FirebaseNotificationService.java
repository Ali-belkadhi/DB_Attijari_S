package com.attijari.reclamation.service;

import com.attijari.reclamation.model.Equipe;
import com.attijari.reclamation.model.User;
import com.attijari.reclamation.model.UserDevice;
import com.attijari.reclamation.repository.UserDeviceRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FirebaseNotificationService {

    private static final int FCM_BATCH_SIZE = 500;

    private final UserDeviceRepository userDeviceRepository;

    public FirebaseNotificationService(UserDeviceRepository userDeviceRepository) {
        this.userDeviceRepository = userDeviceRepository;
    }

    private FirebaseMessaging getMessaging() {
        if (FirebaseApp.getApps().isEmpty()) {
            return null;
        }
        return FirebaseMessaging.getInstance();
    }

    public void sendToToken(String token, String title, String body, Map<String, String> data) {
        FirebaseMessaging messaging = getMessaging();
        if (messaging == null || token == null || token.trim().isEmpty()) return;

        Message message = Message.builder()
                .setToken(token)
                .setNotification(buildNotification(title, body))
                .putAllData(data)
                .setAndroidConfig(buildAndroidConfig())
                .setApnsConfig(buildApnsConfig())
                .build();

        try {
            messaging.send(message);
            log.info("Push FCM envoyé avec succès au token: {}", token);
        } catch (FirebaseMessagingException e) {
            handleException(e, token);
        }
    }

    public void sendToUser(User user, String title, String body, Map<String, String> data) {
        if (user == null) return;
        sendToUsers(Set.of(user.getIdUser()), title, body, data);
    }

    public void sendToUsers(Set<String> userIds, String title, String body, Map<String, String> data) {
        FirebaseMessaging messaging = getMessaging();
        if (messaging == null || userIds == null || userIds.isEmpty()) return;

        List<UserDevice> devices = userDeviceRepository.findByUser_IdUserInAndActiveTrue(userIds);
        if (devices.isEmpty()) return;

        List<String> tokens = devices.stream().map(UserDevice::getFcmToken).distinct().toList();

        for (int start = 0; start < tokens.size(); start += FCM_BATCH_SIZE) {
            int end = Math.min(start + FCM_BATCH_SIZE, tokens.size());
            sendBatch(new ArrayList<>(tokens.subList(start, end)), title, body, data);
        }
    }

    public void sendToEquipe(Equipe equipe, String title, String body, Map<String, String> data) {
        if (equipe == null || equipe.getMembers() == null) return;
        
        Set<String> userIds = equipe.getMembers().stream()
                .map(User::getIdUser)
                .collect(Collectors.toSet());
                
        sendToUsers(userIds, title, body, data);
    }

    protected void sendBatch(List<String> tokens, String title, String body, Map<String, String> data) {
        if (tokens.isEmpty()) return;
        FirebaseMessaging messaging = getMessaging();
        if (messaging == null) return;

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(buildNotification(title, body))
                .putAllData(data)
                .setAndroidConfig(buildAndroidConfig())
                .setApnsConfig(buildApnsConfig())
                .addAllTokens(tokens)
                .build();

        try {
            BatchResponse response = messaging.sendEachForMulticast(message);
            log.info("Push FCM batch envoyé : {} succès, {} échecs", response.getSuccessCount(), response.getFailureCount());
            
            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    SendResponse res = responses.get(i);
                    if (!res.isSuccessful()) {
                        FirebaseMessagingException e = res.getException();
                        handleException(e, tokens.get(i));
                    }
                }
            }
        } catch (FirebaseMessagingException e) {
            log.warn("Échec global d'envoi FCM batch : {}", e.getMessage());
        }
    }

    private Notification buildNotification(String title, String body) {
        return Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();
    }

    private AndroidConfig buildAndroidConfig() {
        return AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder()
                        .setSound("default")
                        .build())
                .build();
    }

    private ApnsConfig buildApnsConfig() {
        return ApnsConfig.builder()
                .setAps(Aps.builder()
                        .setSound("default")
                        .build())
                .build();
    }

    @Transactional
    protected void handleException(FirebaseMessagingException e, String token) {
        MessagingErrorCode errorCode = e.getMessagingErrorCode();
        if (errorCode == MessagingErrorCode.UNREGISTERED || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
            log.info("Désactivation du token invalide ou désinscrit: {}", token);
            deactivateToken(token);
        } else {
            log.warn("Erreur temporaire FCM pour le token {}: {}", token, e.getMessage());
        }
    }

    private void deactivateToken(String token) {
        try {
            userDeviceRepository.findByFcmToken(token).ifPresent(device -> {
                device.setActive(false);
                userDeviceRepository.save(device);
            });
        } catch (Exception e) {
            log.error("Impossible de désactiver le token en base: {}", token, e);
        }
    }
}
