package com.example.demo.security.authorization;

import static com.example.demo.constant.SecurityConstants.ROLE_PREFIX;

import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

@Component
public class RequestAuthorizationManager
    implements AuthorizationManager<RequestAuthorizationContext> {

  @Override
  public AuthorizationDecision check(
      Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {
    Authentication authentication = authenticationSupplier.get();
    if (authentication == null || !authentication.isAuthenticated()) {
      return new AuthorizationDecision(false);
    }

    String path = context.getRequest().getServletPath();
    if (path.isEmpty()) {
      path =
          context
              .getRequest()
              .getRequestURI()
              .substring(context.getRequest().getContextPath().length());
    }

    return AppPermission.resolve(context.getRequest().getMethod(), path)
        .map(
            permission ->
                new AuthorizationDecision(
                    authentication.getAuthorities().stream()
                        .map(authority -> role(authority.getAuthority()))
                        .flatMap(Optional::stream)
                        .anyMatch(
                            role ->
                                permission == AppPermission.SELF_VIEW
                                    || permission == AppPermission.SELF_PASSWORD_UPDATE
                                    || role.hasPermission(permission))))
        .orElseGet(() -> new AuthorizationDecision(false));
  }

  private Optional<AppRole> role(String authority) {
    if (!authority.startsWith(ROLE_PREFIX)) {
      return Optional.empty();
    }
    try {
      return Optional.of(AppRole.valueOf(authority.substring(ROLE_PREFIX.length())));
    } catch (IllegalArgumentException exception) {
      return Optional.empty();
    }
  }
}
