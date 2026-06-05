package com.example.demo.common.infrastructure.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * OpenAPI configuration for Swagger UI OAuth2 support.
 */
@Configuration
@SecurityScheme(name = "keycloak", type = SecuritySchemeType.OAUTH2, flows = @OAuthFlows(authorizationCode = @OAuthFlow(authorizationUrl = "http://localhost:8082/realms/banking-demo/protocol/openid-connect/auth", tokenUrl = "http://localhost:8082/realms/banking-demo/protocol/openid-connect/token")))
public class OpenApiConfig {

	/**
	 * Default constructor for OpenApiConfig.
	 */
	public OpenApiConfig() {
	}

}
