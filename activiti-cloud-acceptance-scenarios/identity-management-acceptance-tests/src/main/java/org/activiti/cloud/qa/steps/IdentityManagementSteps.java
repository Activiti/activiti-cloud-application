/*
 * Copyright 2017-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.cloud.qa.steps;

import java.util.stream.Stream;
import net.serenitybdd.core.Serenity;
import org.activiti.cloud.identity.model.UserRoles;
import org.activiti.cloud.qa.config.EnableIdentityManagementContext;
import org.activiti.cloud.qa.service.IdentityManagementClient;
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
