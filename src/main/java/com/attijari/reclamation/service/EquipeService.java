package com.attijari.reclamation.service;

import com.attijari.reclamation.dto.CreateEquipeDto;
import com.attijari.reclamation.dto.UpdateEquipeDto;
import com.attijari.reclamation.model.Equipe;
import com.attijari.reclamation.model.User;
import com.attijari.reclamation.repository.EquipeRepository;
import com.attijari.reclamation.repository.ReclamationRepository;
import com.attijari.reclamation.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class EquipeService {

    private final EquipeRepository equipeRepository;
    private final ReclamationRepository reclamationRepository;
    private final UserRepository userRepository;

    public EquipeService(
            EquipeRepository equipeRepository,
            ReclamationRepository reclamationRepository,
            UserRepository userRepository
    ) {
        this.equipeRepository = equipeRepository;
        this.reclamationRepository = reclamationRepository;
        this.userRepository = userRepository;
    }

    public Equipe create(CreateEquipeDto dto) {
        String code = requireValue(dto.getCode(), "Le code de l'équipe est requis");
        if (equipeRepository.existsByCodeIgnoreCase(code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Une équipe avec ce code existe déjà");
        }
        Equipe equipe = new Equipe();
        equipe.setCode(code);
        equipe.setNom(requireValue(dto.getNom(), "Le nom de l'équipe est requis"));
        equipe.setDescription(normalize(dto.getDescription()));
        equipe.setActif(dto.getActif() == null || dto.getActif());
        equipe.setMembers(resolveMembers(dto.getMemberIds()));
        return equipeRepository.save(equipe);
    }

    public List<Equipe> findAll() {
        return equipeRepository.findAll();
    }

    public Equipe findOne(Long id) {
        return equipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Équipe introuvable"));
    }

    public Equipe update(Long id, UpdateEquipeDto dto) {
        Equipe equipe = findOne(id);
        if (dto.getCode() != null) {
            String code = requireValue(dto.getCode(), "Le code de l'équipe est requis");
            if (equipeRepository.existsByCodeIgnoreCaseAndIdEquipeNot(code, id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Une équipe avec ce code existe déjà");
            }
            equipe.setCode(code);
        }
        if (dto.getNom() != null) equipe.setNom(requireValue(dto.getNom(), "Le nom de l'équipe est requis"));
        if (dto.getDescription() != null) equipe.setDescription(normalize(dto.getDescription()));
        if (dto.getActif() != null) equipe.setActif(dto.getActif());
        if (dto.getMemberIds() != null) equipe.setMembers(resolveMembers(dto.getMemberIds()));
        return equipeRepository.save(equipe);
    }

    @Transactional
    public void remove(Long id) {
        Equipe equipe = findOne(id);
        if (reclamationRepository.existsByDestinations_IdEquipe(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette équipe reçoit encore des réclamations");
        }
        equipe.getMembers().clear();
        equipeRepository.delete(equipe);
    }

    private Set<User> resolveMembers(Set<String> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) return new LinkedHashSet<>();
        Set<String> uniqueIds = new LinkedHashSet<>(memberIds);
        List<User> users = userRepository.findAllByIdUserIn(uniqueIds);
        if (users.size() != uniqueIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Un ou plusieurs membres sont introuvables");
        }
        return new LinkedHashSet<>(users);
    }

    private String normalize(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String requireValue(String value, String message) {
        String normalized = normalize(value);
        if (normalized == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        return normalized;
    }
}
