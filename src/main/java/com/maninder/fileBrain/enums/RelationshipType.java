package com.maninder.fileBrain.enums;

import lombok.Getter;

@Getter
public enum RelationshipType {
    CONTRACT_FOR_INVOICE("Contract for Invoice"),
    PAYMENT_FOR_INVOICE("Payment for Invoice"),
    AMENDMENT_TO_CONTRACT("Amendment to Contract"),
    REFERENCE_TO_DOCUMENT("Reference to Document"),
    ATTACHMENT_TO_EMAIL("Attachment to Email"),
    SUPERSEDES("Supersedes"),
    SUPERSEDED_BY("Superseded By");

    private final String displayName;

    RelationshipType(String displayName) {
        this.displayName = displayName;
    }

}
