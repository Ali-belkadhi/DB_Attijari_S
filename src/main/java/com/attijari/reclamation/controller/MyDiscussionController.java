package com.attijari.reclamation.controller;

import com.attijari.reclamation.model.Reclamation;
import com.attijari.reclamation.service.ReclamationParticipantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reclamations/my-discussions")
@Tag(name = "reclamation-discussions", description = "Discussions accessibles par utilisateur")
public class MyDiscussionController {

    private final ReclamationParticipantService participantService;

    public MyDiscussionController(ReclamationParticipantService participantService) {
        this.participantService = participantService;
    }

    @GetMapping("/{idUser}")
    @Operation(summary = "Lister les discussions d'un utilisateur")
    public List<Reclamation> findMyDiscussions(@PathVariable String idUser) {
        return participantService.findMyDiscussions(idUser);
    }
}
