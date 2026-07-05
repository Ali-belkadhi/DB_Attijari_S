package com.attijari.reclamation.service;

import com.attijari.reclamation.model.AppNotification;
import com.attijari.reclamation.model.Reclamation;
import com.attijari.reclamation.model.User;
import com.attijari.reclamation.repository.AppNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Service
public class AppNotificationService {

    private final AppNotificationRepository notificationRepository;
    private final FirebaseNotificationService firebaseNotificationService;

    public AppNotificationService(
            AppNotificationRepository notificationRepository,
            FirebaseNotificationService firebaseNotificationService
    ) {
        this.notificationRepository = notificationRepository;
        this.firebaseNotificationService = firebaseNotificationService;
    }

    public void notifyUsers(
            Reclamation reclamation,
            String type,
            String title,
            String body,
            Collection<User> users,
            String excludedUserId
    ) {
        Map<String, User> recipients = new LinkedHashMap<>();
        for (User user : users) {
            if (user != null && !user.getIdUser().equals(excludedUserId)) {
                recipients.putIfAbsent(user.getIdUser(), user);
            }
        }
        if (recipients.isEmpty()) return;

        notificationRepository.saveAll(recipients.values().stream().map(user -> {
            AppNotification notification = new AppNotification();
            notification.setRecipient(user);
            notification.setReclamation(reclamation);
            notification.setType(type);
            notification.setTitle(title);
            notification.setBody(body);
            notification.setRead(false);
            return notification;
        }).toList());

        Set<String> recipientIds = Set.copyOf(recipients.keySet());
        Map<String, String> data = Map.of(
                "type", type,
                "reclamationId", reclamation.getIdReclamation()
        );
        Runnable pushAction = () -> firebaseNotificationService.sendToUsers(
                recipientIds, truncate(title, 150), truncate(body, 500), data);

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    pushAction.run();
                }
            });
        } else {
            pushAction.run();
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) return value;
        return value.substring(0, maxLength - 1) + "…";
    }
}
