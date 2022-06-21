package org.activiti.cloud.qa.story;

import java.util.stream.Stream;
import net.serenitybdd.core.Serenity;
import org.activiti.cloud.identity.model.UserRoles;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

@EnableIdentityManagementContext
public class IdentityManagementSteps {

    private static final String ROLES = "roles";

    @Autowired
    private IdentityManagementClient identityManagementClient;

    public IdentityManagementSteps() {
    }

    public void getRoles() {
        UserRoles userRoles = identityManagementClient.getUserRoles();
        Serenity.setSessionVariable(ROLES).to(userRoles);
    }

    public void containsGlobalAccessRole(String role) {
        UserRoles roles = Serenity.sessionVariableCalled(ROLES);
        Assertions
            .assertThat(roles.getGlobalAccess().getRoles())
            .contains(role);
    }

    public void containsApplicationAccessRole(String applicationName, String role) {
        UserRoles roles = Serenity.sessionVariableCalled(ROLES);
        Stream<String> applicationRoles = roles
            .getApplicationAccess()
            .stream()
            .filter(a -> a.getName().equals(applicationName))
            .flatMap(ar -> ar.getRoles().stream());
        Assertions
            .assertThat(applicationRoles)
            .contains(role);
    }

}
