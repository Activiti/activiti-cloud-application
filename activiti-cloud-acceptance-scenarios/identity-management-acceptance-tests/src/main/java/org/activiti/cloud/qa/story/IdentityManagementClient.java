package org.activiti.cloud.qa.story;

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
