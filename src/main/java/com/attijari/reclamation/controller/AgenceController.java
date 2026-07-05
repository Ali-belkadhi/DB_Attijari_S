package com.attijari.reclamation.controller;

import com.attijari.reclamation.dto.CreateAgenceDto;
import com.attijari.reclamation.dto.UpdateAgenceDto;
import com.attijari.reclamation.model.Agence;
import com.attijari.reclamation.service.AgenceService;
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
@RequestMapping("/agences")
@Tag(name = "agences", description = "Gestion des agences")
public class AgenceController {

    private final AgenceService agenceService;

    public AgenceController(AgenceService agenceService) {
        this.agenceService = agenceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer une agence")
    public Agence create(@Valid @RequestBody CreateAgenceDto createAgenceDto) {
        return agenceService.create(createAgenceDto);
    }

    @GetMapping
    @Operation(summary = "Lister toutes les agences")
    public List<Agence> findAll() {
        return agenceService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une agence par son ID")
    public Agence findOne(@PathVariable Long id) {
        return agenceService.findOne(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une agence")
    public Agence update(@PathVariable Long id, @Valid @RequestBody UpdateAgenceDto updateAgenceDto) {
        return agenceService.update(id, updateAgenceDto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une agence")
    public Map<String, String> remove(@PathVariable Long id) {
        agenceService.remove(id);
        return Map.of("message", "Agence supprimée avec succès");
    }
}
