package com.attijari.reclamation.service;

import com.attijari.reclamation.dto.AddParticipantDto;
import com.attijari.reclamation.model.Reclamation;
import com.attijari.reclamation.model.ReclamationParticipant;
import com.attijari.reclamation.model.User;
import com.attijari.reclamation.repository.ReclamationParticipantRepository;
import com.attijari.reclamation.repository.ReclamationRepository;
import com.attijari.reclamation.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ReclamationParticipantService {

    private final ReclamationParticipantRepository participantRepository;
    private final ReclamationRepository reclamationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ReclamationParticipantService(
            ReclamationParticipantRepository participantRepository,
            ReclamationRepository reclamationRepository,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.participantRepository = participantRepository;
        this.reclamationRepository = reclamationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public ReclamationParticipant addParticipant(String reclamationId, AddParticipantDto dto) {
        Reclamation reclamation = findReclamation(reclamationId);
        User user = userRepository.findByIdUser(dto.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Utilisateur introuvable"));

        ReclamationParticipant participant = addIfMissing(
                reclamation,
                user,
                normalizeRole(dto.getRoleInChat())
        );
        messagingTemplate.convertAndSend(
                topic(reclamationId),
                Map.of("event", "PARTICIPANT_ADDED", "participant", participant)
        );
        return participant;
    }

    public List<ReclamationParticipant> findParticipants(String reclamationId) {
        findReclamation(reclamationId);
        return participantRepository.findByReclamation_IdReclamationOrderByAddedAtAsc(reclamationId);
    }

    public List<User> findParticipantUsers(String reclamationId) {
        return findParticipants(reclamationId).stream()
                .map(ReclamationParticipant::getUser)
                .toList();
    }

    public boolean isParticipant(String reclamationId, String userId) {
        return participantRepository.existsByReclamation_IdReclamationAndUser_IdUser(reclamationId, userId);
    }

    public List<Reclamation> findMyDiscussions(String idUser) {
        if (!userRepository.existsByIdUser(idUser)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable");
        }
        Map<String, Reclamation> discussions = new LinkedHashMap<>();
        for (ReclamationParticipant participant : participantRepository.findByUser_IdUserOrderByAddedAtDesc(idUser)) {
            Reclamation reclamation = participant.getReclamation();
            discussions.putIfAbsent(reclamation.getIdReclamation(), reclamation);
        }
        return List.copyOf(discussions.values());
    }

    @Transactional
    public void addInitialParticipants(Reclamation reclamation) {
        addIfMissing(reclamation, reclamation.getSender(), "OWNER");
        for (User receiver : reclamation.getReceivers()) {
            addIfMissing(reclamation, receiver, "RECEIVER");
        }
        reclamation.getDestinations().forEach(equipe ->
                equipe.getMembers().forEach(member -> addIfMissing(reclamation, member, "TEAM_MEMBER"))
        );
    }

    private ReclamationParticipant addIfMissing(Reclamation reclamation, User user, String roleInChat) {
        return participantRepository
                .findByReclamation_IdReclamationAndUser_IdUser(reclamation.getIdReclamation(), user.getIdUser())
                .orElseGet(() -> {
                    ReclamationParticipant participant = new ReclamationParticipant();
                    participant.setReclamation(reclamation);
                    participant.setUser(user);
                    participant.setRoleInChat(roleInChat);
                    return participantRepository.save(participant);
                });
    }

    private Reclamation findReclamation(String reclamationId) {
        return reclamationRepository.findById(reclamationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Réclamation introuvable"));
    }

    private String normalizeRole(String role) {
        String normalized = role == null ? "PARTICIPANT" : role.trim().toUpperCase(Locale.ROOT);
        return normalized.isEmpty() ? "PARTICIPANT" : normalized;
    }

    private String topic(String reclamationId) {
        return "/topic/reclamations/" + reclamationId;
    }
}
