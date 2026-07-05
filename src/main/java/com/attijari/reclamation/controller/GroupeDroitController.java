package com.attijari.reclamation.controller;

import com.attijari.reclamation.dto.CreateGroupeDroitDto;
import com.attijari.reclamation.dto.UpdateGroupeDroitDto;
import com.attijari.reclamation.model.GroupeDroit;
import com.attijari.reclamation.service.GroupeDroitService;
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
@RequestMapping("/groupes-droits")
@Tag(name = "groupes-droits", description = "Gestion des groupes de droits")
public class GroupeDroitController {

    private final GroupeDroitService groupeDroitService;

    public GroupeDroitController(GroupeDroitService groupeDroitService) {
        this.groupeDroitService = groupeDroitService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un groupe de droits")
    public GroupeDroit create(@Valid @RequestBody CreateGroupeDroitDto dto) {
        return groupeDroitService.create(dto);
    }

    @GetMapping
    @Operation(summary = "Lister les groupes de droits")
    public List<GroupeDroit> findAll() {
        return groupeDroitService.findAll();
    }

    @GetMapping("/{groupId}")
    @Operation(summary = "Obtenir un groupe de droits")
    public GroupeDroit findOne(@PathVariable String groupId) {
        return groupeDroitService.findOne(groupId);
    }

    @PutMapping("/{groupId}")
    @Operation(summary = "Modifier un groupe de droits")
    public GroupeDroit update(@PathVariable String groupId, @Valid @RequestBody UpdateGroupeDroitDto dto) {
        return groupeDroitService.update(groupId, dto);
    }

    @DeleteMapping("/{groupId}")
    @Operation(summary = "Supprimer un groupe de droits")
    public Map<String, String> remove(@PathVariable String groupId) {
        groupeDroitService.remove(groupId);
        return Map.of("message", "Groupe de droits supprimé avec succès");
    }
}
