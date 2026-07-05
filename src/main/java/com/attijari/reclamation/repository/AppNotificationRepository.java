package com.attijari.reclamation.repository;

import com.attijari.reclamation.model.AppNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppNotificationRepository extends JpaRepository<AppNotification, String> {

    List<AppNotification> findByRecipient_IdUserOrderByCreatedAtDesc(String idUser);
}
