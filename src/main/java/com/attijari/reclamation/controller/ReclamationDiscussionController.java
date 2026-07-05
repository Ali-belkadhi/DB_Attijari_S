package com.attijari.reclamation.controller;

import com.attijari.reclamation.dto.AddParticipantDto;
import com.attijari.reclamation.dto.CreateMessageDto;
import com.attijari.reclamation.model.Message;
import com.attijari.reclamation.model.ReclamationParticipant;
import com.attijari.reclamation.service.MessageService;
import com.attijari.reclamation.service.ReclamationParticipantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reclamations/{reclamationId}")
@Tag(name = "reclamation-discussions", description = "Messages et participants des réclamations")
public class ReclamationDiscussionController {

    private final MessageService messageService;
    private final ReclamationParticipantService participantService;

    public ReclamationDiscussionController(
            MessageService messageService,
            ReclamationParticipantService participantService
    ) {
        this.messageService = messageService;
        this.participantService = participantService;
    }

    @GetMapping("/messages")
    @Operation(summary = "Lister les messages d'une réclamation")
    public List<Message> findMessages(@PathVariable String reclamationId) {
        return messageService.findMessages(reclamationId);
    }

    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Envoyer un message dans une réclamation")
    public Message sendMessage(
            @PathVariable String reclamationId,
            @Valid @RequestBody CreateMessageDto dto
    ) {
        return messageService.sendMessage(reclamationId, dto);
    }

    @PostMapping("/participants")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Ajouter un participant à une réclamation")
    public ReclamationParticipant addParticipant(
            @PathVariable String reclamationId,
            @Valid @RequestBody AddParticipantDto dto
    ) {
        return participantService.addParticipant(reclamationId, dto);
    }

    @GetMapping("/participants")
    @Operation(summary = "Lister les participants d'une réclamation")
    public List<ReclamationParticipant> findParticipants(@PathVariable String reclamationId) {
        return participantService.findParticipants(reclamationId);
    }
}
