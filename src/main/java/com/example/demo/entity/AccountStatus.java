package com.example.demo.entity;

import lombok.Getter;

@Getter
public enum AccountStatus {
    INACTIVE(0),
    ACTIVE(1),
    FROZEN(2),
    PENDING(3);

    private final int code;

    AccountStatus(int code) {
        this.code = code;
    }

    public static AccountStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AccountStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown account status code: " + code);
    }
}
