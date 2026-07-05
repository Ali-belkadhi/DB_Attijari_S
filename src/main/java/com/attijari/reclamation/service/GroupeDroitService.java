package com.attijari.reclamation.service;

import com.attijari.reclamation.dto.CreateGroupeDroitDto;
import com.attijari.reclamation.dto.UpdateGroupeDroitDto;
import com.attijari.reclamation.model.GroupeDroit;
import com.attijari.reclamation.model.Permission;
import com.attijari.reclamation.repository.GroupeDroitRepository;
import com.attijari.reclamation.repository.PermissionRepository;
import com.attijari.reclamation.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class GroupeDroitService {

    private final GroupeDroitRepository groupeDroitRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    public GroupeDroitService(
            GroupeDroitRepository groupeDroitRepository,
            PermissionRepository permissionRepository,
            UserRepository userRepository
    ) {
        this.groupeDroitRepository = groupeDroitRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
    }

    public GroupeDroit create(CreateGroupeDroitDto dto) {
        String nom = requireValue(dto.getNom(), "Le nom du groupe est requis");
        if (groupeDroitRepository.existsByNomIgnoreCase(nom)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un groupe avec ce nom existe déjà");
        }

        GroupeDroit groupe = new GroupeDroit();
        groupe.setNom(nom);
        groupe.setDescription(normalize(dto.getDescription()));
        groupe.setActif(dto.getActif() == null || dto.getActif());
        groupe.setPermissions(resolvePermissions(dto.getPermissionIds()));
        return groupeDroitRepository.save(groupe);
    }

    public List<GroupeDroit> findAll() {
        return groupeDroitRepository.findAll();
    }

    public GroupeDroit findOne(String groupId) {
        return groupeDroitRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Groupe de droits introuvable"));
    }

    public GroupeDroit update(String groupId, UpdateGroupeDroitDto dto) {
        GroupeDroit groupe = findOne(groupId);
        if (dto.getNom() != null) {
            String nom = requireValue(dto.getNom(), "Le nom du groupe est requis");
            if (groupeDroitRepository.existsByNomIgnoreCaseAndGroupIdNot(nom, groupId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Un groupe avec ce nom existe déjà");
            }
            groupe.setNom(nom);
        }
        if (dto.getDescription() != null) groupe.setDescription(normalize(dto.getDescription()));
        if (dto.getActif() != null) groupe.setActif(dto.getActif());
        if (dto.getPermissionIds() != null) groupe.setPermissions(resolvePermissions(dto.getPermissionIds()));
        return groupeDroitRepository.save(groupe);
    }

    @Transactional
    public void remove(String groupId) {
        GroupeDroit groupe = findOne(groupId);
        if (userRepository.existsByGroupeDroit_GroupId(groupId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce groupe est encore affecté à des utilisateurs");
        }
        groupe.getPermissions().clear();
        groupeDroitRepository.delete(groupe);
    }

    private Set<Permission> resolvePermissions(Set<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        Set<Long> uniqueIds = new LinkedHashSet<>(permissionIds);
        List<Permission> permissions = permissionRepository.findAllById(uniqueIds);
        if (permissions.size() != uniqueIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Une ou plusieurs permissions sont introuvables");
        }
        return new LinkedHashSet<>(permissions);
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
