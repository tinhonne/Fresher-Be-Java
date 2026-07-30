package com.example.demo.constant;

public final class ValidationConstants {
    public static final int USERNAME_MAX_LENGTH = 15;
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 100;
    public static final int USER_NAME_MAX_LENGTH = 20;
    public static final int CUSTOMER_NAME_MAX_LENGTH = 100;
    public static final int CUSTOMER_ADDRESS_MAX_LENGTH = 255;
    public static final int TRANSACTION_CONTENT_MAX_LENGTH = 255;
    public static final int JWT_TOKEN_MAX_LENGTH = 4096;
    public static final int PERMISSION_CODE_MAX_LENGTH = 50;
    public static final int ROLE_NAME_MAX_LENGTH = 50;
    public static final int DESCRIPTION_MAX_LENGTH = 255;
    public static final int ACCOUNT_NUMBER_LENGTH = 13;
    public static final int MONEY_PRECISION = 19;
    public static final int MONEY_SCALE = 2;
    public static final int IDENTITY_NUMBER_LENGTH = 10;
    public static final String ACCOUNT_NUMBER_PATTERN = "\\d{" + ACCOUNT_NUMBER_LENGTH + "}";
    public static final String IDENTITY_NUMBER_PATTERN = "\\d{" + IDENTITY_NUMBER_LENGTH + "}";
    public static final String MOBILE_PATTERN = "\\d{9,10}";
    public static final String NON_BLANK_PATTERN = ".*\\S.*";

    private ValidationConstants() {
    }
}
