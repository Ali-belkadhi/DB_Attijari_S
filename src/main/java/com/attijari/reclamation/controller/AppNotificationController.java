package com.attijari.reclamation.controller;

import com.attijari.reclamation.dto.AppNotificationDto;
import com.attijari.reclamation.model.AppNotification;
import com.attijari.reclamation.repository.AppNotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class AppNotificationController {

    private final AppNotificationRepository notificationRepository;

    public AppNotificationController(AppNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<AppNotificationDto>> getMyNotifications(@PathVariable String userId) {
        if (userId == null || userId.isBlank()) return ResponseEntity.badRequest().build();
        List<AppNotification> notifications = notificationRepository.findByRecipient_IdUserOrderByCreatedAtDesc(userId);
        List<AppNotificationDto> dtos = notifications.stream().map(notif -> {
            AppNotificationDto dto = new AppNotificationDto();
            dto.setId(notif.getId());
            dto.setReclamationId(notif.getReclamation().getIdReclamation());
            dto.setType(notif.getType());
            dto.setTitle(notif.getTitle());
            dto.setBody(notif.getBody());
            dto.setRead(notif.getRead());
            dto.setCreatedAt(notif.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
