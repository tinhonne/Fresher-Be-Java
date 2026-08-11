package com.example.demo.constant;

public final class SecurityConstants {
  public static final String ADMIN_ROLE_NAME = "Admin";
  public static final String MANAGER_ROLE_NAME = "Manager";
  public static final String EMPLOYEE_ROLE_NAME = "Employee";
  public static final String ROLE_PREFIX = "ROLE_";
  public static final String SCOPE_CLAIM = "scope";
  public static final int MIN_HS512_KEY_BYTES = 64;
  public static final String EMPTY_SCOPE = "";
  public static final String SCOPE_DELIMITER = " ";

  private SecurityConstants() {}
}
