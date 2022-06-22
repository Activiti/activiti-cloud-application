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
package org.activiti.cloud.qa.service;

import feign.Headers;
import org.activiti.cloud.identity.model.UserRoles;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(value = "identityManagementClient", url = "${identityManagement.url}")
public interface IdentityManagementClient {

    @RequestMapping(method = RequestMethod.GET, value = "/roles")
    @Headers("Content-Type: application/json")
    UserRoles getUserRoles();

    /**List<Group> getGroups(@RequestParam(value = "search", required = false) String search,
                                 @RequestParam(value = "role", required = false)  Set<String> roles,
                                 @RequestParam(value = "application", required = false)  String application) {
        public List<User> getUsers(@RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "role", required = false)  Set<String> roles,
            @RequestParam(value = "group", required = false)  Set<String> groups,
            @RequestParam(value = "application", required = false)  String application) {**/

}
