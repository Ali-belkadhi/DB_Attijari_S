package com.attijari.reclamation.controller;

import com.attijari.reclamation.dto.CreateEquipeDto;
import com.attijari.reclamation.dto.UpdateEquipeDto;
import com.attijari.reclamation.model.Equipe;
import com.attijari.reclamation.service.EquipeService;
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
@RequestMapping("/equipes")
@Tag(name = "equipes", description = "Gestion des équipes destinataires")
public class EquipeController {

    private final EquipeService equipeService;

    public EquipeController(EquipeService equipeService) {
        this.equipeService = equipeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer une équipe")
    public Equipe create(@Valid @RequestBody CreateEquipeDto dto) {
        return equipeService.create(dto);
    }

    @GetMapping
    @Operation(summary = "Lister les équipes")
    public List<Equipe> findAll() {
        return equipeService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une équipe")
    public Equipe findOne(@PathVariable Long id) {
        return equipeService.findOne(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une équipe")
    public Equipe update(@PathVariable Long id, @Valid @RequestBody UpdateEquipeDto dto) {
        return equipeService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une équipe")
    public Map<String, String> remove(@PathVariable Long id) {
        equipeService.remove(id);
        return Map.of("message", "Équipe supprimée avec succès");
    }
}
