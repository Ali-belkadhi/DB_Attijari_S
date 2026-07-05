package com.attijari.reclamation.repository;

import com.attijari.reclamation.model.Agence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgenceRepository extends JpaRepository<Agence, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdAgenceNot(String code, Long idAgence);

    Optional<Agence> findByCodeIgnoreCase(String code);

    Optional<Agence> findByNomIgnoreCase(String nom);
}
