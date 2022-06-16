package org.activiti.cloud.modeling;

import java.util.Optional;
import org.activiti.api.runtime.shared.security.SecurityContextTokenProvider;
import org.activiti.cloud.identity.keycloak.KeycloakProperties;
import org.activiti.cloud.identity.keycloak.KeycloakTokenProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({KeycloakProperties.class})
public class IdentityAdapterAuthenticationTestConfiguration {

    @Autowired
    private KeycloakProperties keycloakProperties;

    @Bean
    public SecurityContextTokenProvider securityContextTokenProvider() {
        return () -> Optional.of(new KeycloakTokenProducer(keycloakProperties)
                               .withTestUser("testuser")
                               .withTestPassword("password")
                               .withResource("activiti")
                               .getAccessTokenString());
    }

}
