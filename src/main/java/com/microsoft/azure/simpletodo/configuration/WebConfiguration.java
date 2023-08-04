package com.microsoft.azure.simpletodo.configuration;

import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;


@Configuration
public class WebConfiguration {

    private final AadAuthenticationProperties properties;

    @Value("${spring-apps.public-url}")
    private String springAppsPublicUrl;

    public WebConfiguration(AadAuthenticationProperties properties) {
        this.properties = properties;
    }

    @Bean
    public WebMvcConfigurer webConfigurer() {
        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("*")
                        .allowedHeaders("*");
            }
        };
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().antMatchers("/", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**");
    }

    @Bean
    public OpenAPI customOpenAPI() {
        OAuthFlow authorizationCode = new OAuthFlow();
        authorizationCode.setAuthorizationUrl(String.format("https://login.microsoftonline.com/%s/oauth2/v2.0/authorize/",
            properties.getProfile().getTenantId()));
        authorizationCode.setRefreshUrl(String.format("https://login.microsoftonline.com/%s/oauth2/v2.0/token/",
            properties.getProfile().getTenantId()));
        authorizationCode.setTokenUrl(String.format("https://login.microsoftonline.com/%s/oauth2/v2.0/token/",
            properties.getProfile().getTenantId()));
        authorizationCode.setScopes(new Scopes()
            .addString(properties.getAppIdUri() + "/ToDo.Read", "Read")
            .addString(properties.getAppIdUri() + "/ToDo.Write", "Write")
            .addString(properties.getAppIdUri() + "/ToDo.Delete", "Delete"));
        OAuthFlows oauthFlows = new OAuthFlows();
        oauthFlows.authorizationCode(authorizationCode);
        SecurityScheme securityScheme = new SecurityScheme();
        securityScheme.setType(SecurityScheme.Type.OAUTH2);
        securityScheme.setFlows(oauthFlows);
        return new OpenAPI()
            .info(new Info()
                .title("RESTful APIs for SimpleTodo")
                .version("0.0.1-SNAPSHOT"))
            .servers(List.of(new Server().url(springAppsPublicUrl)))
            .components(new Components()
                .addSecuritySchemes("Azure AD", securityScheme));
    }
}
