package com.attijari.reclamation.service;

import com.attijari.reclamation.dto.AddParticipantDto;
import com.attijari.reclamation.dto.InviteParticipantsDto;
import com.attijari.reclamation.model.Equipe;
import com.attijari.reclamation.model.Reclamation;
import com.attijari.reclamation.model.ReclamationParticipant;
import com.attijari.reclamation.model.User;
import com.attijari.reclamation.model.UserRole;
import com.attijari.reclamation.repository.EquipeRepository;
import com.attijari.reclamation.repository.ReclamationParticipantRepository;
import com.attijari.reclamation.repository.ReclamationRepository;
import com.attijari.reclamation.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class ReclamationParticipantService {

    private final ReclamationParticipantRepository participantRepository;
    private final ReclamationRepository reclamationRepository;
    private final UserRepository userRepository;
    private final EquipeRepository equipeRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final AppNotificationService notificationService;

    public ReclamationParticipantService(
            ReclamationParticipantRepository participantRepository,
            ReclamationRepository reclamationRepository,
            UserRepository userRepository,
            EquipeRepository equipeRepository,
            SimpMessagingTemplate messagingTemplate,
            AppNotificationService notificationService
    ) {
        this.participantRepository = participantRepository;
        this.reclamationRepository = reclamationRepository;
        this.userRepository = userRepository;
        this.equipeRepository = equipeRepository;
        this.messagingTemplate = messagingTemplate;
        this.notificationService = notificationService;
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

    @Transactional
    public List<ReclamationParticipant> inviteParticipants(
            String reclamationId,
            InviteParticipantsDto dto
    ) {
        Reclamation reclamation = findReclamation(reclamationId);
        User inviter = userRepository.findByIdUser(dto.getInviterId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invitant introuvable"));

        if (inviter.getRole() != UserRole.EMPLOYEE_S) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Seul un utilisateur EMPLOYEE_S peut inviter des participants");
        }
        if (!isParticipant(reclamationId, inviter.getIdUser())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "L'invitant ne participe pas à cette discussion");
        }

        Set<User> targets = resolveInvitationTargets(dto);
        List<ReclamationParticipant> participants = new ArrayList<>();
        List<User> newlyInvitedUsers = new ArrayList<>();
        for (User target : targets) {
            boolean alreadyParticipant = isParticipant(reclamationId, target.getIdUser());
            participants.add(addIfMissing(reclamation, target, "INVITED"));
            if (!alreadyParticipant) newlyInvitedUsers.add(target);
        }

        if (!newlyInvitedUsers.isEmpty()) {
            messagingTemplate.convertAndSend(
                    topic(reclamationId),
                    Map.of("event", "PARTICIPANTS_INVITED", "participants", participants)
            );
            notificationService.notifyUsers(
                    reclamation,
                    "PARTICIPANT_INVITED",
                    "Invitation à une discussion",
                    reclamation.getObjet(),
                    newlyInvitedUsers,
                    inviter.getIdUser()
            );
        }
        return participants;
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

    private Set<User> resolveInvitationTargets(InviteParticipantsDto dto) {
        String targetType = dto.getTargetType().trim().toUpperCase(Locale.ROOT);
        if ("USER".equals(targetType)) {
            if (dto.getUserId() == null || dto.getUserId().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'utilisateur à inviter est requis");
            }
            User user = userRepository.findByIdUser(dto.getUserId().trim())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Utilisateur introuvable"));
            if (user.getRole() != UserRole.EMPLOYEE_S) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Seul un utilisateur EMPLOYEE_S peut être invité individuellement");
            }
            return new LinkedHashSet<>(Set.of(user));
        }
        if ("EQUIPE".equals(targetType)) {
            if (dto.getEquipeId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'équipe à inviter est requise");
            }
            Equipe equipe = equipeRepository.findById(dto.getEquipeId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Équipe introuvable"));
            if (!Boolean.TRUE.equals(equipe.getActif())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'équipe sélectionnée est inactive");
            }
            if (equipe.getMembers().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'équipe sélectionnée ne contient aucun membre");
            }
            return new LinkedHashSet<>(equipe.getMembers());
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "targetType doit être USER ou EQUIPE");
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
