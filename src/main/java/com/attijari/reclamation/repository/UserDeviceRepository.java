package com.attijari.reclamation.repository;

import com.attijari.reclamation.model.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, String> {

    Optional<UserDevice> findByFcmToken(String fcmToken);

    List<UserDevice> findByUser_IdUserInAndActiveTrue(Set<String> userIds);
}
