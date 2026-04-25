package com.techstore.chatbot.exception;

public class QuotaExceededException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public QuotaExceededException(String message) {
        super(message);
    }
}
