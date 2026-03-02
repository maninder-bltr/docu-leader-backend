package com.maninder.fileBrain.exception;

public class ProcessingFailedException extends RuntimeException {
    public ProcessingFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
