package com.maninder.fileBrain.enums;

import lombok.Getter;

@Getter
public enum ProcessingStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    PARTIALLY_COMPLETED("Partially Completed");

    private final String displayName;

    ProcessingStatus(String displayName) {
        this.displayName = displayName;
    }

}
