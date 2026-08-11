package com.example.demo.security.authorization;

import java.util.Arrays;
import java.util.Optional;
import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;

public enum AppPermission {
  CUSTOMER_CREATE(route(HttpMethod.POST, "/customers")),
  CUSTOMER_VIEW(
      route(HttpMethod.GET, "/customers"),
      route(HttpMethod.GET, "/customers/{id:\\d+}"),
      route(HttpMethod.GET, "/customers/search"),
      route(HttpMethod.GET, "/customers/by-field")),
  CUSTOMER_UPDATE(
      route(HttpMethod.PUT, "/customers/{id:\\d+}"),
      route(HttpMethod.PUT, "/customers/{id:\\d+}/status"),
      route(HttpMethod.DELETE, "/customers/{id:\\d+}")),
  ACCOUNT_CREATE(route(HttpMethod.POST, "/accounts")),
  ACCOUNT_VIEW(
      route(HttpMethod.GET, "/accounts"),
      route(HttpMethod.GET, "/accounts/{id:\\d+}"),
      route(HttpMethod.GET, "/accounts/number/{accountNumber}"),
      route(HttpMethod.GET, "/accounts/by-number/{accountNumber}"),
      route(HttpMethod.GET, "/accounts/{id:\\d+}/active"),
      route(HttpMethod.GET, "/customers/{id:\\d+}/accounts"),
      route(HttpMethod.GET, "/customers/{id:\\d+}/accounts/active"),
      route(HttpMethod.GET, "/customers/{id:\\d+}/accounts/inactive")),
  ACCOUNT_APPROVE(route(HttpMethod.PUT, "/accounts/{id:\\d+}/approve")),
  ACCOUNT_REJECT(route(HttpMethod.PUT, "/accounts/{id:\\d+}/reject")),
  ACCOUNT_FREEZE(route(HttpMethod.PUT, "/accounts/{id:\\d+}/freeze")),
  ACCOUNT_UNFREEZE(route(HttpMethod.PUT, "/accounts/{id:\\d+}/unfreeze")),
  ACCOUNT_CLOSE(route(HttpMethod.PUT, "/accounts/{id:\\d+}/close")),
  TRANSACTION_CREATE(route(HttpMethod.POST, "/transactions/transfer")),
  TRANSACTION_VIEW(route(HttpMethod.GET, "/accounts/{accountNumber}/transactions")),
  USER_CREATE(route(HttpMethod.POST, "/users")),
  SELF_VIEW(route(HttpMethod.GET, "/users/me")),
  SELF_PASSWORD_UPDATE(route(HttpMethod.PATCH, "/users/me/password")),
  USER_VIEW(route(HttpMethod.GET, "/users"), route(HttpMethod.GET, "/users/{id:\\d+}")),
  USER_UPDATE(route(HttpMethod.PATCH, "/users/{id:\\d+}"));

  private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

  private final Route[] routes;

  AppPermission(Route... routes) {
    this.routes = routes;
  }

  public boolean matches(String requestMethod, String path) {
    return Arrays.stream(routes)
        .anyMatch(
            route ->
                route.method().matches(requestMethod)
                    && PATH_MATCHER.match(route.pathPattern(), path));
  }

  public static Optional<AppPermission> resolve(String requestMethod, String path) {
    return resolve(requestMethod, path, values());
  }

  static Optional<AppPermission> resolve(
      String requestMethod, String path, AppPermission... permissions) {
    AppPermission match = null;
    for (AppPermission permission : permissions) {
      if (!permission.matches(requestMethod, path)) {
        continue;
      }
      if (match != null) {
        throw new IllegalStateException(
            "Multiple AppPermissions match "
                + requestMethod
                + " "
                + path
                + ": "
                + match
                + ", "
                + permission);
      }
      match = permission;
    }
    return Optional.ofNullable(match);
  }

  private static Route route(HttpMethod method, String pathPattern) {
    return new Route(method, pathPattern);
  }

  private record Route(HttpMethod method, String pathPattern) {}
}
