package com.attijari.reclamation.service;

import com.attijari.reclamation.dto.CreatePermissionDto;
import com.attijari.reclamation.dto.UpdatePermissionDto;
import com.attijari.reclamation.model.GroupeDroit;
import com.attijari.reclamation.model.Permission;
import com.attijari.reclamation.repository.GroupeDroitRepository;
import com.attijari.reclamation.repository.PermissionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final GroupeDroitRepository groupeDroitRepository;

    public PermissionService(PermissionRepository permissionRepository, GroupeDroitRepository groupeDroitRepository) {
        this.permissionRepository = permissionRepository;
        this.groupeDroitRepository = groupeDroitRepository;
    }

    public Permission create(CreatePermissionDto dto) {
        String code = requireValue(dto.getCode(), "Le code de la permission est requis");
        if (permissionRepository.existsByCodeIgnoreCase(code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Une permission avec ce code existe déjà");
        }

        Permission permission = new Permission();
        permission.setCode(code);
        permission.setNom(requireValue(dto.getNom(), "Le nom de la permission est requis"));
        permission.setDescription(normalize(dto.getDescription()));
        return permissionRepository.save(permission);
    }

    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }

    public Permission findOne(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Permission introuvable"));
    }

    public Permission update(Long id, UpdatePermissionDto dto) {
        Permission permission = findOne(id);
        if (dto.getCode() != null) {
            String code = requireValue(dto.getCode(), "Le code de la permission est requis");
            if (permissionRepository.existsByCodeIgnoreCaseAndIdPermissionNot(code, id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Une permission avec ce code existe déjà");
            }
            permission.setCode(code);
        }
        if (dto.getNom() != null) permission.setNom(requireValue(dto.getNom(), "Le nom de la permission est requis"));
        if (dto.getDescription() != null) permission.setDescription(normalize(dto.getDescription()));
        return permissionRepository.save(permission);
    }

    @Transactional
    public void remove(Long id) {
        Permission permission = findOne(id);
        for (GroupeDroit groupe : groupeDroitRepository.findAll()) {
            groupe.getPermissions().remove(permission);
        }
        permissionRepository.delete(permission);
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
