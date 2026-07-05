package com.attijari.reclamation.repository;

import com.attijari.reclamation.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByIdUser(String idUser);

    List<User> findAllByIdUserIn(Set<String> idUsers);

    boolean existsByEmail(String email);

    boolean existsByIdUser(String idUser);

    boolean existsByAgence_IdAgence(Long agenceId);

    boolean existsByGroupeDroit_GroupId(String groupId);

    long deleteByIdUser(String idUser);
}
