package com.maninder.fileBrain.service;

import com.maninder.fileBrain.entity.DocumentChunk;
import com.maninder.fileBrain.repository.DocumentChunkRepository;
import com.pgvector.PGvector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VectorSearchService {

    private final DocumentChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;

    public VectorSearchService(DocumentChunkRepository chunkRepository, EmbeddingService embeddingService) {
        this.chunkRepository = chunkRepository;
        this.embeddingService = embeddingService;
    }

    public List<DocumentChunk> searchSimilarChunks(String query, int limit, UUID userId) {
        float[] queryEmbedding = embeddingService.generateEmbedding(query);
        return chunkRepository.findSimilarChunksByUser(userId, queryEmbedding, limit);
    }

    public String buildContext(List<DocumentChunk> chunks) {
        return chunks.stream()
                .map(chunk -> {
                    String fileName = chunk.getDocument() != null ?
                            chunk.getDocument().getFileName() : "Unknown";
                    return fileName + ":\n" + chunk.getContent();
                })
                .collect(Collectors.joining("\n\n---\n\n"));
    }
}
