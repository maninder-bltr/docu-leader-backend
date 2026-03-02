package com.maninder.fileBrain.service;

import com.maninder.fileBrain.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class AIService {
    private final ChatClient chatClient;

    @Value("classpath:prompts/invoice-extraction.st")
    private Resource invoiceExtractionPrompt;

    @Value("classpath:prompts/qa-legal.st")
    private Resource qaLegalPrompt;

    @Value("classpath:prompts/qa-financial.st")
    private Resource qaFinancialPrompt;

    @Value("classpath:prompts/qa-technical.st")
    private Resource qaTechnicalPrompt;

    @Value("classpath:prompts/reminder.st")
    private Resource reminderPrompt;

    public AIService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String extractInvoiceData(String text) {
        PromptTemplate promptTemplate = new PromptTemplate(invoiceExtractionPrompt);
        Prompt prompt = promptTemplate.create(Map.of("document_text", text));
        return chatClient.prompt(prompt).call().content();
    }

    public String answerQuestion(String question, String context, String documentType) {
        Resource template = selectPromptTemplate(documentType);
        PromptTemplate promptTemplate = new PromptTemplate(template);
        Prompt prompt = promptTemplate.create(Map.of(
                "question", question,
                "context", context
        ));
        return chatClient.prompt(prompt).call().content();
    }

    private Resource selectPromptTemplate(String documentType) {
        return switch (documentType.toLowerCase()) {
            case "legal" -> qaLegalPrompt;
            case "financial" -> qaFinancialPrompt;
            case "technical" -> qaTechnicalPrompt;
            default -> qaTechnicalPrompt; // fallback
        };
    }

    public String classifyDocument(String text) {
        String prompt = "Classify the following document into one of these categories: LEGAL, FINANCIAL, TECHNICAL, INVOICE, OTHER. Return only the category name.\n\nDocument text:\n" + text;
        return chatClient.prompt(prompt).call().content();
    }

    public String generateReminder(String vendor, String invoiceNumber, Long dueDateEpoch) {
        String dueDateStr = DateUtils.formatDate(dueDateEpoch);
        PromptTemplate promptTemplate = new PromptTemplate(reminderPrompt);
        Prompt prompt = promptTemplate.create(Map.of(
                "vendor", vendor,
                "invoiceNumber", invoiceNumber,
                "dueDate", dueDateStr != null ? dueDateStr : "unknown date"
        ));
        return chatClient.prompt(prompt).call().content();
    }
}
