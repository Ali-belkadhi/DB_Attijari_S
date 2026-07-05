package com.attijari.reclamation.controller;

import com.attijari.reclamation.dto.CreateReclamationDto;
import com.attijari.reclamation.dto.UpdateReclamationDto;
import com.attijari.reclamation.model.Reclamation;
import com.attijari.reclamation.service.ReclamationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/reclamations")
@Tag(name = "reclamations", description = "Gestion des réclamations multi-destinataires")
public class ReclamationController {

    private final ReclamationService reclamationService;

    public ReclamationController(ReclamationService reclamationService) {
        this.reclamationService = reclamationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Envoyer une réclamation à une ou plusieurs équipes")
    public Reclamation create(@Valid @RequestBody CreateReclamationDto dto) {
        return reclamationService.create(dto);
    }

    @GetMapping
    @Operation(summary = "Lister toutes les réclamations")
    public List<Reclamation> findAll() {
        return reclamationService.findAll();
    }

    @GetMapping("/receiver/{idUser}")
    @Operation(summary = "Lister les réclamations reçues directement par un utilisateur")
    public List<Reclamation> findReceivedByUser(@PathVariable String idUser) {
        return reclamationService.findReceivedByUser(idUser);
    }

    @GetMapping("/user/{idUser}")
    @Operation(summary = "Lister toutes les réclamations d'un utilisateur (envoyées ET reçues)")
    public List<Reclamation> findByUser(@PathVariable String idUser) {
        return reclamationService.findByUser(idUser);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une réclamation par son ID technique")
    public Reclamation findOne(@PathVariable String id) {
        return reclamationService.findOne(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une réclamation")
    public Reclamation update(@PathVariable String id, @Valid @RequestBody UpdateReclamationDto dto) {
        return reclamationService.update(id, dto);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Modifier partiellement une réclamation")
    public Reclamation patch(@PathVariable String id, @Valid @RequestBody UpdateReclamationDto dto) {
        return reclamationService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une réclamation")
    public Map<String, String> remove(@PathVariable String id) {
        reclamationService.remove(id);
        return Map.of("message", "Réclamation supprimée avec succès");
    }
}
