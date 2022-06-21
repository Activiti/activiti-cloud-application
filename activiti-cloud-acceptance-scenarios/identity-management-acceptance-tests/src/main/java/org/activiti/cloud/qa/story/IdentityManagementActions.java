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
package org.activiti.cloud.qa.story;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.thucydides.core.annotations.Steps;

/**
 * Identity Management projects scenarios
 */
public class IdentityManagementActions {

    @Steps
    private IdentityManagementSteps identityManagementSteps;

    public IdentityManagementActions() {

    }

    @When("the user retrieves his roles")
    public void getRoles() {
        identityManagementSteps.getRoles();
    }


    @Then("roles list contains global role $role")
    public void containsGlobalRole(String role) {
        identityManagementSteps.containsGlobalAccessRole(role);
    }

    @Then("roles list for $application contains  role $role")
    public void containsApplicationRole(String application, String role) {
        identityManagementSteps.containsApplicationAccessRole(application, role);
    }
}
