package com.attijari.reclamation.repository;

import com.attijari.reclamation.model.GroupeDroit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupeDroitRepository extends JpaRepository<GroupeDroit, String> {

    boolean existsByNomIgnoreCase(String nom);

    boolean existsByNomIgnoreCaseAndGroupIdNot(String nom, String groupId);
}
