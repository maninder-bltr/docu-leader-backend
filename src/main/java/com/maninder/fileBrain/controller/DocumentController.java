package com.maninder.fileBrain.controller;

import com.maninder.fileBrain.annotation.CurrentUser;
import com.maninder.fileBrain.dto.DocumentDto;
import com.maninder.fileBrain.dto.DocumentUploadResponse;
import com.maninder.fileBrain.dto.QueryResponse;
import com.maninder.fileBrain.entity.Document;
import com.maninder.fileBrain.entity.User;
import com.maninder.fileBrain.service.DocumentProcessingService;
import com.maninder.fileBrain.service.DocumentService;
import com.maninder.fileBrain.service.QueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@Slf4j
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentProcessingService processingService;
    private final QueryService queryService;

    public DocumentController(DocumentService documentService, DocumentProcessingService processingService, QueryService queryService) {
        this.documentService = documentService;
        this.processingService = processingService;
        this.queryService = queryService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
            @CurrentUser User user,
            @RequestParam("file") MultipartFile file) throws IOException {

        Document document = documentService.storeFile(file, user);
        processingService.processDocument(document.getId(), user.getId());

        DocumentUploadResponse response = new DocumentUploadResponse(
                document.getId(),
                document.getFileName(),
                document.getStatus().name()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/query")
    public ResponseEntity<QueryResponse> query(
            @CurrentUser User user,
            @Valid @RequestBody String request) {

        QueryResponse response = queryService.answerQuery(request, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DocumentDto>> getUserDocuments(@CurrentUser User user) {
        return ResponseEntity.ok(documentService.getUserDocuments(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(
            @CurrentUser User user,
            @PathVariable UUID id) {

        documentService.deleteDocument(id, user);
        return ResponseEntity.noContent().build();
    }
}
