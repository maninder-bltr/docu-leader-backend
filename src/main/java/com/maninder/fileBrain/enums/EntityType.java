package com.maninder.fileBrain.enums;

import lombok.Getter;

@Getter
public enum EntityType {
    PERSON_NAME("Person Name"),
    COMPANY_NAME("Company Name"),
    EMAIL("Email Address"),
    PHONE_NUMBER("Phone Number"),
    DATE("Date"),
    AMOUNT("Amount"),
    CONTRACT_CLAUSE("Contract Clause"),
    PAYMENT_TERM("Payment Term"),
    INTEREST_RATE("Interest Rate"),
    LEGAL_TERM("Legal Term"),
    ADDRESS("Address"),
    INVOICE_NUMBER("Invoice Number"),
    TAX_ID("Tax ID");

    private final String displayName;

    EntityType(String displayName) {
        this.displayName = displayName;
    }

}
