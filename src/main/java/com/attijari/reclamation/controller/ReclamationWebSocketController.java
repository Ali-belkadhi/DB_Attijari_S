package com.attijari.reclamation.controller;

import com.attijari.reclamation.dto.CreateMessageDto;
import com.attijari.reclamation.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class ReclamationWebSocketController {

    private final MessageService messageService;

    public ReclamationWebSocketController(MessageService messageService) {
        this.messageService = messageService;
    }

    @MessageMapping("/reclamations/{reclamationId}/send")
    public void sendMessage(
            @DestinationVariable String reclamationId,
            @Valid @Payload CreateMessageDto dto
    ) {
        messageService.sendMessage(reclamationId, dto);
    }
}
