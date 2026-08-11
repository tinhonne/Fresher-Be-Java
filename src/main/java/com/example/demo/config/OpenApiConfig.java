package com.example.demo.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
    name = com.example.demo.constant.OpenApiConstants.SECURITY_SCHEME_NAME,
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT")
public class OpenApiConfig {

  private static final String API_TITLE = "Bank App API";
  private static final String API_VERSION = "1.0";

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI().info(new Info().title(API_TITLE).version(API_VERSION));
  }
}
