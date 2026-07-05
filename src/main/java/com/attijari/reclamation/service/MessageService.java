package com.attijari.reclamation.service;

import com.attijari.reclamation.dto.CreateMessageDto;
import com.attijari.reclamation.model.Message;
import com.attijari.reclamation.model.Reclamation;
import com.attijari.reclamation.model.User;
import com.attijari.reclamation.repository.MessageRepository;
import com.attijari.reclamation.repository.ReclamationRepository;
import com.attijari.reclamation.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ReclamationRepository reclamationRepository;
    private final UserRepository userRepository;
    private final ReclamationParticipantService participantService;
    private final SimpMessagingTemplate messagingTemplate;
    private final AppNotificationService notificationService;

    public MessageService(
            MessageRepository messageRepository,
            ReclamationRepository reclamationRepository,
            UserRepository userRepository,
            ReclamationParticipantService participantService,
            SimpMessagingTemplate messagingTemplate,
            AppNotificationService notificationService
    ) {
        this.messageRepository = messageRepository;
        this.reclamationRepository = reclamationRepository;
        this.userRepository = userRepository;
        this.participantService = participantService;
        this.messagingTemplate = messagingTemplate;
        this.notificationService = notificationService;
    }

    public List<Message> findMessages(String reclamationId) {
        findReclamation(reclamationId);
        return messageRepository.findByReclamation_IdReclamationOrderByCreatedAtAsc(reclamationId);
    }

    @Transactional
    public Message sendMessage(String reclamationId, CreateMessageDto dto) {
        Reclamation reclamation = findReclamation(reclamationId);
        User sender = userRepository.findByIdUser(dto.getSenderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expéditeur introuvable"));

        if (!participantService.isParticipant(reclamationId, sender.getIdUser())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "L'utilisateur ne participe pas à cette discussion");
        }

        Message message = new Message();
        message.setReclamation(reclamation);
        message.setSender(sender);
        message.setContent(dto.getContent().trim());
        message.setMessageType(normalizeType(dto.getMessageType()));
        Message savedMessage = messageRepository.save(message);

        messagingTemplate.convertAndSend(
                "/topic/reclamations/" + reclamationId,
                Map.of("event", "MESSAGE_CREATED", "message", savedMessage)
        );
        notificationService.notifyUsers(
                reclamation,
                "MESSAGE_CREATED",
                "Nouveau message - " + reclamation.getObjet(),
                savedMessage.getContent(),
                participantService.findParticipantUsers(reclamationId),
                sender.getIdUser()
        );
        return savedMessage;
    }

    private Reclamation findReclamation(String reclamationId) {
        return reclamationRepository.findById(reclamationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Réclamation introuvable"));
    }

    private String normalizeType(String messageType) {
        if (messageType == null || messageType.isBlank()) return "TEXT";
        return messageType.trim().toUpperCase(Locale.ROOT);
    }
}
