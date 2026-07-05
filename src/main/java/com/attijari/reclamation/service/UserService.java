package com.attijari.reclamation.service;

import com.attijari.reclamation.dto.CreateUserDto;
import com.attijari.reclamation.dto.UpdateUserDto;
import com.attijari.reclamation.model.Agence;
import com.attijari.reclamation.model.GroupeDroit;
import com.attijari.reclamation.model.User;
import com.attijari.reclamation.repository.AgenceRepository;
import com.attijari.reclamation.repository.GroupeDroitRepository;
import com.attijari.reclamation.repository.ReclamationRepository;
import com.attijari.reclamation.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AgenceRepository agenceRepository;
    private final GroupeDroitRepository groupeDroitRepository;
    private final ReclamationRepository reclamationRepository;

    public UserService(
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder,
            AgenceRepository agenceRepository,
            GroupeDroitRepository groupeDroitRepository,
            ReclamationRepository reclamationRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.agenceRepository = agenceRepository;
        this.groupeDroitRepository = groupeDroitRepository;
        this.reclamationRepository = reclamationRepository;
    }

    public User create(CreateUserDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        if (userRepository.existsByIdUser(dto.getIdUser())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User ID already exists");
        }

        User user = new User();
        user.setIdUser(dto.getIdUser());
        user.setNom(dto.getNom());
        user.setPrenom(dto.getPrenom());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());
        user.setTelephone(dto.getTelephone());
        user.setCin(dto.getCin());
        user.setDepartementId(dto.getDepartementId());
        user.setAgence(resolveAgence(dto.getAgenceId(), dto.getAgence()));
        user.setGroupeDroit(resolveGroupe(dto.getGroupId()));
        user.setImage(dto.getImage());
        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findOne(String id) {
        return userRepository.findByIdUser(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with ID " + id + " not found"));
    }

    public User update(String id, UpdateUserDto dto) {
        User user = findOne(id);
        if (dto.getNom() != null) user.setNom(dto.getNom());
        if (dto.getPrenom() != null) user.setPrenom(dto.getPrenom());
        if (dto.getEmail() != null) {
            if (!user.getEmail().equalsIgnoreCase(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
            }
            user.setEmail(dto.getEmail());
        }
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (dto.getRole() != null) user.setRole(dto.getRole());
        if (dto.getTelephone() != null) user.setTelephone(dto.getTelephone());
        if (dto.getCin() != null) user.setCin(dto.getCin());
        if (dto.getDepartementId() != null) user.setDepartementId(dto.getDepartementId());
        if (dto.getAgenceId() != null) user.setAgence(resolveAgence(dto.getAgenceId(), null));
        else if (dto.getAgence() != null) user.setAgence(resolveAgence(null, dto.getAgence()));
        if (dto.getGroupId() != null) user.setGroupeDroit(resolveGroupe(dto.getGroupId()));
        if (dto.getImage() != null) user.setImage(dto.getImage());
        return userRepository.save(user);
    }

    @Transactional
    public void remove(String id) {
        if (reclamationRepository.existsByClientIdUser(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet utilisateur possède encore des réclamations");
        }
        long deletedCount = userRepository.deleteByIdUser(id);
        if (deletedCount == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with ID " + id + " not found");
        }
    }

    public User updateImage(String id, String imagePath) {
        User user = findOne(id);
        user.setImage(imagePath);
        return userRepository.save(user);
    }

    private Agence resolveAgence(Long agenceId, String legacyAgence) {
        if (agenceId != null) {
            return agenceRepository.findById(agenceId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Agence introuvable"));
        }
        String value = normalize(legacyAgence);
        if (value == null) return null;
        try {
            Long parsedId = Long.valueOf(value);
            return agenceRepository.findById(parsedId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Agence introuvable"));
        } catch (NumberFormatException ignored) {
            return agenceRepository.findByCodeIgnoreCase(value)
                    .or(() -> agenceRepository.findByNomIgnoreCase(value))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Agence introuvable"));
        }
    }

    private GroupeDroit resolveGroupe(String groupId) {
        String value = normalize(groupId);
        if (value == null) return null;
        return groupeDroitRepository.findById(value)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Groupe de droits introuvable"));
    }

    private String normalize(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
