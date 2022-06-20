package org.activiti.cloud.identity.adapter;

import java.util.Optional;
import org.activiti.api.runtime.shared.security.SecurityContextTokenProvider;
import org.activiti.cloud.services.test.identity.keycloak.KeycloakTokenProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdentityAdapterAuthenticationTestConfiguration {

    @Bean
    public SecurityContextTokenProvider securityContextTokenProvider(@Value("${keycloak.auth-server-url:}") String authServerUrl,
                                                                     @Value("${keycloak.realm:}") String realm) {
        return () -> Optional.of(new KeycloakTokenProducer(authServerUrl, realm)
                                     .withTestUser("testuser")
                                     .withTestPassword("password")
                                     .withResource("activiti")
                                     .getAccessTokenString());
    }

}
