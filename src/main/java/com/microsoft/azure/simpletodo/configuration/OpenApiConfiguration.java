package com.microsoft.azure.simpletodo.configuration;

import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

/**
 * Configuration for Spring doc OpenAPI (Swagger).
 */
@Configuration
class OpenApiConfiguration {

    private static final String AUTHORIZATION_URL = "https://login.microsoftonline.com/%s/oauth2/v2.0/authorize/";
    private static final String TOKEN_URL = "https://login.microsoftonline.com/%s/oauth2/v2.0/token/";

    private final AadAuthenticationProperties properties;

    OpenApiConfiguration(AadAuthenticationProperties properties) {
        this.properties = properties;
    }

    @Bean
    WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().antMatchers("/", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**");
    }

    @Bean
    OpenAPI customOpenAPI() {
        OAuthFlow authorizationCodeFlow = new OAuthFlow();
        authorizationCodeFlow.setAuthorizationUrl(String.format(AUTHORIZATION_URL, properties.getProfile().getTenantId()));
        authorizationCodeFlow.setRefreshUrl(String.format(TOKEN_URL, properties.getProfile().getTenantId()));
        authorizationCodeFlow.setTokenUrl(String.format(TOKEN_URL, properties.getProfile().getTenantId()));
        authorizationCodeFlow.setScopes(new Scopes()
            .addString(properties.getAppIdUri() + "/ToDo.Read", "Read")
            .addString(properties.getAppIdUri() + "/ToDo.Write", "Write")
            .addString(properties.getAppIdUri() + "/ToDo.Delete", "Delete"));
        OAuthFlows oauthFlows = new OAuthFlows();
        oauthFlows.authorizationCode(authorizationCodeFlow);
        SecurityScheme securityScheme = new SecurityScheme();
        securityScheme.setType(SecurityScheme.Type.OAUTH2);
        securityScheme.setFlows(oauthFlows);
        return new OpenAPI()
            .info(new Info().title("RESTful APIs for SimpleTodo"))
            .components(new Components().addSecuritySchemes("Azure AD", securityScheme));
    }
}
