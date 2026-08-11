package com.example.demo.security.authorization;

import static com.example.demo.constant.SecurityConstants.ROLE_PREFIX;

import java.util.EnumSet;
import java.util.Set;

public enum AppRole {
  EMPLOYEE(
      AppPermission.CUSTOMER_VIEW,
      AppPermission.CUSTOMER_CREATE,
      AppPermission.CUSTOMER_UPDATE,
      AppPermission.ACCOUNT_VIEW,
      AppPermission.ACCOUNT_CREATE,
      AppPermission.TRANSACTION_VIEW,
      AppPermission.TRANSACTION_CREATE),
  MANAGER(
      AppPermission.CUSTOMER_VIEW,
      AppPermission.CUSTOMER_CREATE,
      AppPermission.CUSTOMER_UPDATE,
      AppPermission.ACCOUNT_VIEW,
      AppPermission.ACCOUNT_CREATE,
      AppPermission.ACCOUNT_APPROVE,
      AppPermission.ACCOUNT_FREEZE,
      AppPermission.ACCOUNT_CLOSE,
      AppPermission.TRANSACTION_VIEW,
      AppPermission.TRANSACTION_CREATE,
      AppPermission.USER_VIEW,
      AppPermission.USER_CREATE,
      AppPermission.USER_UPDATE),
  ADMIN(AppPermission.USER_VIEW, AppPermission.USER_CREATE, AppPermission.USER_UPDATE);

  private final Set<AppPermission> permissions;

  AppRole(AppPermission first, AppPermission... remaining) {
    permissions = EnumSet.of(first, remaining);
  }

  public boolean hasPermission(AppPermission permission) {
    return permissions.contains(permission);
  }

  public String authority() {
    return ROLE_PREFIX + name();
  }
}
