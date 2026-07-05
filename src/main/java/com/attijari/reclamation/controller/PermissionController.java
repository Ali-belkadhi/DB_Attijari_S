package com.attijari.reclamation.controller;

import com.attijari.reclamation.dto.CreatePermissionDto;
import com.attijari.reclamation.dto.UpdatePermissionDto;
import com.attijari.reclamation.model.Permission;
import com.attijari.reclamation.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/permissions")
@Tag(name = "permissions", description = "Gestion des permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer une permission")
    public Permission create(@Valid @RequestBody CreatePermissionDto dto) {
        return permissionService.create(dto);
    }

    @GetMapping
    @Operation(summary = "Lister les permissions")
    public List<Permission> findAll() {
        return permissionService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une permission")
    public Permission findOne(@PathVariable Long id) {
        return permissionService.findOne(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une permission")
    public Permission update(@PathVariable Long id, @Valid @RequestBody UpdatePermissionDto dto) {
        return permissionService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une permission")
    public Map<String, String> remove(@PathVariable Long id) {
        permissionService.remove(id);
        return Map.of("message", "Permission supprimée avec succès");
    }
}
