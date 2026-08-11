package com.example.demo.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.demo.security.authorization.AppPermission;
import org.junit.jupiter.api.Test;

class AppPermissionTest {

  @Test
  void resolvesSelfAndNumericUserRoutes() {
    assertEquals(AppPermission.SELF_VIEW, AppPermission.resolve("GET", "/users/me").orElseThrow());
    assertEquals(AppPermission.USER_VIEW, AppPermission.resolve("GET", "/users/42").orElseThrow());
  }

  @Test
  void resolvesOverlapSensitiveLiteralRoutes() {
    assertEquals(
        AppPermission.CUSTOMER_VIEW,
        AppPermission.resolve("GET", "/customers/search").orElseThrow());
    assertTrue(AppPermission.resolve("GET", "/roles/options").isEmpty());
  }

  @Test
  void numericLongRoutesRejectNonnumericValues() {
    assertEquals(
        AppPermission.CUSTOMER_VIEW, AppPermission.resolve("GET", "/customers/42").orElseThrow());
    assertTrue(AppPermission.resolve("GET", "/customers/not-an-id").isEmpty());
    assertEquals(
        AppPermission.ACCOUNT_APPROVE,
        AppPermission.resolve("PUT", "/accounts/42/approve").orElseThrow());
    assertTrue(AppPermission.resolve("PUT", "/accounts/not-an-id/approve").isEmpty());
  }

  @Test
  void stringAccountNumberRoutesRemainUnconstrained() {
    assertEquals(
        AppPermission.TRANSACTION_VIEW,
        AppPermission.resolve("GET", "/accounts/ABC-123/transactions").orElseThrow());
  }
}
