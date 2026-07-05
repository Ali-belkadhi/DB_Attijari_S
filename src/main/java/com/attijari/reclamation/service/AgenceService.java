package com.attijari.reclamation.service;

import com.attijari.reclamation.dto.CreateAgenceDto;
import com.attijari.reclamation.dto.UpdateAgenceDto;
import com.attijari.reclamation.model.Agence;
import com.attijari.reclamation.repository.AgenceRepository;
import com.attijari.reclamation.repository.ReclamationRepository;
import com.attijari.reclamation.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AgenceService {

    private final AgenceRepository agenceRepository;
    private final UserRepository userRepository;
    private final ReclamationRepository reclamationRepository;

    public AgenceService(
            AgenceRepository agenceRepository,
            UserRepository userRepository,
            ReclamationRepository reclamationRepository
    ) {
        this.agenceRepository = agenceRepository;
        this.userRepository = userRepository;
        this.reclamationRepository = reclamationRepository;
    }

    public Agence create(CreateAgenceDto dto) {
        String code = dto.getCode().trim();
        if (agenceRepository.existsByCodeIgnoreCase(code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Une agence avec ce code existe déjà");
        }

        Agence agence = new Agence();
        agence.setCode(code);
        agence.setNom(dto.getNom().trim());
        agence.setAdresse(normalize(dto.getAdresse()));
        agence.setVille(normalize(dto.getVille()));
        agence.setTelephone(normalize(dto.getTelephone()));
        agence.setEmail(normalize(dto.getEmail()));
        agence.setDirecteur(normalize(dto.getDirecteur()));
        return agenceRepository.save(agence);
    }

    public List<Agence> findAll() {
        return agenceRepository.findAll();
    }

    public Agence findOne(Long id) {
        return agenceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agence introuvable avec l'ID " + id));
    }

    public Agence update(Long id, UpdateAgenceDto dto) {
        Agence agence = findOne(id);
        if (dto.getCode() != null) {
            String code = requireValue(dto.getCode(), "Le code de l'agence est requis");
            if (agenceRepository.existsByCodeIgnoreCaseAndIdAgenceNot(code, id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Une agence avec ce code existe déjà");
            }
            agence.setCode(code);
        }
        if (dto.getNom() != null) agence.setNom(requireValue(dto.getNom(), "Le nom de l'agence est requis"));
        if (dto.getAdresse() != null) agence.setAdresse(normalize(dto.getAdresse()));
        if (dto.getVille() != null) agence.setVille(normalize(dto.getVille()));
        if (dto.getTelephone() != null) agence.setTelephone(normalize(dto.getTelephone()));
        if (dto.getEmail() != null) agence.setEmail(normalize(dto.getEmail()));
        if (dto.getDirecteur() != null) agence.setDirecteur(normalize(dto.getDirecteur()));
        return agenceRepository.save(agence);
    }

    @Transactional
    public void remove(Long id) {
        Agence agence = findOne(id);
        if (userRepository.existsByAgence_IdAgence(id) || reclamationRepository.existsByAgenceIdAgence(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette agence est encore utilisée");
        }
        agenceRepository.delete(agence);
    }

    private String normalize(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String requireValue(String value, String message) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return normalized;
    }
}
