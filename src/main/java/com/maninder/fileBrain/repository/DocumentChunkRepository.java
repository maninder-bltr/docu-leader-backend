package com.maninder.fileBrain.repository;

import com.maninder.fileBrain.entity.Document;
import com.maninder.fileBrain.entity.DocumentChunk;
import com.pgvector.PGvector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, UUID> {

    @Query(value = "SELECT dc.* FROM document_chunks dc\n" +
            "        JOIN documents d ON dc.document_id = d.id\n" +
            "        WHERE d.user_id = :userId ORDER BY embedding <=> (:embedding)::vector LIMIT :limit", nativeQuery = true)
    List<DocumentChunk> findSimilarChunksByUser(@Param("userId") UUID userId, @Param("embedding") float[] embedding, @Param("limit") int limit);

    void deleteByDocument(Document document);
}
