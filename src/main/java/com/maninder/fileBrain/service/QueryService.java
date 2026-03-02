package com.maninder.fileBrain.service;

import com.maninder.fileBrain.dto.QueryResponse;
import com.maninder.fileBrain.entity.Document;
import com.maninder.fileBrain.entity.DocumentChunk;
import com.maninder.fileBrain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueryService {

    private final VectorSearchService vectorSearchService;
    private final AIService aiService;

    public QueryResponse answerQuery(String question, User user) {
        // 1. Find relevant chunks
        List<DocumentChunk> chunks = vectorSearchService.searchSimilarChunks(question, 5, user.getId());

        // 2. Build context
        String context = vectorSearchService.buildContext(chunks);

        // 3. Determine document type from first chunk's document (simplified)
        String docType = "technical";
        if (!chunks.isEmpty()) {
            Document doc = chunks.getFirst().getDocument();
            if (doc.getDocumentType() != null) {
                docType = doc.getDocumentType().name().toLowerCase();
            }
        }

        // 4. Get answer
        String answer = aiService.answerQuestion(question, context, docType);

        // 5. Collect source filenames
        List<String> sources = chunks.stream()
                .map(chunk -> chunk.getDocument().getFileName())
                .distinct()
                .collect(Collectors.toList());

        return new QueryResponse(answer, sources);
    }
}
