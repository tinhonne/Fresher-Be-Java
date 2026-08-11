package com.example.demo.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.example.demo.security.authorization.AppRole;
import com.example.demo.security.context.AuthenticationFacade;
import com.example.demo.security.context.AuthenticationFacadeImpl;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class AuthenticationFacadeImplTest {

  private final AuthenticationFacade facade = new AuthenticationFacadeImpl();

  @Test
  void returnsAuthenticationAndUsername() {
    Authentication authentication = authentication("user", "ROLE_ADMIN");

    try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
      SecurityContext context = context(authentication);
      holder.when(SecurityContextHolder::getContext).thenReturn(context);

      assertSame(authentication, facade.getAuthentication());
      assertTrue(facade.hasRole(AppRole.ADMIN));
      assertTrue(facade.hasAuthority("ROLE_ADMIN"));
      assertFalse(facade.hasAuthority("ADMIN"));
      assertTrue("user".equals(facade.getCurrentUsername()));
    }
  }

  @Test
  void handlesNullAuthentication() {
    try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
      SecurityContext context = context(null);
      holder.when(SecurityContextHolder::getContext).thenReturn(context);

      assertNull(facade.getAuthentication());
      assertNull(facade.getCurrentUsername());
      assertFalse(facade.hasRole(AppRole.ADMIN));
      assertFalse(facade.hasAuthority("ROLE_ADMIN"));
    }
  }

  private Authentication authentication(String username, String... authorities) {
    return new UsernamePasswordAuthenticationToken(
        username, null, List.of(authorities).stream().map(SimpleGrantedAuthority::new).toList());
  }

  private SecurityContext context(Authentication authentication) {
    SecurityContext context = mock(SecurityContext.class);
    when(context.getAuthentication()).thenReturn(authentication);
    return context;
  }
}
