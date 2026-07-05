package com.attijari.reclamation.service;

import com.attijari.reclamation.dto.CreateReclamationDto;
import com.attijari.reclamation.dto.UpdateReclamationDto;
import com.attijari.reclamation.model.Agence;
import com.attijari.reclamation.model.Equipe;
import com.attijari.reclamation.model.Reclamation;
import com.attijari.reclamation.model.User;
import com.attijari.reclamation.repository.AgenceRepository;
import com.attijari.reclamation.repository.EquipeRepository;
import com.attijari.reclamation.repository.ReclamationRepository;
import com.attijari.reclamation.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class ReclamationService {

    private static final String DESTINATION_USER = "USER";
    private static final String DESTINATION_EQUIPE = "EQUIPE";

    private final ReclamationRepository reclamationRepository;
    private final UserRepository userRepository;
    private final AgenceRepository agenceRepository;
    private final EquipeRepository equipeRepository;
    private final ReclamationParticipantService participantService;
    private final AppNotificationService notificationService;

    public ReclamationService(
            ReclamationRepository reclamationRepository,
            UserRepository userRepository,
            AgenceRepository agenceRepository,
            EquipeRepository equipeRepository,
            ReclamationParticipantService participantService,
            AppNotificationService notificationService) {
        this.reclamationRepository = reclamationRepository;
        this.userRepository = userRepository;
        this.agenceRepository = agenceRepository;
        this.equipeRepository = equipeRepository;
        this.participantService = participantService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Reclamation create(CreateReclamationDto dto) {
        String destinationType = requireValue(
                dto.getDestinationType(),
                "Le type de destination USER ou EQUIPE est requis").toUpperCase(Locale.ROOT);

        Reclamation reclamation = new Reclamation();
        reclamation.setIdReclamation(generateBusinessId());
        reclamation.setSender(resolveSender(dto.getSenderId()));
        reclamation.setObjet(dto.getObjet().trim());
        reclamation.setType(dto.getType().trim());
        reclamation.setStatut(normalize(dto.getStatut()) == null ? "NOUVELLE" : dto.getStatut().trim());
        reclamation.setDescription(dto.getDescription().trim());
        reclamation.setReference(reclamation.getIdReclamation());
        reclamation.setTitre(reclamation.getObjet());
        reclamation.setPriorite("NORMALE");
        reclamation.setClientAccessDiscussion(false);
        reclamation.setDestinationType(destinationType);
        reclamation.setAgence(resolveAgence(dto.getAgenceId()));

        switch (destinationType) {
            case DESTINATION_USER -> {
                reclamation.setReceivers(resolveReceivers(dto.getReceiverIds()));
                reclamation.setDestinations(new LinkedHashSet<>());

            }
            case DESTINATION_EQUIPE -> {
                reclamation.setDestinations(resolveDestinations(dto.getDestinationIds()));
                reclamation.setReceivers(new LinkedHashSet<>());
            }
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "destinationType doit être USER ou EQUIPE");
        }

        Reclamation savedReclamation = reclamationRepository.save(reclamation);
        participantService.addInitialParticipants(savedReclamation);
        notificationService.notifyUsers(
                savedReclamation,
                "RECLAMATION_CREATED",
                "Nouvelle réclamation",
                savedReclamation.getObjet(),
                participantService.findParticipantUsers(savedReclamation.getIdReclamation()),
                savedReclamation.getSender().getIdUser()
        );
        return savedReclamation;
    }

    public List<Reclamation> findAll() {
        return reclamationRepository.findAll();
    }

    public List<Reclamation> findReceivedByUser(String idUser) {
        if (!userRepository.existsByIdUser(idUser)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable");
        }
        return reclamationRepository.findByReceivers_IdUserOrderByCreatedAtDesc(idUser);
    }

    /**
     * Retourne toutes les réclamations liées à un utilisateur :
     * celles qu'il a envoyées ET celles qu'il a reçues (sender OR receiver).
     */
    public List<Reclamation> findByUser(String idUser) {
        if (!userRepository.existsByIdUser(idUser)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable");
        }
        return reclamationRepository.findBySenderOrReceiver(idUser);
    }

    public Reclamation findOne(String id) {
        return reclamationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Réclamation introuvable"));
    }

    public Reclamation update(String id, UpdateReclamationDto dto) {
        Reclamation reclamation = findOne(id);
        if (dto.getSenderId() != null)
            reclamation.setSender(resolveSender(dto.getSenderId()));
        if (dto.getObjet() != null)
            reclamation.setObjet(requireValue(dto.getObjet(), "L'objet est requis"));
        if (dto.getType() != null)
            reclamation.setType(requireValue(dto.getType(), "Le type est requis"));
        if (dto.getStatut() != null)
            reclamation.setStatut(requireValue(dto.getStatut(), "Le statut est requis"));
        if (dto.getDescription() != null)
            reclamation.setDescription(requireValue(dto.getDescription(), "La description est requise"));
        if (dto.getDestinationIds() != null) {
            reclamation.setDestinationType(DESTINATION_EQUIPE);
            reclamation.setDestinations(resolveDestinations(dto.getDestinationIds()));
            reclamation.getReceivers().clear();
        }
        if (dto.getAgenceId() != null)
            reclamation.setAgence(resolveAgence(dto.getAgenceId()));
        Reclamation savedReclamation = reclamationRepository.save(reclamation);
        participantService.addInitialParticipants(savedReclamation);

        return savedReclamation;
    }

    @Transactional
    public void remove(String id) {
        Reclamation reclamation = findOne(id);
        reclamation.getReceivers().clear();
        reclamation.getDestinations().clear();
        reclamationRepository.delete(reclamation);
    }

    private User resolveSender(String senderId) {
        String value = requireValue(senderId, "L'expéditeur est requis");
        return userRepository.findByIdUser(value)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expéditeur introuvable"));
    }

    private Set<User> resolveReceivers(Set<String> receiverIds) {
        if (receiverIds == null || receiverIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Au moins un utilisateur destinataire est requis");
        }

        Set<String> uniqueIds = new LinkedHashSet<>();
        for (String receiverId : receiverIds) {
            uniqueIds.add(requireValue(receiverId, "Un identifiant destinataire est vide"));
        }

        List<User> receivers = userRepository.findAllByIdUserIn(uniqueIds);
        if (receivers.size() != uniqueIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Un ou plusieurs utilisateurs destinataires sont introuvables");
        }
        return new LinkedHashSet<>(receivers);
    }

    private Set<Equipe> resolveDestinations(Set<Long> destinationIds) {
        if (destinationIds == null || destinationIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Au moins une équipe destinataire est requise");
        }
        Set<Long> uniqueIds = new LinkedHashSet<>(destinationIds);
        List<Equipe> equipes = equipeRepository.findAllById(uniqueIds);
        if (equipes.size() != uniqueIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Une ou plusieurs équipes sont introuvables");
        }
        if (equipes.stream().anyMatch(equipe -> !Boolean.TRUE.equals(equipe.getActif()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Une équipe destinataire est inactive");
        }
        return new LinkedHashSet<>(equipes);
    }

    private Agence resolveAgence(Long agenceId) {
        if (agenceId == null)
            return null;
        return agenceRepository.findById(agenceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Agence introuvable"));
    }

    private String generateBusinessId() {
        String businessId;
        do {
            businessId = "REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (reclamationRepository.existsByIdReclamation(businessId));
        return businessId;
    }

    private String normalize(String value) {
        if (value == null)
            return null;
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String requireValue(String value, String message) {
        String normalized = normalize(value);
        if (normalized == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        return normalized;
    }
}
