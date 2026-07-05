package com.attijari.reclamation.service;

import com.attijari.reclamation.dto.RegisterDeviceTokenDto;
import com.attijari.reclamation.model.User;
import com.attijari.reclamation.model.UserDevice;
import com.attijari.reclamation.repository.UserDeviceRepository;
import com.attijari.reclamation.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class UserDeviceService {

    private final UserDeviceRepository userDeviceRepository;
    private final UserRepository userRepository;

    public UserDeviceService(UserDeviceRepository userDeviceRepository, UserRepository userRepository) {
        this.userDeviceRepository = userDeviceRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public UserDevice registerToken(RegisterDeviceTokenDto dto) {
        User user = userRepository.findByIdUser(dto.getUserId().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Utilisateur introuvable"));

        String token = dto.getFcmToken().trim();
        UserDevice device = userDeviceRepository.findByFcmToken(token).orElseGet(UserDevice::new);
        device.setUser(user);
        device.setFcmToken(token);
        device.setDeviceType(dto.getDeviceType().trim().toUpperCase(Locale.ROOT));
        device.setActive(true);
        device.setLastActive(LocalDateTime.now());
        return userDeviceRepository.save(device);
    }
}
