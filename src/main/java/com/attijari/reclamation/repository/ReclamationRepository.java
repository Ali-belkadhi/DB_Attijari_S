package com.attijari.reclamation.repository;

import com.attijari.reclamation.model.Reclamation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReclamationRepository extends JpaRepository<Reclamation, String> {

    boolean existsByIdReclamation(String idReclamation);

    boolean existsByAgence_IdAgence(Long agenceId);

    boolean existsBySender_IdUser(String senderId);

    boolean existsByReceivers_IdUser(String receiverId);

    boolean existsByDestinations_IdEquipe(Long equipeId);

    @Query("""
            SELECT r FROM Reclamation r
            WHERE r.sender.idUser <> :idUser
              AND (
                   EXISTS (
                       SELECT 1 FROM r.receivers u WHERE u.idUser = :idUser
                   )
                   OR EXISTS (
                       SELECT 1 FROM r.participants p WHERE p.user.idUser = :idUser
                   )
              )
            ORDER BY r.createdAt DESC
            """)
    List<Reclamation> findReceivedByUser(@Param("idUser") String idUser);

    /**
     * Réclamations où l'utilisateur est expéditeur OU destinataire direct.
     * On évite DISTINCT sur des colonnes CLOB (Oracle ORA-00932).
     */
    @Query("""
            SELECT r FROM Reclamation r
            WHERE r.sender.idUser = :idUser
               OR EXISTS (
                   SELECT 1 FROM r.receivers u WHERE u.idUser = :idUser
               )
               OR EXISTS (
                   SELECT 1 FROM r.participants p WHERE p.user.idUser = :idUser
               )
            ORDER BY r.createdAt DESC
            """)
    List<Reclamation> findBySenderOrReceiver(@Param("idUser") String idUser);

    default boolean existsByAgenceIdAgence(Long agenceId) {
        return existsByAgence_IdAgence(agenceId);
    }

    default boolean existsByClientIdUser(String userId) {
        return existsBySender_IdUser(userId) || existsByReceivers_IdUser(userId);
    }
}
