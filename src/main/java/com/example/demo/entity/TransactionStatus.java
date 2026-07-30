package com.example.demo.entity;

import lombok.Getter;

@Getter
public enum TransactionStatus {
    SUCCESS(0),
    INSUFFICIENT_BALANCE(3),
    SYSTEM_ERROR(4);

    private final int code;

    TransactionStatus(int code) {
        this.code = code;
    }

    /**
     * Resolves the persistent integer code to its transaction status.
     *
     * @param code the persistent code; {@code null} produces {@code null}
     * @return the matching status, or {@code null} for a null code
     * @throws IllegalArgumentException when the code is not recognized
     */
    public static TransactionStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (TransactionStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }

        throw new IllegalArgumentException("Unknown transaction status code: " + code);
    }
}
