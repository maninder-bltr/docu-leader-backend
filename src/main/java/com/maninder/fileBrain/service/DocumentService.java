package com.maninder.fileBrain.service;

import com.maninder.fileBrain.dto.DocumentDto;
import com.maninder.fileBrain.entity.Document;
import com.maninder.fileBrain.entity.User;
import com.maninder.fileBrain.enums.DocumentStatus;
import com.maninder.fileBrain.repository.DocumentChunkRepository;
import com.maninder.fileBrain.repository.DocumentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final Path fileStoragePath;

    @Value("${file.storage-dir}")
    private String storageDir;

    public Document storeFile(MultipartFile file, User user) throws IOException {
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        assert originalFilename != null;
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String storedFilename = UUID.randomUUID() + extension;
        Path targetPath = fileStoragePath.resolve(storedFilename);

        // Copy file to storage
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Save metadata
        Document document = new Document();
        document.setUser(user);
        document.setFileName(originalFilename);
        document.setFilePath(targetPath.toString());
        document.setStatus(DocumentStatus.UPLOADED);

        return documentRepository.save(document);
    }

    public Document getDocument(UUID id, User user) {
        return documentRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Document not found"));
    }

    public String extractTextFromFile(Path filePath) {
        try {
            FileSystemResource resource = new FileSystemResource(filePath.toFile());
            TikaDocumentReader reader = new TikaDocumentReader(resource);
            List<org.springframework.ai.document.Document> docs = reader.get();
            return docs.stream()
                    .map(org.springframework.ai.document.Document::getText)
                    .collect(Collectors.joining("\n\n"));
        } catch (Exception e) {
            log.error("Failed to extract text from file: {}", filePath, e);
            throw new RuntimeException("Text extraction failed", e);
        }
    }

    public List<DocumentDto> getUserDocuments(User user) {
        List<Document> documents = documentRepository.findByUser(user);
        List<DocumentDto> response = documents.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return response;
    }

    private DocumentDto mapToDto(Document document) {
        DocumentDto dto = new DocumentDto();
        dto.setId(document.getId());
        dto.setFileName(document.getFileName());
        dto.setDocumentType(document.getDocumentType() != null ? document.getDocumentType().name() : null);
        dto.setStatus(document.getStatus().name());
        dto.setCreatedAt(document.getCreatedAt());
        return dto;
    }

    @Transactional
    public void deleteDocument(UUID documentId, User user) {
        Document document = documentRepository.findByIdAndUser(documentId, user)
                .orElseThrow(() -> new RuntimeException("Document not found or access denied"));

        try {
            Path filePath = Paths.get(document.getFilePath());
            Files.deleteIfExists(filePath);
            log.info("Deleted file: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to delete physical file for document: {}", documentId, e);
        }

        // Delete document chunks first (they will be cascade deleted, but explicit is cleaner)
        documentChunkRepository.deleteByDocument(document);

        // Delete the document
        documentRepository.delete(document);

        log.info("Document deleted successfully: {} by user: {}", documentId, user.getEmail());
    }
}
