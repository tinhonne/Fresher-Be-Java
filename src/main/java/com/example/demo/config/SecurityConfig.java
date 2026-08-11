package com.example.demo.config;

import static com.example.demo.constant.SecurityConstants.EMPTY_SCOPE;
import static com.example.demo.constant.SecurityConstants.SCOPE_CLAIM;

import com.example.demo.config.properties.SecurityProperties;
import com.example.demo.security.authorization.RequestAuthorizationManager;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final SecurityProperties securityProperties;

  /**
   * Creates the security configuration from validated application properties.
   *
   * @param securityProperties password encoding and CORS settings
   */
  public SecurityConfig(SecurityProperties securityProperties) {
    this.securityProperties = securityProperties;
  }

  private static final String[] PUBLIC_ENDPOINTS = {
    "/auth/token", "/auth/introspect", "/auth/logout"
  };

  /**
   * Builds the stateless HTTP security filter chain for public and authenticated endpoints.
   *
   * @param httpSecurity the HTTP security builder
   * @param corsConfigurationSource the application CORS configuration
   * @return the configured security filter chain
   * @throws Exception if the filter chain cannot be built
   */
  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity httpSecurity,
      CorsConfigurationSource corsConfigurationSource,
      RequestAuthorizationManager authorizationManager)
      throws Exception {

    httpSecurity
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            request ->
                request
                    .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS)
                    .permitAll()
                    .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml")
                    .permitAll()
                    .anyRequest()
                    .access(authorizationManager))
        .oauth2ResourceServer(
            oauth2 ->
                oauth2.jwt(
                    jwtConfigurer ->
                        jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter())));

    return httpSecurity.build();
  }

  /**
   * Creates the BCrypt password encoder using the configured strength.
   *
   * @return the application password encoder
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(securityProperties.passwordStrength());
  }

  /**
   * Creates the CORS configuration applied to all application endpoints.
   *
   * @return the configured CORS source
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(securityProperties.cors().allowedOrigins());
    configuration.setAllowedMethods(
        List.of(
            HttpMethod.GET.name(),
            HttpMethod.POST.name(),
            HttpMethod.PUT.name(),
            HttpMethod.PATCH.name(),
            HttpMethod.DELETE.name(),
            HttpMethod.OPTIONS.name()));
    configuration.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE));
    configuration.setAllowCredentials(false);
    configuration.setMaxAge(securityProperties.cors().maxAge().toSeconds());

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  /**
   * Maps authorities from the application's JWT scope claim without a prefix.
   *
   * @return the configured JWT authentication converter
   */
  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter =
        new JwtGrantedAuthoritiesConverter();
    grantedAuthoritiesConverter.setAuthorityPrefix(EMPTY_SCOPE);
    grantedAuthoritiesConverter.setAuthoritiesClaimName(SCOPE_CLAIM);

    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
    return converter;
  }
}
