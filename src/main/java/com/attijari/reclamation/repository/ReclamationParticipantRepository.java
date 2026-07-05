package com.attijari.reclamation.repository;

import com.attijari.reclamation.model.ReclamationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReclamationParticipantRepository extends JpaRepository<ReclamationParticipant, Long> {

    boolean existsByReclamation_IdReclamationAndUser_IdUser(String reclamationId, String userId);

    Optional<ReclamationParticipant> findByReclamation_IdReclamationAndUser_IdUser(
            String reclamationId,
            String userId
    );

    List<ReclamationParticipant> findByReclamation_IdReclamationOrderByAddedAtAsc(String reclamationId);

    List<ReclamationParticipant> findByUser_IdUserOrderByAddedAtDesc(String userId);
}
