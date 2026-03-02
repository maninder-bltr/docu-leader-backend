package com.maninder.fileBrain.enums;

import lombok.Getter;

@Getter
public enum ConversationType {
    QUERY("General Query"),
    DOCUMENT_ANALYSIS("Document Analysis"),
    FINANCIAL_ADVICE("Financial Advice"),
    CONTRACT_REVIEW("Contract Review"),
    SUMMARIZATION("Summarization");

    private final String displayName;

    ConversationType(String displayName) {
        this.displayName = displayName;
    }

}
