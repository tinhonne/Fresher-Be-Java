package com.example.demo.security.context;

import com.example.demo.security.authorization.AppRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Spring Security implementation of {@link AuthenticationFacade}. */
@Component
public class AuthenticationFacadeImpl implements AuthenticationFacade {

  /** {@inheritDoc} */
  @Override
  public Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  /** {@inheritDoc} */
  @Override
  public String getCurrentUsername() {
    Authentication authentication = getAuthentication();
    return authentication == null ? null : authentication.getName();
  }

  /** {@inheritDoc} */
  @Override
  public boolean hasRole(AppRole role) {
    return hasAuthority(role.authority());
  }

  /** {@inheritDoc} */
  @Override
  public boolean hasAuthority(String authority) {
    Authentication authentication = getAuthentication();
    return authentication != null
        && authentication.getAuthorities().stream()
            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
  }
}
