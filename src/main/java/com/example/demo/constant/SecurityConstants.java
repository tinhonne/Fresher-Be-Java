package com.example.demo.constant;

public final class SecurityConstants {
    public static final String ADMIN_ROLE = "ADMIN";
    public static final String MANAGER_ROLE = "MANAGER";
    public static final String ADMIN_ROLE_NAME = "Admin";
    public static final String MANAGER_ROLE_NAME = "Manager";
    public static final String EMPLOYEE_ROLE_NAME = "Employee";
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String SCOPE_CLAIM = "scope";
    public static final int MIN_HS512_KEY_BYTES = 64;
    public static final String ASSIGN_RESTRICTED_ROLE_AUTHORITY = "USER_ASSIGN_RESTRICTED_ROLE";
    public static final String ACCOUNT_CREATE = "ACCOUNT_CREATE";
    public static final String ACCOUNT_VIEW = "ACCOUNT_VIEW";
    public static final String ACCOUNT_APPROVE = "ACCOUNT_APPROVE";
    public static final String ACCOUNT_REJECT = "ACCOUNT_REJECT";
    public static final String ACCOUNT_FREEZE = "ACCOUNT_FREEZE";
    public static final String ACCOUNT_UNFREEZE = "ACCOUNT_UNFREEZE";
    public static final String ACCOUNT_CLOSE = "ACCOUNT_CLOSE";

    private SecurityConstants() {
    }
}


