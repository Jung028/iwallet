package com.alipay.business.core.model.exception;

public class RepositoryException extends RuntimeException {
    // For throwing with just a message (e.g., "0 rows affected")
    public RepositoryException(String message) {
        super(message);
    }

    // For wrapping a caught DB exception — NEVER lose the original cause
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
