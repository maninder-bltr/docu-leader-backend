package com.maninder.fileBrain.repository;

import com.maninder.fileBrain.entity.Document;
import com.maninder.fileBrain.entity.User;
import com.maninder.fileBrain.enums.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByUser(User user);

    List<Document> findByUserAndStatus(User user, DocumentStatus status);

    Optional<Document> findByIdAndUser(UUID id, User user);

    boolean existsByIdAndUser(UUID id, User user);
}
