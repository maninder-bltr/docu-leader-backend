package com.maninder.fileBrain.service;

import com.maninder.fileBrain.entity.Document;
import com.maninder.fileBrain.entity.DocumentChunk;
import com.maninder.fileBrain.entity.User;
import com.maninder.fileBrain.enums.DocumentStatus;
import com.maninder.fileBrain.enums.DocumentType;
import com.maninder.fileBrain.repository.DocumentChunkRepository;
import com.maninder.fileBrain.repository.DocumentRepository;
import com.maninder.fileBrain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class DocumentProcessingService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DocumentChunkRepository chunkRepository;
    private final AIService aiService;
    private final EmbeddingService embeddingService;
    private final InvoiceExtractionService invoiceExtractionService;

    @Value("${app.chunk-size:1000}")
    private int chunkSize;

    @Value("${app.chunk-overlap:200}")
    private int chunkOverlap;

    public DocumentProcessingService(DocumentRepository documentRepository, UserRepository userRepository, DocumentChunkRepository chunkRepository, AIService aiService, EmbeddingService embeddingService, InvoiceExtractionService invoiceExtractionService) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.chunkRepository = chunkRepository;
        this.aiService = aiService;
        this.embeddingService = embeddingService;
        this.invoiceExtractionService = invoiceExtractionService;
    }

    @Async
    @Transactional
    public void processDocument(UUID documentId, UUID userId) {
        Document document = documentRepository.findByIdAndUser(documentId,
                        userRepository.getReferenceById(userId))
                .orElseThrow(() -> new RuntimeException("Document not found"));

        try {
            document.setStatus(DocumentStatus.PROCESSING);
            documentRepository.save(document);

            // Step 1: Extract text using Tika
            Path filePath = Path.of(document.getFilePath());
            FileSystemResource resource = new FileSystemResource(filePath.toFile());
            TikaDocumentReader reader = new TikaDocumentReader(resource);
            List<org.springframework.ai.document.Document> tikaDocs = reader.get();
            String fullText = tikaDocs.stream()
                    .map(org.springframework.ai.document.Document::getText)
                    .reduce("", (a, b) -> a + "\n\n" + b);

            // Step 2: Split into chunks
            List<String> chunks = splitText(fullText, chunkSize, chunkOverlap);

            // Step 3: Generate embeddings and save chunks
            List<DocumentChunk> chunkEntities = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                String chunkText = chunks.get(i);

                // Generate embedding as float array
                float[] embeddingArray = embeddingService.generateEmbedding(chunkText);

                // Create chunk entity
                DocumentChunk chunk = new DocumentChunk();
                chunk.setDocument(document);
                chunk.setContent(chunkText);
                chunk.setEmbedding(embeddingArray);
                chunk.setChunkIndex(i);

                chunkEntities.add(chunk);
            }
            chunkRepository.saveAll(chunkEntities);

            // Step 4: Classify document type
            String typeStr = aiService.classifyDocument(fullText);
            DocumentType docType = DocumentType.valueOf(typeStr.toUpperCase());
            document.setDocumentType(docType);
            document.setStatus(DocumentStatus.COMPLETED);
            documentRepository.save(document);

            // Step 5: If invoice, extract invoice data
            if (docType == DocumentType.INVOICE) {
                User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
                invoiceExtractionService.extractAndSaveInvoice(document, fullText, user);
            }

            log.info("Document {} processed successfully with {} chunks", documentId, chunkEntities.size());

        } catch (Exception e) {
            log.error("Error processing document: " + documentId, e);
            document.setStatus(DocumentStatus.FAILED);
            documentRepository.save(document);
        }
    }

    private List<String> splitText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        int length = text.length();
        int start = 0;
        while (start < length) {
            int end = Math.min(start + chunkSize, length);
            // try to end at a sentence or paragraph
            if (end < length) {
                int lastPeriod = text.lastIndexOf('.', end);
                if (lastPeriod > start + chunkSize / 2) {
                    end = lastPeriod + 1;
                }
            }
            chunks.add(text.substring(start, end));
            if(end>= length) break;
            start = Math.max(start+1, end-overlap);
        }
        return chunks;
    }
}
