package com.attijari.reclamation.service;

import com.attijari.reclamation.dto.LoginDto;
import com.attijari.reclamation.dto.LoginResponseDto;
import com.attijari.reclamation.model.User;
import com.attijari.reclamation.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponseDto login(LoginDto loginDto) {
        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect."));

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect.");
        }

        LoginResponseDto.UserSummary userSummary = new LoginResponseDto.UserSummary(
                user.getIdUser(),
                user.getNom(),
                user.getPrenom(),
                user.getEmail(),
                user.getRole(),
                user.getTelephone(),
                user.getCin(),
                user.getAgenceNom(),
                user.getIdEquipe(),
                user.getImage()
        );

        return new LoginResponseDto("Connexion réussie", userSummary);
    }
}
