package com.maninder.fileBrain.enums;

import lombok.Getter;

@Getter
public enum RecordType {
    INVOICE("Invoice"),
    RECEIPT("Receipt"),
    PAYMENT("Payment"),
    EXPENSE("Expense"),
    CONTRACT("Contract"),
    BANK_STATEMENT("Bank Statement"),
    TAX_DOCUMENT("Tax Document");

    private final String displayName;

    RecordType(String displayName) {
        this.displayName = displayName;
    }

}
