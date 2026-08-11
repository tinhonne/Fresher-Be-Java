package com.example.demo.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.demo.security.authorization.RequestAuthorizationManager;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

class RequestAuthorizationManagerTest {

  private final RequestAuthorizationManager manager = new RequestAuthorizationManager();

  @Test
  void enforcesRoleMatrixWithoutTrustingPermissionAuthorities() {
    assertTrue(decide("POST", "/customers", "ROLE_EMPLOYEE"));
    assertTrue(decide("PUT", "/accounts/7/approve", "ROLE_MANAGER"));
    assertFalse(decide("POST", "/customers", "ROLE_ADMIN"));
    assertFalse(decide("POST", "/customers", "CUSTOMER_CREATE"));
  }

  @Test
  void deniesUnresolvedAndUnknownRoutes() {
    assertFalse(decide("PUT", "/accounts/7/reject", "ROLE_MANAGER"));
    assertFalse(decide("PUT", "/accounts/7/unfreeze", "ROLE_MANAGER"));
    assertFalse(decide("GET", "/unknown", "ROLE_ADMIN"));
  }

  @Test
  void matchesNestedAccountPathsToAccountPermission() {
    assertTrue(decide("GET", "/customers/7/accounts", "ROLE_EMPLOYEE"));
    assertTrue(decide("GET", "/customers/7/accounts/active", "ROLE_MANAGER"));
    assertFalse(decide("GET", "/customers/7/accounts", "ROLE_ADMIN"));
  }

  @Test
  void distinguishesHttpMethodsAndPathVariables() {
    assertTrue(decide("GET", "/customers/7", "ROLE_EMPLOYEE"));
    assertFalse(decide("POST", "/customers/7", "ROLE_EMPLOYEE"));
    assertTrue(decide("GET", "/accounts/ABC-123/transactions", "ROLE_EMPLOYEE"));
  }

  @Test
  void allowsSelfServiceForEveryKnownRole() {
    assertTrue(decide("GET", "/users/me", "ROLE_EMPLOYEE"));
    assertTrue(decide("PATCH", "/users/me/password", "ROLE_MANAGER"));
    assertFalse(decide("GET", "/users/me", "ROLE_UNKNOWN"));
  }

  private boolean decide(String method, String path, String authority) {
    MockHttpServletRequest request = new MockHttpServletRequest(method, path);
    request.setServletPath(path);
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
            "user", null, List.of(new SimpleGrantedAuthority(authority)));
    return manager
        .check(() -> authentication, new RequestAuthorizationContext(request))
        .isGranted();
  }
}
