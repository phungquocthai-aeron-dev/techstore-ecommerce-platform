package com.techstore.warehouse.constant;

public enum TransactionStatus {
    PENDING,
    COMPLETED,
    CANCELLED;

    public static boolean isValid(String value) {
        for (TransactionStatus status : values()) {
            if (status.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
